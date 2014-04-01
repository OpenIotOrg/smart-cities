/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openiot.gsndatapusher.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;


/**
 *
 * @author admin-jacoby
 */
public class SensorStatus implements Serializable {
    private SensorState state = SensorState.NOT_CREATED;
    private String message = "object created";
    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    public SensorStatus() {
        
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
        org.openiot.gsndatapusher.core.SensorState oldState = this.state;
        this.state = state;
        propertyChangeSupport.firePropertyChange("state", oldState, state);
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        java.lang.String oldMessage = this.message;
        this.message = message;
        propertyChangeSupport.firePropertyChange("message", oldMessage, message);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
