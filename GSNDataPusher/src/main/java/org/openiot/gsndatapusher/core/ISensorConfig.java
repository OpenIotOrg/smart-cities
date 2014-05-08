package org.openiot.gsndatapusher.core;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public interface ISensorConfig<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> extends Serializable {

	List<C> createAdaptedCopies(int n, List<Double> ids);

	A getAdapter();

	String getGsnAddress();

	void setGsnAddress(String gsnAddress);

	String getName();

	void setName(String name);

	double getId();

	String getType();

	void setType(String type);

	int getFieldCount();

	void setFieldCount(int fieldCount);

	FieldType getFieldType();

	void setFieldType(FieldType fieldType);

	int getPoolSize();

	void setPoolSize(int poolSize);

	String getHistorySize();

	void setHistorySize(String historySize);

	int getSamplingRate();

	void setSamplingRate(int samplingRate);

	int getStorageSize();

	void setStorageSize(int storageSize);

	int getPriority();

	void setPriority(int priority);

	boolean isPublishToLSM();

	void setPublishToLSM(boolean PublishToLSM);
}
