package org.openiot.gsn.wrappers.tcplistener;

import com.google.common.eventbus.Subscribe;
import com.usoog.commons.network.MessageListener;
import com.usoog.commons.network.NetworkServer;
import com.usoog.commons.network.NetworkServerConnection;
import com.usoog.commons.network.event.EventConnectionEstablished;
import com.usoog.commons.network.message.FactoryMessage;
import com.usoog.commons.network.message.Message;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
public class TcpListenWrapper extends AbstractWrapper implements MessageListener {

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
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpListenWrapper.class);
	/**
	 * Counter for the number of concurrent wrappers of this type.
	 */
	private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
	/**
	 * Handler that checks the validity of the posted data.
	 */
	private final JsonHandler handler = new JsonHandler();
	/**
	 * The factory used to decode incoming lines into Messages.
	 */
	private static final FactoryMessage messageFactory = new FactoryMessage();

	static {
		try {
			messageFactory.registerMessage(MessageData.class);
		} catch (InstantiationException | IllegalAccessException ex) {
			LOGGER.error("Failed to register messages.", ex);
		}
	}
	/**
	 * The handlers for the different message types.
	 */
	private final Map<String, MessageHandler> handlers = new HashMap<>();
	/**
	 * The data fields for this wrapper.
	 */
	private DataField[] dataFields;
	/**
	 * The port the wrapper listens on.
	 */
	private int port = 22002;
	/**
	 * Set server that listens on the given port.
	 */
	private NetworkServer server;

	@Override
	public boolean initialize() {
		setName("TcpListenWrapper-Thread" + (THREAD_COUNTER.incrementAndGet()));
		LOGGER.info("Initialising wrapper {}.", getName());
		AddressBean addressBean = getActiveAddressBean();
		String csvFields = addressBean.getPredicateValueWithException("fields");
		String csvFormats = addressBean.getPredicateValueWithException("formats");
		String timezone = addressBean.getPredicateValueWithDefault("timezone", JsonHandler.LOCAL_TIMEZONE_ID);
		String nullValues = addressBean.getPredicateValueWithDefault("bad-values", "");
		port = addressBean.getPredicateValueAsInt("port", port);

		if (!handler.initialize(csvFields, csvFormats, nullValues, timezone)) {
			LOGGER.warn("Initialisation of jsonHandler failed.");
		}
		dataFields = handler.getDataFields();

		createMessageHandlers();

		server = new NetworkServer(messageFactory);
		server.setPort(port);
		server.getEventBus().register(this);
		server.start();

		return true;
	}

	/**
	 * Initialise the message handlers.
	 */
	private void createMessageHandlers() {
		handlers.put(MessageData.KEY, new MessageHandler() {

			@Override
			public void handle(Message m) {
				LOGGER.debug("Handling message {}.", m.getMessage());
				MessageData md = (MessageData) m;
				String data = md.getData();
				handleData(data);
			}
		});
	}

	@Override
	public void dispose() {
		LOGGER.info("Disposing of wrapper {}.", getName());
		if (server != null) {
			server.stop();
			server.getEventBus().unregister(this);
		}
	}

	@Override
	public String getWrapperName() {
		return this.getClass().getName();
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataFields;
	}

	/**
	 * A new connection was opened by a client. Start listening to it. Currently
	 * we just listen to every connection ourselves. Each connection should get
	 * its own handling instance of a proper connection handler.
	 *
	 * @param e The event containing the new connection.
	 */
	@Subscribe
	public void newConnection(EventConnectionEstablished e) {
		NetworkServerConnection serverConnection = e.getServerConnection();
		if (serverConnection != null) {
			serverConnection.addMessageListener(this);
		}
	}

	@Override
	public void messageReceived(Message message) {
		MessageHandler h = handlers.get(message.getKey());
		if (h == null) {
			LOGGER.warn("Unknown message received: {}.", message.getMessage());
			return;
		}
		h.handle(message);
	}

	@Override
	public void connectionLost(String reason) {
	}

	@Override
	public void connectionClosed() {
	}

	/**
	 * We have received data. See if it's valid, and insert it if it is.
	 *
	 * @param data The data that was received.
	 */
	private void handleData(String data) {
		TreeMap<String, Serializable> elementData = handler.parseValues(data);
		StreamElement streamElement = new StreamElement(elementData, getOutputFormat());
		boolean insertionSuccess = postStreamElement(streamElement);
		LOGGER.debug("Inserting: {}, success: {}", elementData, insertionSuccess);
	}
}
