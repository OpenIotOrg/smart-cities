package org.openiot.gsn.wrappers.tcplistener;

import com.google.common.eventbus.Subscribe;
import com.usoog.commons.network.MessageListener;
import com.usoog.commons.network.NetworkServer;
import com.usoog.commons.network.NetworkServerConnection;
import com.usoog.commons.network.event.EventConnectionEstablished;
import com.usoog.commons.network.message.FactoryMessage;
import com.usoog.commons.network.message.Message;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper that listens on a TCP port. It communicates with single-line
 * (newline terminated) messages. Currently using the USOOG server code, but
 * that should be changed.
 *
 * @author scf
 */
public class SingletonTcpListenManager {

	/**
	 * An interface for objects that handle messages.
	 */
	private static interface MessageHandler {

		/**
		 * Deal with the message.
		 *
		 * @param m The message to deal with.
		 */
		public void handle(Message m, NetworkServerConnection serverConnection);
	}

	private static class ConnectionHandler implements MessageListener {

		private final NetworkServerConnection serverConnection;
		private final Map<String, MessageHandler> handlers;

		public ConnectionHandler(NetworkServerConnection serverConnection, Map<String, MessageHandler> handlers) {
			this.serverConnection = serverConnection;
			this.handlers = handlers;
		}

		@Override
		public void messageReceived(Message message) {
			MessageHandler h = handlers.get(message.getKey());
			if (h == null) {
				LOGGER.warn("Unknown message received: {}.", message.getMessage());
				return;
			}
			h.handle(message, serverConnection);
		}

		@Override
		public void connectionLost(String reason) {
		}

		@Override
		public void connectionClosed() {
		}
	}

	private static class ServerInfo {

		public int connections = 0;
		public SingletonTcpListenManager server;

		public ServerInfo(SingletonTcpListenManager server) {
			this.server = server;
		}
	}
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonTcpListenManager.class);
	/**
	 * The factory used to decode incoming lines into Messages.
	 */
	private final FactoryMessage messageFactory = new FactoryMessage();
	/**
	 * The handlers for the different message types.
	 */
	private final Map<String, MessageHandler> handlers = new HashMap<>();

	/**
	 * The port the wrapper listens on.
	 */
	private int port = 22002;

	/**
	 * Set server that listens on the given port.
	 */
	private NetworkServer server;
	/**
	 * Mapping from ID to corresponding wrapper
	 */
	private final Map<Double, SingletonTcpListenWrapper> wrappers = new HashMap<>();

	/**
	 * Mapping from port to corresponding Server
	 */
	private static final Map<Integer, ServerInfo> servers = new HashMap<>();

	private SingletonTcpListenManager(int port) {
		this.port = port;
		initialize();
	}

	private static synchronized ServerInfo getInstance(int port) {
		if (!servers.containsKey(port)) {
			servers.put(port, new ServerInfo(new SingletonTcpListenManager(port)));
		}
		return servers.get(port);
	}

	public static void subscribe(int port, double id, SingletonTcpListenWrapper wrapper) {
		if (getInstance(port).server.subscribe(id, wrapper)) {
			getInstance(port).connections++;
		}
	}

	public static void unsubscribe(int port, double id, SingletonTcpListenWrapper wrapper) {
		if (getInstance(port).server.unsubscribe(id, wrapper)) {
			getInstance(port).connections--;
			if (getInstance(port).connections <= 0) {
				getInstance(port).server.dispose();
				servers.remove(port);
			}
		}
	}

	private boolean subscribe(double id, SingletonTcpListenWrapper wrapper) {
		if (wrappers.containsKey(id)) {
			LOGGER.warn("id is already in use: {}, replacing old wrapper.", id);
		}
		wrappers.put(id, wrapper);
		return true;
	}

	private boolean unsubscribe(double id, SingletonTcpListenWrapper wrapper) {
		if (!wrappers.containsKey(id)) {
			LOGGER.warn("can't unsubscribe wrapper. ID not found: ", id);
			return false;
		}
		if (!wrappers.get(id).equals(wrapper)) {
			LOGGER.warn("Unauthorized wrapper tried to unsubscribe with id: ", id);
			return false;
		}
		wrappers.remove(id);
		return true;
	}

	private void initialize() {
		try {
			messageFactory.registerMessage(MessageIdData.class);
		} catch (InstantiationException | IllegalAccessException ex) {
			LOGGER.error("Failed to register messages.", ex);
		}

		createMessageHandlers();

		server = new NetworkServer(messageFactory);
		server.setPort(port);
		server.getEventBus().register(this);
		server.start();
	}

	/**
	 * Initialise the message handlers.
	 */
	private void createMessageHandlers() {
		handlers.put(MessageIdData.KEY, new MessageHandler() {

			@Override
			public void handle(Message m, NetworkServerConnection serverConnection) {
				LOGGER.trace("Handling message '{}'.", m.getMessage());
				MessageIdData md = (MessageIdData) m;
				String data = md.getData();
				double id = md.getId();
				if (!wrappers.containsKey(id)) {
					LOGGER.warn("No wrapper with ID: '{}' in '{}'.", id, wrappers.keySet());
					MessageResult mr = new MessageResult();
					mr.setResult(MessageResult.RESULT.FAILED);
					serverConnection.sendMessage(mr);
				} else {
					SingletonTcpListenWrapper wrapper = wrappers.get(id);
					MessageResult mr = wrapper.handleData(data);
					serverConnection.sendMessage(mr);
				}
			}

		});
	}

	public void dispose() {
		if (server != null) {
			server.stop();
			server.getEventBus().unregister(this);
		}
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
			serverConnection.addMessageListener(new ConnectionHandler(serverConnection, handlers));
		}
	}

}
