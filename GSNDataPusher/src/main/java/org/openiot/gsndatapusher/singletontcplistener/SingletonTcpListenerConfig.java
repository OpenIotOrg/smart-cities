package org.openiot.gsndatapusher.singletontcplistener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openiot.gsndatapusher.core.AbstractSensorConfig;

/**
 *
 * @author admin-jacoby
 */
public class SingletonTcpListenerConfig extends AbstractSensorConfig<SingletonTcpListenerAdapter, SingletonTcpListenerConfig> {

	private double id;
	private int port;
	private String server;
	private String badValues;
	private String timeZone;

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
	public List<SingletonTcpListenerConfig> createAdaptedCopies(int n, List<Double> ids) {
		List<SingletonTcpListenerConfig> result = new ArrayList<>(n);
		Iterator<Double> iIds = ids.iterator();
		for (int i = 1; i <= n; i++) {
			Double id = null;
			if (iIds.hasNext()) {
				id = iIds.next();
			}
			result.add(createAdaptedCopy(i, id));
		}
		return result;
	}

	/**
	 * @return the id
	 */
	@Override
	public double getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(double id) {
		this.id = id;
	}

	@Override
	protected SingletonTcpListenerConfig createAdaptedCopy(int offset, Double id) {
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
			result.setId(Math.random());
		}
		return result;
	}

	@Override
	public SingletonTcpListenerAdapter getAdapter() {
		return new SingletonTcpListenerAdapter();
	}

}
