package org.openiot.gsndatapusher.singletontcplistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.openiot.gsndatapusher.core.AbstractSensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin-jacoby
 */
public class SingletonTcpListenerConfig extends AbstractSensorConfig<SingletonTcpListenerAdapter, SingletonTcpListenerConfig> {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonTcpListenerConfig.class);
	private static final Random RANDOM = new Random();
	private int id;
	private int port;
	private String server;
	private String badValues;
	private String timeZone;
	private final Map<String, String> setPoints = new HashMap<>();
	private final Map<String, Integer> lastValue = new HashMap<>();

	public SingletonTcpListenerConfig() {
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the badValues
	 */
	public String getBadValues() {
		return badValues;
	}

	/**
	 * @param badValues the badValues to set
	 */
	public void setBadValues(String badValues) {
		this.badValues = badValues;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 *
	 * @param n
	 * @return
	 */
	@Override
	public List<SingletonTcpListenerConfig> createAdaptedCopies(int n, List<Integer> ids) {
		List<SingletonTcpListenerConfig> result = new ArrayList<>(n);
		Iterator<Integer> iIds = ids.iterator();
		for (int i = 1; i <= n; i++) {
			Integer cid = null;
			if (iIds.hasNext()) {
				cid = iIds.next();
			}
			result.add(createAdaptedCopy(i, cid));
		}
		return result;
	}

	/**
	 * @return the id
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	protected SingletonTcpListenerConfig createAdaptedCopy(int offset, Integer id) {
		SingletonTcpListenerConfig result = new SingletonTcpListenerConfig();
		result.setFieldCount(getFieldCount());
		result.setFieldType(getFieldType());
		result.setGsnAddress(getGsnAddress());
		result.setHistorySize(getHistorySize());
		result.setPoolSize(getPoolSize());
		result.setPriority(getPriority());
		result.setPublishToLSM(isPublishToLSM());
		result.setSamplingRate(getSamplingRate());
		result.setStorageSize(getStorageSize());
		result.setBadValues(getBadValues());
		result.setPort(getPort());
		result.setServer(getServer());
		result.setTimeZone(getTimeZone());
		result.setName(getName() + offset);
		result.setType(getType());
		if (id != null) {
			result.setId(id);
		} else {
			result.setId(RANDOM.nextInt());
		}
		return result;
	}

	@Override
	public SingletonTcpListenerAdapter getAdapter() {
		return new SingletonTcpListenerAdapter();
	}

	public int getIntSetpointFor(String name, int dflt) {
		String sp = setPoints.get(name);
		if (sp == null) {
			return dflt;
		}
		try {
			return Integer.parseInt(sp);
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	public void setSetpointFor(String name, String value) {
		setPoints.put(name, value);
		LOGGER.debug("Setting {} to {}.", name, value);
	}

	public void setSetpointFor(String name, int value) {
		setPoints.put(name, Integer.toString(value));
	}

	public int getLastIntValueFor(String name, int dflt) {
		Integer sp = lastValue.get(name);
		if (sp == null) {
			return dflt;
		}
		return sp;
	}

	public void setLastValueFor(String name, int value) {
		lastValue.put(name, value);
	}
}
