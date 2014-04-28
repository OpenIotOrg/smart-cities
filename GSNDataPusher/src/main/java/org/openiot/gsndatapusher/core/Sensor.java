package org.openiot.gsndatapusher.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public class Sensor<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> {

	/**
	 * The logger for this class.
	 */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Sensor.class);

	private List<SensorStatusChangedListener> listeners = new ArrayList<>();
	private CloseableHttpClient client;
	private A adapter;
	private C config;
	private SensorStatus status = new SensorStatus();
	private String lastData;
	private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

	private double averageExecutionTime = 0;
	private double averageDuration = 0;
	private long lastDataSend = 0;

	public Sensor(SensorState initialState, C config, CloseableHttpClient client) {
		adapter = config.getAdapter();
		setStatus(initialState);
		this.config = config;
		this.client = client;
	}

	public Sensor(C config, CloseableHttpClient client) {
		this(SensorState.NOT_CREATED, config, client);
	}

	public Callable<Boolean> create() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return createSensor();
			}
		};
	}

	public Callable<Boolean> delete() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return deleteSensor();
			}
		};
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
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

	public boolean createSensor() {
		boolean result = false;
		setStatus(SensorState.CREATING, "creating sensor");
		CloseableHttpResponse response = null;
		HttpPost request = null;
		String response1 = "";
		String response2 = "";
		try {
			String url = config.getGsnAddress() + "/vs/vsensor/" + URLEncoder.encode(config.getName(), "UTF-8") + "/create";
			LOGGER.info("Posting sensor to {}.", url);
			request = new HttpPost(url);
			StringEntity input = new StringEntity(getAdapter().getGSNConfigFile(config));
			input.setContentType("text/xml");
			request.setEntity(input);
			response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			result = statusLine.getStatusCode() == 200;
			response1 = EntityUtils.toString(response.getEntity());
			response.getEntity().getContent().close();
			response.close();
			if (!result) {
				LOGGER.info("Failed to create sensor: {}, {}.", statusLine.getStatusCode(), statusLine.getReasonPhrase());
			} else {
				if (config.isPublishToLSM()) {
					url = config.getGsnAddress() + "/vs/vsensor/" + URLEncoder.encode(config.getName(), "UTF-8") + "/register";
					LOGGER.info("Posting sensor metadata to {}.", url);
					request = new HttpPost(url);
					String configString = getAdapter().getGSNMetadataFile(config);
					input = new StringEntity(configString);
					input.setContentType("text/xml");
					request.setEntity(input);
					response = client.execute(request);
					statusLine = response.getStatusLine();
					boolean result2 = statusLine.getStatusCode() == 200;
					if (!result2) {
						LOGGER.info("Failed to upload sensor metadata: {}, {}.", statusLine.getStatusCode(), statusLine.getReasonPhrase());
					}
					response2 = EntityUtils.toString(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
					result = result && result2;
				}
			}

			setStatus(result ? SensorState.STOPPED : SensorState.NOT_CREATED, response1 + response2);
		} catch (UnsupportedEncodingException ex) {
			setStatus(SensorState.NOT_CREATED, "Can not encode sensor name.");
			LOGGER.error("Failed to create sensor.", ex);
		} catch (HttpHostConnectException ex) {
			setStatus(SensorState.NOT_CREATED, "HTTP Exception");
			LOGGER.error("Failed to create sensor.", ex);
		} catch (IOException ex) {
			setStatus(SensorState.NOT_CREATED, "IO Exception (" + ex.getMessage() + ")");
			LOGGER.error("Failed to create sensor.", ex);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException ex) {
					LOGGER.error("Error creating sensor.", ex);
				}
			}
			if (request != null) {
				request.releaseConnection();
			}
		}
		return result;
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

	public boolean deleteSensor() {
		boolean result = false;
		HttpPost request = null;
		CloseableHttpResponse response = null;
		try {
			String url = config.getGsnAddress() + "/vs/vsensor/" + URLEncoder.encode(config.getName(), "UTF-8") + "/delete";
			request = new HttpPost(url);
			setStatus(SensorState.DELETING, "deleting sensor");
			StringEntity input = new StringEntity("deleteFromLSM=true");
			input.setContentType("text/xml");
			request.setEntity(input);
			response = client.execute(request);
			result = response.getStatusLine().getStatusCode() == 200;
			setStatus(result ? SensorState.NOT_CREATED : SensorState.STOPPED, EntityUtils.toString(response.getEntity()));
		} catch (UnsupportedEncodingException ex) {
			setStatus(SensorState.STOPPED, "Can not encode sensor name.");
			LOGGER.error("Failed to create sensor.", ex);
		} catch (IOException ex) {
			LOGGER.error("Failed to delete sensor.", ex);
			setStatus(SensorState.STOPPED, "IO Exception (" + ex.getMessage() + ")");
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException ex) {
					LOGGER.error("Exception while deleting sensor.", ex);
				}
			}
			if (request != null) {
				request.releaseConnection();
			}
		}
		return result;
	}

	public Runnable sendData() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					setAverageExecutionTime(lastDataSend > 0 ? System.currentTimeMillis() - lastDataSend : 0);
					lastDataSend = System.currentTimeMillis();
					setStatus(SensorState.RUNNING, "connecting to server");
					if (getAdapter().setupSensorConnection(config).call()) {

						Callable<ISensorAdapter.SendResult> call = getAdapter().sendData(config);
						ISensorAdapter.SendResult result = call.call();
						if (result.success) {
							setStatus(SensorState.RUNNING, "data sent");
							setLastData(result.data);
							LOGGER.trace("data send: {}", result.data);
						} else {
							setStatus(SensorState.RUNNING, "data could not be sent");
						}
						if (!getAdapter().teardownSensorConnection(config).call()) {
							setStatus(SensorState.RUNNING, "connection could not be closed");
							return;
						} else {
							setStatus(SensorState.RUNNING, "connection closed");
						}
					} else {
						setStatus(SensorState.STOPPED, "could not connect to server");
					}
					setAverageDuration(System.currentTimeMillis() - lastDataSend);
				} catch (Exception ex) {
					LOGGER.error("Exception while sending date.", ex);
				}
			}
		};
	}

	public void start() {
		lastDataSend = 0;
		setStatus(SensorState.RUNNING);
	}

	public void stop() {
		setStatus(SensorState.STOPPED);
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
		propertyChangeSupport.firePropertyChange("status", oldStatus, status);
		fireSensorStateChanged(new SensorStatusChangedEvent(this, oldStatus, status));
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
	public void setLastData(String lastData) {
		java.lang.String oldLastData = this.lastData;
		this.lastData = lastData;
		propertyChangeSupport.firePropertyChange("lastData", oldLastData, lastData);
	}

	/**
	 * @return the adapter
	 */
	public A getAdapter() {
		return adapter;
	}

	public C getConfig() {
		return config;
	}

	public double getAverageExecutionTime() {
		return averageExecutionTime;
	}

	public double getAverageDuration() {
		return averageDuration;
	}

	/**
	 * @param averageExecutionTime the averageExecutionTime to set
	 */
	public void setAverageExecutionTime(double averageExecutionTime) {
		double oldAverageExecutionTime = this.averageExecutionTime;
		this.averageExecutionTime = averageExecutionTime;
		propertyChangeSupport.firePropertyChange("averageExecutionTime", oldAverageExecutionTime, averageExecutionTime);
	}

	/**
	 * @param averageDuration the averageDuration to set
	 */
	public void setAverageDuration(double averageDuration) {
		double oldAverageDuration = this.averageDuration;
		this.averageDuration = averageDuration;
		propertyChangeSupport.firePropertyChange("averageDuration", oldAverageDuration, averageDuration);
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(CloseableHttpClient client) {
		this.client = client;
	}

}
