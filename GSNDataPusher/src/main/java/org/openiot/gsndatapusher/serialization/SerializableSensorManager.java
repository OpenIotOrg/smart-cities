package org.openiot.gsndatapusher.serialization;

import java.io.Serializable;
import org.openiot.gsndatapusher.core.ISensorAdapter;
import org.openiot.gsndatapusher.core.ISensorConfig;
import org.openiot.gsndatapusher.core.SensorManager;
import org.openiot.gsndatapusher.core.SensorState;

/**
 *
 * @author admin-jacoby
 */
public class SerializableSensorManager<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> implements Serializable {

	private SensorState state;
	private int multiplicity;
	private int threadCount;
	private int connectionCount;
	private String displayName;
	private C config;

	public SerializableSensorManager() {

	}

	public SerializableSensorManager(SensorManager<A, C> manager) {
		this.state = manager.getStatus().getState();
		this.multiplicity = manager.getMultiplicity();
		this.threadCount = manager.getThreadCount();
		this.connectionCount = manager.getConnectionCount();
		this.displayName = manager.getDisplayName();
		this.config = manager.getConfig();
	}

	public SensorManager<A, C> asManager() {
		return new SensorManager<A, C>(state, multiplicity, threadCount, connectionCount, displayName, config);
	}

	/**
	 * @return the multiplicity
	 */
	public int getMultiplicity() {
		return multiplicity;
	}

	/**
	 * @param multiplicity the multiplicity to set
	 */
	public void setMultiplicity(int multiplicity) {
		this.multiplicity = multiplicity;
	}

	/**
	 * @return the threadCount
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * @param threadCount the threadCount to set
	 */
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/**
	 * @return the connectionCount
	 */
	public int getConnectionCount() {
		return connectionCount;
	}

	/**
	 * @param connectionCount the connectionCount to set
	 */
	public void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
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
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the config
	 */
	public C getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(C config) {
		this.config = config;
	}

	/**
	 * @return the state
	 */
	public SensorState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(SensorState state) {
		this.state = state;
	}
}
