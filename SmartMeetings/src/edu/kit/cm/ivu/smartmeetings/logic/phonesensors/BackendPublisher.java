package edu.kit.cm.ivu.smartmeetings.logic.phonesensors;

import java.io.IOException;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.smartmeetings.Smartmeetings.Phonesensors;
import com.google.api.services.smartmeetings.model.SensorData;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISensorDataPublisher;

public class BackendPublisher implements ISensorDataPublisher {

	// phoneSensors is static because it can't be written to a parcel.
	private static Phonesensors phoneSensors;

	private final String roomURI;
	
	/** Creator for a {@link BackendPublisher} */
	// needed for parcelability
	public static final Parcelable.Creator<BackendPublisher> CREATOR = new Parcelable.Creator<BackendPublisher>() {

		@Override
		public BackendPublisher createFromParcel(final Parcel source) {
			return new BackendPublisher(source.readString());
		}

		@Override
		public BackendPublisher[] newArray(final int size) {
			return new BackendPublisher[size];
		}
	};

	/**
	 * Blank constructor that is only used by createFromParcel
	 */
	private BackendPublisher(final String roomURI) {
		this.roomURI = roomURI;
	}

	/**
	 * Constructs a new {@link BackendPublisher} which publishes its received
	 * sensor data to the SmartMeetings backend.
	 * 
	 * @param endpointConnector
	 *            the {@link IEndpointConnector} used for the backend
	 *            communication
	 * 
	 */
	public BackendPublisher(final IEndpointConnector endpointConnector, final String roomURI) {
		phoneSensors = endpointConnector.getService().phonesensors();
		this.roomURI = roomURI;
	}

	@Override
	public boolean publishSensorData(final Bundle sensorDataBundle) {
		if (phoneSensors == null) {
			throw new IllegalStateException(
					"Endpoint connector has not been initialized yet!");
		}
		final SensorData sensorData = new SensorData();

		if (sensorDataBundle.containsKey(TEMPERATURE)) {
			sensorData.setTemperature(sensorDataBundle.getInt(TEMPERATURE));
		}
		if (sensorDataBundle.containsKey(PRESSURE)) {
			sensorData.setPressure(sensorDataBundle.getInt(PRESSURE));
		}
		if (sensorDataBundle.containsKey(HUMIDITY)) {
			sensorData.setHumidity(sensorDataBundle.getInt(HUMIDITY));
		}
		sensorData.setRoomURI(roomURI);

		try {
			phoneSensors.processSensorData(sensorData).execute();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(roomURI);
	}

}
