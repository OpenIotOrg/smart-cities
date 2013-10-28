package edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces;

import android.content.BroadcastReceiver;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.SensorDataRetrievalService;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.StatusReceiver;

/**
 * This interface defines all methods an access point to the data retrieval of
 * the internal environment sensors of the phone.
 * 
 * @author David Kulicke
 * 
 */
public interface IInternalSensorConnector {

	/**
	 * Registers a new {@link BroadcastReceiver} which will receive status
	 * messages sent by the sensor data retrieval service The convenience class
	 * {@link StatusReceiver} can be used as well as the more general type
	 * {@link BroadcastReceiver}.
	 * 
	 * @param receiver
	 */
	public void registerSensingStatusReceiver(BroadcastReceiver receiver);

	/**
	 * Starts the sensor data retrieval. If an instance of the service is
	 * already running, it will be stopped before then.
	 * 
	 * @param refreshRate
	 *            the refresh rate of the data retrieval in milliseconds
	 * @param roomURI the URI of the room where the measuring takes place
	 */
	public void startSensing(int refreshRate, String roomURI);

	/**
	 * Stops the sensing process of the {@link SensorDataRetrievalService}.
	 */
	public void stopSensing();

}
