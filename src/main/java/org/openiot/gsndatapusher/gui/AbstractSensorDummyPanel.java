package org.openiot.gsndatapusher.gui;

import javax.swing.JPanel;
import org.openiot.gsndatapusher.core.SensorManager;

/**
 *
 * @author admin-jacoby
 */
public abstract class AbstractSensorDummyPanel extends JPanel {

	public abstract SensorManager getSensorManager(int mulitplicity, String gsnAddress);

	public abstract String getDisplayName();
}
