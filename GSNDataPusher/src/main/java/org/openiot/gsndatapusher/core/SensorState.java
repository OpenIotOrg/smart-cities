package org.openiot.gsndatapusher.core;

import java.awt.Color;

/**
 *
 * @author admin-jacoby
 */
public enum SensorState {

	UNDEFINED,
	NOT_CREATED,
	CREATING,
	STARTING,
	RUNNING,
	STOPPING,
	STOPPED,
	DELETING;

	public Color toColor() {
		switch (this) {
			case CREATING:
			case DELETING:
			case STARTING:
			case STOPPING:
				return Color.YELLOW;
			case NOT_CREATED:
				return Color.BLACK;
			case RUNNING:
				return Color.GREEN;
			case STOPPED:
				return Color.RED;
			case UNDEFINED:
				return Color.MAGENTA;
		}
		return Color.MAGENTA;
	}
}
