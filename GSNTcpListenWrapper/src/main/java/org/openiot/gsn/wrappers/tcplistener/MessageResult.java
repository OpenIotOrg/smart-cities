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
public class MessageResult implements Message {

	public static enum RESULT {

		OK,
		QUEUED,
		QUEUE_FULL,
		FAILED
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageResult.class);
	public static final String KEY = "R";
	private RESULT result;
	private double id;
	private int queueSize;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 4);
		if (parts.length != 4) {
			LOGGER.error("Incorrect part count: {}.", parts.length);
			throw new IllegalArgumentException("Incorrect part count.");
		}
		try {
			id = Double.parseDouble(parts[1]);
		} catch (NumberFormatException e) {
			LOGGER.error("ID must be double >= 0. Current value: {}.", parts[1]);
			throw new IllegalArgumentException("Not a numeric id.", e);
		}
		result = RESULT.valueOf(parts[2]);
		try {
			queueSize = Integer.parseInt(parts[3]);
		} catch (NumberFormatException e) {
			LOGGER.error("Queuesize must be integer. Current value: {}.", parts[3]);
			throw new IllegalArgumentException("Not a numeric queuesize.", e);
		}
	}

	@Override
	public int getSenderId() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void setSenderId(int senderId) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public String getMessage() {
		return formatMessage(id, result, queueSize);
	}

	/**
	 * This will format a message String using the given data.
	 *
	 * @param id Id of the message
	 * @param result
	 * @param queueSize
	 * @return The message to be sent.
	 */
	public static String formatMessage(double id, RESULT result, int queueSize) {
		return KEY + " " + Double.toString(id) + " " + result.name() + " " + Integer.toString(queueSize);
	}

	public double getId() {
		return id;
	}

	public void setId(double id) {
		this.id = id;
	}

	/**
	 * @return the result
	 */
	public RESULT getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(RESULT result) {
		this.result = result;
	}

	/**
	 * @return the queueSize
	 */
	public int getQueueSize() {
		return queueSize;
	}

	/**
	 * @param queueSize the queueSize to set
	 */
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
}
