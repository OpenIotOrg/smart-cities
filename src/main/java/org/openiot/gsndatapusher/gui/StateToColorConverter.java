/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.gui;

/**
 *
 * @author admin-jacoby
 */
import java.awt.Color;
import org.jdesktop.beansbinding.Converter;
import org.openiot.gsndatapusher.core.SensorState;
import static org.openiot.gsndatapusher.core.SensorState.CREATING;
import static org.openiot.gsndatapusher.core.SensorState.DELETING;
import static org.openiot.gsndatapusher.core.SensorState.NOT_CREATED;
import static org.openiot.gsndatapusher.core.SensorState.RUNNING;
import static org.openiot.gsndatapusher.core.SensorState.STARTING;
import static org.openiot.gsndatapusher.core.SensorState.STOPPED;
import static org.openiot.gsndatapusher.core.SensorState.STOPPING;
import static org.openiot.gsndatapusher.core.SensorState.UNDEFINED;

public class StateToColorConverter extends Converter<SensorState, Color> {

    @Override
    public Color convertForward(SensorState value) {        
        return value.toColor();
    }

    @Override
    public SensorState convertReverse(Color value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
