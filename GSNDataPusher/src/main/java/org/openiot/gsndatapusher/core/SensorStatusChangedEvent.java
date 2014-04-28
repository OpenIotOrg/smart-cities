package org.openiot.gsndatapusher.core;

import java.util.EventObject;

/**
 *
 * @author admin-jacoby
 */
public class SensorStatusChangedEvent extends EventObject {

	private SensorStatus oldStatus;
	private SensorStatus newStatus;

	public SensorStatusChangedEvent(Object source, SensorStatus oldStatus, SensorStatus newStatus) {
		super(source);
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	/**
	 * @return the oldStatus
	 */
	public SensorStatus getOldStatus() {
		return oldStatus;
	}

	/**
	 * @param oldStatus the oldStatus to set
	 */
	public void setOldStatus(SensorStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	/**
	 * @return the newStatus
	 */
	public SensorStatus getNewStatus() {
		return newStatus;
	}

	/**
	 * @param newStatus the newStatus to set
	 */
	public void setNewStatus(SensorStatus newStatus) {
		this.newStatus = newStatus;
	}

}
