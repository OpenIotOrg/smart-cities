package org.openiot.gsndatapusher.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public abstract class AbstractSensorConfig<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> implements ISensorConfig<A, C> {

	private String gsnAddress;
	private String name;
	private String type;
	private long interval;
	private int fieldCount;
	private FieldType fieldType;
	private int poolSize;
	private String historySize;
	private int samplingRate;
	private int storageSize;
	private int priority;
	private boolean publishToLSM;

	public AbstractSensorConfig() {
	}

	/**
	 * @return the gsnAddress
	 */
	@Override
	public String getGsnAddress() {
		return gsnAddress;
	}

	/**
	 * @param gsnAddress the gsnAddress to set
	 */
	@Override
	public void setGsnAddress(String gsnAddress) {
		this.gsnAddress = gsnAddress;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	@Override
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the fieldCount
	 */
	@Override
	public int getFieldCount() {
		return fieldCount;
	}

	/**
	 * @param fieldCount the fieldCount to set
	 */
	@Override
	public void setFieldCount(int fieldCount) {
		this.fieldCount = fieldCount;
	}

	/**
	 * @return the fieldType
	 */
	@Override
	public FieldType getFieldType() {
		return fieldType;
	}

	/**
	 * @param fieldType the fieldType to set
	 */
	@Override
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * @return the poolSize
	 */
	@Override
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	@Override
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @return the historySize
	 */
	@Override
	public String getHistorySize() {
		return historySize;
	}

	/**
	 * @param historySize the historySize to set
	 */
	@Override
	public void setHistorySize(String historySize) {
		this.historySize = historySize;
	}

	/**
	 * @return the samplingRate
	 */
	@Override
	public int getSamplingRate() {
		return samplingRate;
	}

	/**
	 * @param samplingRate the samplingRate to set
	 */
	@Override
	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}

	/**
	 * @return the storageSize
	 */
	@Override
	public int getStorageSize() {
		return storageSize;
	}

	/**
	 * @param storageSize the storageSize to set
	 */
	@Override
	public void setStorageSize(int storageSize) {
		this.storageSize = storageSize;
	}

	/**
	 * @return the priority
	 */
	@Override
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the publishToLSM
	 */
	@Override
	public boolean isPublishToLSM() {
		return publishToLSM;
	}

	/**
	 * @param publishToLSM the publishToLSM to set
	 */
	@Override
	public void setPublishToLSM(boolean publishToLSM) {
		this.publishToLSM = publishToLSM;
	}

	@Override
	public List<C> createAdaptedCopies(int n) {
		List<C> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			C copy = createAdaptedCopy(i);
			copy.setName(getName() + i);
			result.add(copy);
		}
		return result;
	}

	protected abstract C createAdaptedCopy(int offset);

}
