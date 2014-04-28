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
public class MessageData implements Message {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageData.class);
	public static final String KEY = "D";
	private String data;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 2);
		if (parts.length == 2) {
			data = URLDecoder.decode(parts[1], "UTF-8");
		} else {
			LOGGER.error("Incorrect part count: " + parts.length);
		}
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
			return formatMessage(data);
		} catch (UnsupportedEncodingException ex) {
			//never happens with UTF-8
			throw new RuntimeException(ex);
		}
	}

	/**
	 * This will format a message String using the given data.
	 *
	 * @param data The text to be transmitted.
	 * @return The message to be sent.
	 * @throws UnsupportedEncodingException If UTF-8 is not supported.
	 */
	public static String formatMessage(String data) throws UnsupportedEncodingException {
		if (data == null) {
			data = "";
		}
		return KEY + " " + URLEncoder.encode(data, "UTF-8");
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
