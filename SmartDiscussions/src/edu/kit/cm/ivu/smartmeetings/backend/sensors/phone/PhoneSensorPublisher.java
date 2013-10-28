package edu.kit.cm.ivu.smartmeetings.backend.sensors.phone;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publishes phone sensor data to a sensor server.
 * 
 * @author Andreas Bender
 */
public class PhoneSensorPublisher {
	
	private static final Logger LOG = Logger
			.getLogger("SmartDiscussions");
	
	private enum GSNHeaders {
		TEMPERATURE("temperature"), PRESSURE("pressure"), HUMIDITY(
				"humidity"), ROOM_URI("room_uri");

		private final String value;

		private GSNHeaders(final String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	private static final int CONNECTION_TIMEOUT = 3000;
	private static final int SOCKET_TIMEOUT = 5000;

	private URL serverURL;

	public PhoneSensorPublisher(final URL serverURL) {
		this.serverURL = serverURL;
	}

	public boolean publishSensorData(SensorData sensorData) {
	
			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) serverURL.openConnection();
				connection.setRequestMethod("POST");
			} catch (final ProtocolException e) {
				LOG.log(Level.WARNING, e.getMessage(), e);
			} catch (final IOException e) {
				LOG.log(Level.WARNING, e.getMessage(), e);
			}
			
			if (connection != null) {
				connection.setRequestProperty("Connection", "close");
				
				final Integer temperature = sensorData.getTemperature();
				final Integer pressure = sensorData.getPressure();
				final Integer humidity = sensorData.getHumidity();
				
				if (temperature != null) {
					LOG.fine("Temperature available: " + temperature);
					connection.addRequestProperty(GSNHeaders.TEMPERATURE.toString(),
							String.valueOf(temperature));
				}
				
				if (pressure != null) {
					LOG.fine("Pressure available: " + pressure);
					connection.addRequestProperty(GSNHeaders.PRESSURE.toString(), 
							String.valueOf(pressure));
				}
				if (humidity != null) {
					LOG.fine("Humidity available: " + humidity);
					connection.addRequestProperty(GSNHeaders.HUMIDITY.toString(),
							String.valueOf(humidity));
				}
				
				connection.addRequestProperty(GSNHeaders.ROOM_URI.toString(), sensorData.getRoomURI());
				
				connection.setConnectTimeout(CONNECTION_TIMEOUT);
				connection.setReadTimeout(SOCKET_TIMEOUT);
				connection.setDoOutput(true);
				connection.setDoInput(true);
				try {
					//send data
					connection.getOutputStream().close();
					connection.getInputStream().close();
				} catch (final IOException e) {
					LOG.log(Level.WARNING, e.getMessage(), e);
				}
				connection.disconnect();
				return true;
			} else {
				return false;
			}			
	}
	
	/**
	 * Sets the {@link PhoneSensorPublisher}'s URL to the given value.
	 * @param serverURL the new URL
	 */
	public void setServerURL(final URL serverURL) {
		this.serverURL = serverURL;
	}
}
