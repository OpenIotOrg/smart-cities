package org.openiot.gsndatapusher.gui;

import java.text.NumberFormat;
import org.jdesktop.beansbinding.Converter;

/**
 *
 * @author admin-jacoby
 */
public class DoubleToStringConverter extends Converter<Double, String> {

	private NumberFormat format;

	public DoubleToStringConverter(int fractionDigits) {
		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(fractionDigits);
		format.setMaximumFractionDigits(fractionDigits);
	}

	public DoubleToStringConverter(NumberFormat format) {
		this.format = format;
	}

	@Override
	public String convertForward(Double value) {
		return format.format(value);
	}

	@Override
	public Double convertReverse(String value) {
		return Double.parseDouble(value);
	}

}
