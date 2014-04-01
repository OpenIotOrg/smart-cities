/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openiot.gsndatapusher.gui;

import java.util.EventListener;
import org.openiot.gsndatapusher.core.SensorManager;

/**
 *
 * @author admin-jacoby
 */
public interface SensorRemoveListener extends EventListener{
    void onSensorRemove(SensorManager manager, SensorManagerPanel panel);
}
