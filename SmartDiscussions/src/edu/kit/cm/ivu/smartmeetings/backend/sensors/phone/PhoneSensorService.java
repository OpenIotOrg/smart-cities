package edu.kit.cm.ivu.smartmeetings.backend.sensors.phone;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import edu.kit.cm.ivu.smartdiscussions.Ids;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.ReturnStatus;

/**
 * Class which receives sensor data and forwards it to a GSN node.
 * 
 * @author Andreas Bender
 * @author David Kulicke
 */
@Api(name = "smartmeetings", version = "v1", clientIds = {
		Ids.ANDROID_CLIENT_ID1, Ids.ANDROID_CLIENT_ID2, Ids.ANDROID_CLIENT_ID3,
		Ids.ANDROID_CLIENT_ID4, Ids.ANDROID_CLIENT_ID5, Ids.ANDROID_CLIENT_ID6,
		Ids.WEB_CLIENT_ID,
		com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID }, audiences = { Ids.ANDROID_AUDIENCE })
public class PhoneSensorService {
	
	/** The logger used by all SmartDiscussions classes */
	private static final Logger LOG = Logger.getLogger("SmartDiscussions");
	
	private static final String DEFAULT_SERVER_ADDRESS = "http://78.42.25.94/GSNWrapper";
	private URL serverURL;
	private final PhoneSensorPublisher sensorPublisher;
	
	public PhoneSensorService() {
		try {
			serverURL = new URL(DEFAULT_SERVER_ADDRESS);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Invalid server address.", e);
		}
		this.sensorPublisher = new PhoneSensorPublisher(serverURL);
	}

	@ApiMethod(name = "phonesensors.processSensorData")
	public void processSensorData(final SensorData sensorData, final User user)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}
		sensorPublisher.publishSensorData(sensorData);
	}
	
	@ApiMethod(name = "phonesensors.setGSNServerAddress") 
	public ReturnStatus setGSNServerAddress(@Named("address") final String address, final User user) 
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		} else {
			return new ReturnStatus(setAddress(address));
		}
	}
	
	private boolean setAddress(final String address) {
		try {
			serverURL = new URL(address);
			sensorPublisher.setServerURL(serverURL);
			LOG.info("Address is set to " + address);
			return true;
		} catch (final MalformedURLException e) {
			LOG.severe("Invalid server address: " + address);
			return false;
		}
	}
}
