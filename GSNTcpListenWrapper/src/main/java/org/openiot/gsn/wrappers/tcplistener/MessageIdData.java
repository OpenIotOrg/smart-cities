package org.openiot.gsn.wrappers.tcplistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.usoog.commons.network.message.Message;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message used to transmit (json) dataJson.
 *
 * @author scf
 */
public class MessageIdData implements Message {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdData.class);
	private static final Type mapType = new TypeToken<Map<String, Object>>() {
	}.getType();
	private static Gson gson;
	public static final String KEY = "ID";
	private String dataJson;
	private long senderId;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 3);
		long id;
		if (parts.length != 3) {
			LOGGER.error("Incorrect part count: {}.", parts.length);
			throw new IllegalArgumentException("Incorrect part count.");
		}
		try {
			id = Long.parseLong(parts[1]);
		} catch (NumberFormatException e) {
			LOGGER.error("ID must be double >= 0. Current value: {}.", parts[1]);
			throw new IllegalArgumentException("Not a numeric id.", e);
		}
		setSenderIdLong(id);
		dataJson = URLDecoder.decode(parts[2], "UTF-8");
	}

	@Override
	public int getSenderId() {
		if (senderId > Integer.MAX_VALUE || senderId < Integer.MIN_VALUE) {
			throw new IllegalStateException("The sender id does not fit in an integer. Use getSenderIdLong() instead.");
		}
		return (int) senderId;
	}

	public long getSenderIdLong() {
		return senderId;
	}

	@Override
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public void setSenderIdLong(long senderId) {
		this.senderId = senderId;
	}

	@Override
	public String getMessage() {
		try {
			return formatMessage(dataJson, getSenderIdLong());
		} catch (UnsupportedEncodingException ex) {
			//never happens with UTF-8
			throw new RuntimeException(ex);
		}
	}

	/**
	 * This will format a message String using the given dataJson.
	 *
	 * @param data The text to be transmitted.
	 * @param id Id of the message
	 * @return The message to be sent.
	 * @throws UnsupportedEncodingException If UTF-8 is not supported.
	 */
	public static String formatMessage(String data, long id) throws UnsupportedEncodingException {
		if (data == null) {
			data = "";
		}
		return KEY + " " + Long.toString(id) + " " + URLEncoder.encode(data, "UTF-8");
	}

	public String getDataJson() {
		return dataJson;
	}

	public void setData(String data) {
		this.dataJson = data;
	}

	/**
	 * return the data in the message as a Map.
	 *
	 * @return the data in the message as a Map.
	 */
	public Map<String, Object> getDataMap() {
		Gson g = getGson();
		return g.fromJson(dataJson, mapType);
	}

	/**
	 * Set the data in the message using a Map.
	 *
	 * @param data the data in the message.
	 */
	public void setData(Map<String, Object> data) {
		Gson g = getGson();
		dataJson = g.toJson(data);
	}

	private static Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder().create();
		}
		return gson;
	}

}
