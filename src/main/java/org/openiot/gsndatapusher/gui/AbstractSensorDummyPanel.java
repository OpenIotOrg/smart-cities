package org.openiot.gsndatapusher.gui;

import javax.swing.JPanel;
import org.openiot.gsndatapusher.core.SensorManager;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author admin-jacoby
 */
public abstract class AbstractSensorDummyPanel extends JPanel {
    public abstract SensorManager getSensorManager(int mulitplicity, String gsnAddress);
    public abstract String getDisplayName();
}
