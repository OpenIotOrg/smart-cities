package org.openiot.gsndatapusher.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public class SensorManager<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> implements RejectedExecutionHandler, SensorStatusChangedListener {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SensorManager.class);

	private ScheduledThreadPoolExecutor sensorThreadPool;
	private ScheduledThreadPoolExecutor internalThreadPool;
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient client;
	private final C config;
	private final ObservableList<Sensor> sensors = ObservableCollections.observableList(new ArrayList<Sensor>());
	private SensorStatus status = new SensorStatus();
	private String lastData;
	private final int multiplicity;
	private int threadCount;
	private int connectionCount;
	private int interval = 1000;
	private double averageExecutionTime;
	private double averageDuration;
	private int failures = 0;
	private String displayName;
	private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
	private List<SensorStatusChangedListener> listeners = new ArrayList<>();
	private final List<ScheduledFuture<?>> runningSensorTasks = new ArrayList<>();
	private final List<ScheduledFuture<?>> runningInternalTasks = new ArrayList<>();
	private final Map<SensorState, Integer> childStatus = new HashMap<>();

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public SensorManager(SensorState initialState, int multiplicity, int threadCount, int connectionCount, String displayName, C config, List<Double> ids) {
		for (SensorState state : SensorState.values()) {
			childStatus.put(state, state == initialState ? multiplicity : 0);
		}
		setStatus(initialState);
		setDisplayName(displayName);
		this.multiplicity = multiplicity;
		this.threadCount = threadCount;
		this.connectionCount = connectionCount;
		this.config = config;
		internalThreadPool = new ScheduledThreadPoolExecutor(1, this);
		for (C newConfig : config.createAdaptedCopies(multiplicity, ids)) {
			Sensor sensor = new Sensor(initialState, newConfig, client);
			sensor.addListener(this);
			sensors.add(sensor);
		}
		setConnectionCountInternal();
		setThreadCountInternal();
	}

	private void setThreadCountInternal() {
		sensorThreadPool = new ScheduledThreadPoolExecutor(Math.max(1, threadCount), this);
		sensorThreadPool.setMaximumPoolSize(threadCount);
	}

	private void setConnectionCountInternal() {
		if (connectionManager != null) {
			connectionManager.shutdown();
		}
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(connectionCount);
		connectionManager.setMaxTotal(connectionCount);
		client = HttpClients.custom().setConnectionManager(connectionManager).build();
		for (Sensor sensor : sensors) {
			sensor.setClient(client);
		}
	}

	public SensorManager(int multiplicity, int threadCount, int connectionCount, String displayName, C config) {
		this(SensorState.NOT_CREATED, multiplicity, threadCount, connectionCount, displayName, config, new ArrayList<Double>());
	}

	private void fireSensorStateChanged(SensorStatusChangedEvent e) {
		for (SensorStatusChangedListener hl : listeners) {
			hl.onSensorStateChangedListener(e);
		}
	}

	public void addListener(SensorStatusChangedListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		listeners.add(listener);
	}

	public void removeListener(SensorStatusChangedListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public Callable<Boolean> start() {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean result = true;
				setStatus(SensorState.STARTING);
				startFetchSensorPerformance();
				for (Sensor sensor : sensors) {
					runningSensorTasks.add(sensorThreadPool.scheduleAtFixedRate(sensor.sendData(), 0, interval, TimeUnit.MILLISECONDS));
					sensor.start();
					result = true;
				}
				setStatus(SensorState.RUNNING);
				return result;
			}
		};
	}

	public Callable<Boolean> sendDataOnce() {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean result = true;
				setStatus(SensorState.RUNNING);
				for (Sensor sensor : sensors) {
					sensor.start();
					if (!sensorThreadPool.submit(sensor.sendData(), result).get()) {
						result = false;
						setFailures(failures + 1);
					}
					sensor.stop();
				}
				setStatus(SensorState.STOPPED);
				calcSensorPerformance();
				return result;
			}
		};
	}

	public Callable<Boolean> stop() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				LOGGER.info("Starting the stop process...");
				boolean result;
				setStatus(SensorState.STOPPING);
				LOGGER.info("Stopping sensor statistics...");
				stopFetchSensorPerformance();
				LOGGER.info("Stopping the tasks...");
				for (ScheduledFuture<?> task : runningSensorTasks) {
					task.cancel(false);
				}
				LOGGER.info("Stopping the sensors...");
				for (Sensor sensor : sensors) {
					sensor.stop();
				}
				result = sensorThreadPool.getQueue().isEmpty();
				setStatus(SensorState.STOPPED);
				LOGGER.debug("Finished the stop process.");
				for (Iterator<ScheduledFuture<?>> it = runningSensorTasks.iterator(); it.hasNext();) {
					ScheduledFuture<?> task = it.next();
					if (task.isDone()) {
						it.remove();
					}
				}
				return result;
			}
		};
	}

	public Callable<Boolean> create() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				boolean result = true;
				setStatus(SensorState.CREATING);
				List<Future<Boolean>> futures = new LinkedList<>();
				for (Sensor sensor : sensors) {
					futures.add(sensorThreadPool.submit(sensor.create()));
				}
				for (Future<Boolean> future : futures) {
					if (!future.get()) {
						result = false;
					}
				}
				setStatus(result ? SensorState.STOPPED : SensorState.NOT_CREATED);
				return result;
			}
		};
	}

	public Callable<Boolean> delete() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				boolean result = true;
				setStatus(SensorState.DELETING);
				List<Future<Boolean>> futures = new LinkedList<>();
				for (Sensor sensor : sensors) {
					futures.add(sensorThreadPool.submit(sensor.delete()));
				}
				for (Future<Boolean> future : futures) {
					if (!future.get()) {
						result = false;
					}
				}
				setStatus(result ? SensorState.NOT_CREATED : SensorState.RUNNING);
				return result;
			}
		};
	}

	private void setStatus(SensorState state, String message) {
		SensorStatus newStatus = new SensorStatus();
		newStatus.setState(state);
		newStatus.setMessage(message);
		setStatus(newStatus);
	}

	private void setStatus(SensorState state) {
		setStatus(state, getStatus().getMessage());
	}

	private void setStatus(String message) {
		setStatus(getStatus().getState(), message);
	}

	/**
	 * @return the sensors
	 */
	public ObservableList<Sensor> getSensors() {
		return sensors;
	}

	/**
	 * @return the status
	 */
	public SensorStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	private void setStatus(SensorStatus status) {
		SensorStatus oldStatus = this.status;
		this.status = status;
		if (oldStatus != status) {
			propertyChangeSupport.firePropertyChange("status", oldStatus, status);
			fireSensorStateChanged(new SensorStatusChangedEvent(this, oldStatus, status));
		}
	}

	/**
	 * @return the lastData
	 */
	public String getLastData() {
		return lastData;
	}

	/**
	 * @param lastData the lastData to set
	 */
	private void setLastData(String lastData) {
		java.lang.String oldLastData = this.lastData;
		this.lastData = lastData;
		propertyChangeSupport.firePropertyChange("lastData", oldLastData, lastData);
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * @return the multiplicity
	 */
	public int getMultiplicity() {
		return multiplicity;
	}

	/**
	 * @return the threadCount
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * @return the connectionCount
	 */
	public int getConnectionCount() {
		return connectionCount;
	}

	public String getGsnAddress() {
		return config.getGsnAddress();
	}

	public double getAverageExecutionTime() {
		return averageExecutionTime;
	}

	public double getAverageDuration() {
		return averageDuration;
	}

	/**
	 * @return the config
	 */
	public C getConfig() {
		return config;
	}

	public List<Double> getIds() {
		List<Double> list = new ArrayList<>(sensors.size());
		for (Sensor s : sensors) {
			list.add(s.getConfig().getId());
		}
		return list;
	}

	/**
	 * @param threadCount the threadCount to set
	 */
	public void setThreadCount(int threadCount) {
		int oldThreadCount = this.threadCount;
		this.threadCount = threadCount;
		setThreadCountInternal();
		propertyChangeSupport.firePropertyChange("threadCount", oldThreadCount, threadCount);
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	private void setDisplayName(String displayName) {
		java.lang.String oldDisplayName = this.displayName;
		this.displayName = displayName;
		propertyChangeSupport.firePropertyChange("displayName", oldDisplayName, displayName);
	}

	@Override
	public synchronized void onSensorStateChangedListener(SensorStatusChangedEvent e) {
		if (e.getOldStatus().getState() != e.getNewStatus().getState()) {
			childStatus.put(e.getOldStatus().getState(), childStatus.get(e.getOldStatus().getState()) - 1);
			childStatus.put(e.getNewStatus().getState(), childStatus.get(e.getNewStatus().getState()) + 1);
		}
		StringBuilder result = new StringBuilder();
		for (Map.Entry<SensorState, Integer> entry : childStatus.entrySet()) {
			result.append(String.format("%s: \t%d%s", entry.getKey().toString().toLowerCase().replace('_', ' '), entry.getValue(), System.lineSeparator()));
		}
		setStatus(result.toString());
		//build up new status message

	}

	private void stopFetchSensorPerformance() {
		for (Iterator<ScheduledFuture<?>> it = runningInternalTasks.iterator(); it.hasNext();) {
			Future<?> task = it.next();
			task.cancel(true);
			if (task.isDone()) {
				it.remove();
			}
		}
	}

	private void startFetchSensorPerformance() {
		runningInternalTasks.add(internalThreadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				calcSensorPerformance();
			}
		}, 1, 1, TimeUnit.SECONDS));
	}

	private void calcSensorPerformance() {
		double avgDuration = 0;
		double avgExecTime = 0;
		int failuresSum = 0;
		for (Sensor sensor : sensors) {
			avgDuration += sensor.getAverageDuration() / sensors.size();
			avgExecTime += sensor.getAverageExecutionTime() / sensors.size();
			failuresSum += sensor.getFailures();
		}
		setAverageDuration(avgDuration);
		setAverageExecutionTime(avgExecTime);
		setFailures(failuresSum);
	}

	/**
	 * @param averageExecutionTime the averageExecutionTime to set
	 */
	private void setAverageExecutionTime(double averageExecutionTime) {
		double oldAverageExecutionTime = this.averageExecutionTime;
		this.averageExecutionTime = averageExecutionTime;
		propertyChangeSupport.firePropertyChange("averageExecutionTime", oldAverageExecutionTime, averageExecutionTime);
	}

	/**
	 * @param averageDuration the averageDuration to set
	 */
	private void setAverageDuration(double averageDuration) {
		double oldAverageDuration = this.averageDuration;
		this.averageDuration = averageDuration;
		propertyChangeSupport.firePropertyChange("averageDuration", oldAverageDuration, averageDuration);
	}

	/**
	 * @param connectionCount the connectionCount to set
	 */
	public void setConnectionCount(int connectionCount) {
		int oldconnectionCount = this.connectionCount;
		this.connectionCount = connectionCount;
		setConnectionCountInternal();
		propertyChangeSupport.firePropertyChange("connectionCount", oldconnectionCount, connectionCount);
	}

	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		int oldFailures = this.failures;
		this.failures = failures;
		propertyChangeSupport.firePropertyChange("failures", oldFailures, failures);
	}

	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

}
