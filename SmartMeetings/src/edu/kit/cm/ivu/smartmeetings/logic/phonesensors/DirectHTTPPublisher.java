package edu.kit.cm.ivu.smartmeetings.logic.phonesensors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISensorDataPublisher;

/**
 * A {@link SensoDataPublisher} which sends its received sensor data directly to a HTTP server
 * @author David Kulicke
 *
 */
public class DirectHTTPPublisher implements ISensorDataPublisher {


	private String serverAddress;

	/** Creator for a {@link DirectHTTPPublisher} */
	// needed for parcelability
	public static final Parcelable.Creator<DirectHTTPPublisher> CREATOR = new Parcelable.Creator<DirectHTTPPublisher>() {

		@Override
		public DirectHTTPPublisher createFromParcel(Parcel source) {
			return new DirectHTTPPublisher(source.readString());
		}

		@Override
		public DirectHTTPPublisher[] newArray(int size) {
			return new DirectHTTPPublisher[size];
		}
	};
	
	/** timeout values for the HTTP POSTs */
	private static final int CONNECTION_TIMEOUT = 3000;
	private static final int SOCKET_TIMEOUT = 5000;

	
	/**
	 * Constructs a new {@link DirectHTTPPublisher} which publishes its received sensor data to the given server address
	 * @param serverAddress the target address for the data
	 */
	public DirectHTTPPublisher(String serverAddress) {
		boolean addressValid = false;
		// URI is used to test if the address string is formed correctly
		try {
			URI testURI = new URI(serverAddress);
			addressValid = (testURI.getHost() != null);
		} catch (URISyntaxException e) {
			Log.e("URISyntaxException", e.getMessage() + "");
		}
		if (!addressValid) {
			throw new IllegalArgumentException("Invaild server address!");
		} else {
			this.serverAddress = serverAddress;
		}
	}

	
	@Override
	public boolean publishSensorData(Bundle sensorData) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
		HttpClient client = new DefaultHttpClient(params);
		
		//data is send via a HTTP POST message
		HttpPost post = new HttpPost(serverAddress);
		post.setHeader("Connection", "close");

		if (sensorData.containsKey(TEMPERATURE)) {
			post.addHeader(TEMPERATURE, String.valueOf(sensorData.getInt(TEMPERATURE)));
		}
		if (sensorData.containsKey(PRESSURE)) {
			post.addHeader(PRESSURE, String.valueOf(sensorData.getInt(PRESSURE)));
		}
		if (sensorData.containsKey(HUMIDITY)) {
			post.addHeader(HUMIDITY, String.valueOf(sensorData.getInt(HUMIDITY)));
		}

		boolean success = false;
		try {
			client.execute(post);
			success = true;
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage() + "");
		} catch (IOException e) {
			Log.e("IOException", e.getMessage() + "");
		}
		return success;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(serverAddress);
	}

}
