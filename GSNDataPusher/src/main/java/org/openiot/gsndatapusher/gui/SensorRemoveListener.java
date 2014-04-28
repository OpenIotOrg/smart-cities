package org.openiot.gsndatapusher.gui;

import java.util.EventListener;
import org.openiot.gsndatapusher.core.SensorManager;

/**
 *
 * @author admin-jacoby
 */
public interface SensorRemoveListener extends EventListener {

	void onSensorRemove(SensorManager manager, SensorManagerPanel panel);
}
