package org.openiot.gsn.wrappers.tcplistener;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.usoog.commons.network.message.Message;
import java.lang.reflect.Type;
import java.util.Map;
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
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageResult.class);
	private static final Gson gson = new Gson();
	private static final Type type = new TypeToken<Map<String, String>>() {}.getType();
	public static final String KEY = "R";
	private RESULT result;
	private double id;
	private int queueSize;
	private Map<String, String> settings;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void initFromString(String message) throws Exception {
		String[] parts = message.split(" ", 5);
		if (parts.length < 4) {
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

		if (parts.length >= 5 && parts[4].length() > 1) {
			LOGGER.trace("Parsing {} as map.", parts[4]);

			try {
				settings = gson.fromJson(parts[4], type);
			} catch (JsonSyntaxException e) {
				LOGGER.info("Failed to parse map.");
			}
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
		return formatMessage(id, result, queueSize, settings);
	}

	/**
	 * This will format a message String using the given data.
	 *
	 * @param id Id of the message
	 * @param result
	 * @param queueSize
	 * @param settings
	 * @return The message to be sent.
	 */
	public static String formatMessage(double id, RESULT result, int queueSize, Map<String, String> settings) {
		String setString = "";
		if (settings != null && !settings.isEmpty()) {
			LOGGER.debug("Adding settings map {} to result.", settings.size());
			setString = " " + gson.toJson(settings, type);
		}
		return KEY + " " + Double.toString(id) + " " + result.name() + " " + Integer.toString(queueSize) + setString;
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

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

}
