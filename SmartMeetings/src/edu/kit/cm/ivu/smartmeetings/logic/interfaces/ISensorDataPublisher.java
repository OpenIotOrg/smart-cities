package edu.kit.cm.ivu.smartmeetings.logic.interfaces;

import android.os.Bundle;
import android.os.Parcelable;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.SensorDataRetrievalService;

/**
 * Interface for classes that can be used as sensor data publishers by the
 * {@link SensorDataRetrievalService}.
 * 
 * @author David Kulicke
 * 
 */
public interface ISensorDataPublisher extends Parcelable {

	/** Header name for the humidity value */
	public static final String HUMIDITY = "humidity";

	/** Header name for the temperature value */
	public static final String TEMPERATURE = "temperature";

	/** Header name for the pressure value */
	public static final String PRESSURE = "pressure";

	/** Header name for the room uri */
	public static final String ROOM_URI = "room_uri";

	/**
	 * Publishes the sensor data passed by the given {@link Bundle}
	 * 
	 * @param sensorData
	 *            the sensor data which will be published
	 * @return true if the publishing process was successful, false otherwise
	 */
	public boolean publishSensorData(Bundle sensorData);

}
