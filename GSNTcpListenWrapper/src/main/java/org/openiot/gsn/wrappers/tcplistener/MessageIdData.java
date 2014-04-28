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
	private double id;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 3);
		if (parts.length != 3) {
			LOGGER.error("Incorrect part count: {}.", parts.length);
			throw new IllegalArgumentException("Incorrect part count.");
		}
		try {
			id = Double.parseDouble(parts[1]);
		} catch (NumberFormatException e) {
			LOGGER.error("ID must be double >= 0. Current value: {}.", id);
			throw new IllegalArgumentException("Not a numeric id.", e);
		}
		data = URLDecoder.decode(parts[2], "UTF-8");
	}

	@Override
	public int getSenderId() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSenderId(int senderId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getMessage() {
		try {
			return formatMessage(data, id);
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
	public static String formatMessage(String data, double id) throws UnsupportedEncodingException {
		if (data == null) {
			data = "";
		}
		return KEY + " " + Double.toString(id) + " " + URLEncoder.encode(data, "UTF-8");
	}

	public double getId() {
		return id;
	}

	public void setId(double id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
