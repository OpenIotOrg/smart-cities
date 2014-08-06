package org.openiot.gsn.wrappers.tcplistener;

import com.usoog.commons.network.message.Message;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message used to transmit (json) data.
 *
 * @author scf
 */
public class MessageIdData implements Message {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdData.class);
	public static final String KEY = "ID";
	private String data;
	private int senderId;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 3);
		int id;
		if (parts.length != 3) {
			LOGGER.error("Incorrect part count: {}.", parts.length);
			throw new IllegalArgumentException("Incorrect part count.");
		}
		try {
			id = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			LOGGER.error("ID must be double >= 0. Current value: {}.", parts[1]);
			throw new IllegalArgumentException("Not a numeric id.", e);
		}
		setSenderId(id);
		data = URLDecoder.decode(parts[2], "UTF-8");
	}

	@Override
	public int getSenderId() {
		return senderId;
	}

	@Override
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	@Override
	public String getMessage() {
		try {
			return formatMessage(data, getSenderId());
		} catch (UnsupportedEncodingException ex) {
			//never happens with UTF-8
			throw new RuntimeException(ex);
		}
	}

	/**
	 * This will format a message String using the given data.
	 *
	 * @param data The text to be transmitted.
	 * @param id Id of the message
	 * @return The message to be sent.
	 * @throws UnsupportedEncodingException If UTF-8 is not supported.
	 */
	public static String formatMessage(String data, int id) throws UnsupportedEncodingException {
		if (data == null) {
			data = "";
		}
		return KEY + " " + Integer.toString(id) + " " + URLEncoder.encode(data, "UTF-8");
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
