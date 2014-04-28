package org.openiot.gsn.wrappers.tcplistener;

import com.usoog.commons.network.message.Message;
import java.io.Serializable;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.openiot.gsn.beans.AddressBean;
import org.openiot.gsn.beans.DataField;
import org.openiot.gsn.beans.StreamElement;
import org.openiot.gsn.wrappers.AbstractWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper that listens on a TCP port. It communicates with single-line
 * (newline terminated) messages. Currently using the USOOG server code, but
 * that should be changed.
 *
 * @author scf
 */
public class SingletonTcpListenWrapper extends AbstractWrapper {

	/**
	 * An interface for objects that handle messages.
	 */
	private static interface MessageHandler {

		/**
		 * Deal with the message.
		 *
		 * @param m The message to deal with.
		 */
		public void handle(Message m);
	}
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonTcpListenWrapper.class);
	/**
	 * Counter for the number of concurrent wrappers of this type.
	 */
	private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
	/**
	 * Handler that checks the validity of the posted data.
	 */
	private final JsonHandler handler = new JsonHandler();
	/**
	 * The data fields for this wrapper.
	 */
	private DataField[] dataFields;
	/**
	 * The port the wrapper listens on.
	 */
	private int port = 22002;
	/**
	 * Our sensor id.
	 */
	private double id = -1;
	private final Queue<StreamElement> dataQueue = new ArrayBlockingQueue<>(5);

	@Override
	public boolean initialize() {
		setName("SingletonTcpListenWrapper-Thread" + (THREAD_COUNTER.decrementAndGet()));
		LOGGER.info("Initialising wrapper {}.", getName());
		AddressBean addressBean = getActiveAddressBean();
		String csvFields = addressBean.getPredicateValueWithException("fields");
		String csvFormats = addressBean.getPredicateValueWithException("formats");

		String timezone = addressBean.getPredicateValueWithDefault("timezone", JsonHandler.LOCAL_TIMEZONE_ID);
		String nullValues = addressBean.getPredicateValueWithDefault("bad-values", "");
		port = addressBean.getPredicateValueAsInt("port", port);
		id = Double.parseDouble(addressBean.getPredicateValueWithException("id"));
		if (id <= 0) {
			LOGGER.error("id must be a double >= 0. Current value: " + id);
		}

		if (!handler.initialize(csvFields, csvFormats, nullValues, timezone)) {
			LOGGER.warn("Initialisation of jsonHandler failed.");
		}
		dataFields = handler.getDataFields();

		SingletonTcpListenManager.subscribe(port, id, this);
		return true;
	}

	@Override
	public void dispose() {
		LOGGER.info("Disposing of wrapper {}.", getName());
		SingletonTcpListenManager.unsubscribe(port, id, this);
	}

	@Override
	public String getWrapperName() {
		return this.getClass().getName();
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataFields;
	}

	@Override
	public void run() {
		while (isActive()) {
			StreamElement nextElement = dataQueue.poll();
			while (nextElement != null) {
				forwardData(nextElement);
				nextElement = dataQueue.poll();
			}

			synchronized (this) {
				if (dataQueue.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
		}
	}

	/**
	 * We have received data. See if it's valid, and insert it if it is.
	 *
	 * @param data The data that was received.
	 */
	public void handleData(String data) {
		TreeMap<String, Serializable> elementData = handler.parseValues(data);
		StreamElement streamElement = new StreamElement(elementData, getOutputFormat());
		LOGGER.debug("Sensor {}, Queueing: {}", id, elementData);
		try {
			synchronized (this) {
				dataQueue.add(streamElement);
				this.notify();
			}
		} catch (IllegalStateException e) {
			LOGGER.warn("Could not add data for sensor {}, queue full!", id);
		}
	}

	public void forwardData(StreamElement streamElement) {
		LOGGER.debug("Sensor {}, forwarding data.", id);
		boolean insertionSuccess = postStreamElement(streamElement);
		LOGGER.debug("Sensor {}, success: {}", id, insertionSuccess);
	}
}
