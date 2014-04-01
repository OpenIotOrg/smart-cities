/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.core;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author admin-jacoby
 */
public interface ISensorConfig<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A,C>> extends Serializable {

    List<C> createAdaptedCopies(int n);    
    
    A getAdapter();
    
    String getGsnAddress();
    void setGsnAddress(String gsnAddress);

    String getName();
    void setName(String name);

    long getInterval();
    void setInterval(long interval);

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
