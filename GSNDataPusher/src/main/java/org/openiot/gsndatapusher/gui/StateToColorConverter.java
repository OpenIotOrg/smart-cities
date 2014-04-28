package org.openiot.gsndatapusher.gui;

/**
 *
 * @author admin-jacoby
 */
import java.awt.Color;
import org.jdesktop.beansbinding.Converter;
import org.openiot.gsndatapusher.core.SensorState;

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
