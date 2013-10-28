package edu.kit.cm.ivu.smartmeetings.logic.phonesensors;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISensorDataPublisher;

/**
 * This class implements a background service which periodically retrieves the
 * data of the available environment sensors of the phone and sends it to an
 * external entity.
 * 
 * @author David Kulicke
 * 
 */
public class SensorDataRetrievalService extends IntentService {

	/** status codes used to publish the service's current status */
	public static final int STARTED = 0;
	public static final int TERMINATED = 1;
	public static final int PUBLISHING_FAILED = 2;
	public static final int NO_SENSORS = 3;

	/** All status broadcast intents sent from this service use this action */
	public static final String STATUS_CHANGED = "edu.kit.ivu.smartmeetings.logic.phonesensors.SensorDataRetrievalService.STATUS_CHANGED";

	private int refreshRate;

	private Sensor temperatureSensor;
	private Sensor humiditySensor;
	private Sensor pressureSensor;

	private int temperature;
	private int pressure;
	private int humidity;

	private boolean tempAvailable;
	private boolean pressureAvailable;
	private boolean humidityAvailable;

	private boolean tempSet;
	private boolean pressureSet;
	private boolean humiditySet;

	private SensorEventListener sensorListener;

	private ISensorDataPublisher publisher;

	/** indicates whether the sensor retrieval process is still running */
	private static boolean isRunning;

	/**
	 * Constructs a new {@link SensorDataRetrievalService}
	 */
	public SensorDataRetrievalService() {
		super("SensorDataRetrievalService");
	}

	/**
	 * Starts the sensing process using the given parameters
	 * 
	 * @param intent
	 *            must contain
	 *            <ul>
	 *            <li>Parcelable extra "publisher": a SensorDataPublisher which
	 *            should be used for the data publishment</li>
	 *            <li>int extra "refreshRate": Refresh rate in milliseconds</li>
	 *            </ul>
	 */
	@Override
	protected void onHandleIntent(final Intent intent) {

		if (!discoverAndRegisterSensors()) {
			publishStatus(NO_SENSORS);
			stopSelf();
			return;
		}
		publisher = intent.getParcelableExtra("publisher");
		refreshRate = intent.getIntExtra("refreshRate", 10000);
		isRunning = true;
		publishStatus(STARTED);
		Log.d("SensorService", "Started with refresh rate " + refreshRate);

		// Main loop for sensor data retrieval
		while (isRunning) {
			// retrieve and send a new data package
			final Bundle sensorData = new Bundle();

			if (tempAvailable && tempSet) {
				sensorData
						.putInt(ISensorDataPublisher.TEMPERATURE, temperature);
			}
			if (pressureAvailable && pressureSet) {
				sensorData.putInt(ISensorDataPublisher.PRESSURE, pressure);
			}
			if (humidityAvailable && humiditySet) {
				sensorData.putInt(ISensorDataPublisher.HUMIDITY, humidity);
			}

			final boolean success = publisher.publishSensorData(sensorData);
			Log.d("SensorService", "Published new data");
			if (!success) {
				publishStatus(PUBLISHING_FAILED);
			}

			// wait until next sensing event
			try {
				Thread.sleep(refreshRate);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		publishStatus(TERMINATED);

	}

	@Override
	public void onDestroy() {
		isRunning = false;
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(sensorListener);
		Log.d("SensorService", "Stopped");
	}

	/**
	 * Discovers the available environment sensors in the phone and registers
	 * the sensorListener for each available one.
	 * 
	 * @return true if at least one sensor was found, false if no environment
	 *         sensors exist
	 */
	private boolean discoverAndRegisterSensors() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		temperatureSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		humiditySensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		pressureAvailable = (pressureSensor != null);
		tempAvailable = (temperatureSensor != null);
		humidityAvailable = (humiditySensor != null);
		tempSet = false;
		humiditySet = false;
		pressureSet = false;

		// at least one of the three sensors must exist to continue
		if (!(humidityAvailable || tempAvailable || pressureAvailable)) {
			return false;
		}
		sensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(final SensorEvent event) {
				final Sensor sensor = event.sensor;
				if (sensor.equals(temperatureSensor)) {
					temperature = Math.round(event.values[0]);
					tempSet = true;
				} else if (sensor.equals(pressureSensor)) {
					pressure = Math.round(event.values[0]);
					pressureSet = true;
				} else if (sensor.equals(humiditySensor)) {
					humidity = Math.round(event.values[0]);
					humiditySet = true;
				}
			}

			@Override
			public void onAccuracyChanged(final Sensor sensor,
					final int accuracy) {
			}
		};
		if (humidityAvailable) {
			sensorManager.registerListener(sensorListener, humiditySensor,
					refreshRate * 1000);
			Log.d("Registered", "Humidity Sensor");
		}
		if (tempAvailable) {
			sensorManager.registerListener(sensorListener, temperatureSensor,
					refreshRate * 1000);
			Log.d("Registered", "Temperature Sensor");
		}
		if (pressureAvailable) {
			sensorManager.registerListener(sensorListener, pressureSensor,
					refreshRate * 1000);
			Log.d("Registered", "Pressure Sensor");
		}
		return true;
	}

	/**
	 * Publishes a status message via broadcast
	 * 
	 * @param status
	 *            the status code
	 */
	private void publishStatus(final int status) {
		final Intent statusIntent = new Intent(STATUS_CHANGED);
		statusIntent.putExtra("status", status);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(statusIntent);
	}

}
