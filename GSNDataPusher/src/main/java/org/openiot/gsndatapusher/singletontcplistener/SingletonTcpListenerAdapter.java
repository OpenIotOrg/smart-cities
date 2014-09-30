package org.openiot.gsndatapusher.singletontcplistener;

import com.google.common.eventbus.Subscribe;
import com.usoog.commons.network.NetworkClient;
import com.usoog.commons.network.event.EventConnectionEstablished;
import com.usoog.commons.network.event.EventMessageRecieved;
import com.usoog.commons.network.message.FactoryMessage;
import com.usoog.commons.network.message.Message;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import org.openiot.gsn.wrappers.tcplistener.MessageResult;
import org.openiot.gsndatapusher.core.AbstractSensorAdapter;
import org.openiot.gsndatapusher.core.FieldType;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin-jacoby
 */
public class SingletonTcpListenerAdapter extends AbstractSensorAdapter<SingletonTcpListenerAdapter, SingletonTcpListenerConfig> {

	/**
	 * The logger for this class.
	 */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SingletonTcpListenerAdapter.class);
	/**
	 * The factory used to decode incoming lines into Messages.
	 */
	private static final FactoryMessage messageFactory = new FactoryMessage();

	static {
		try {
			messageFactory.registerMessage(MessageResult.class);
		} catch (InstantiationException | IllegalAccessException ex) {
			LOGGER.error("Failed to register message.", ex);
		}
	}

	private NetworkClient client;
	private SendResult sendResult;

	@Override
	public String getGSNConfigFile(SingletonTcpListenerConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("<virtual-sensor name=\"%s\" priority=\"%d\" publish-to-lsm=\"%s\">\n", config.getName(), config.getPriority(), Boolean.toString(config.isPublishToLSM())));

		builder.append(String.format("   <processing-class>\n"));
		builder.append(String.format("      <class-name>org.openiot.gsn.wrappers.tcplistener.BridgeLsmWebVS</class-name>\n"));
		builder.append(String.format("      <init-params>\n"));
		builder.append(String.format("          <param name='allow-nulls'>false</param>\n"));
		builder.append(String.format("          <param name='publish-to-lsm'>%s</param>\n", (config.isPublishToLSM() ? "true" : "false")));
		builder.append(String.format("      </init-params>\n"));
		StringBuilder output = new StringBuilder();
		StringBuilder input = new StringBuilder();

		output.append(String.format("      <output-structure>\n"));
		input.append(String.format("      <web-input>\n"));
		input.append(String.format("        <command name=\"cmd1\">\n"));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			output.append(String.format("         <field name=\"field%d\" type=\"%s\" />\n", i, config.getFieldType().toString()));
			input.append(String.format("             <field name=\"field%d\" type=\"%s\">field%d</field>\n", i, config.getFieldType().toString(), i));
		}
		input.append(String.format("        </command>\n"));
		input.append(String.format("      </web-input>\n"));
		output.append(String.format("      </output-structure>\n"));

		builder.append(input);
		builder.append(output);
		builder.append(String.format("   </processing-class>\n"));

		builder.append(String.format("   <life-cycle pool-size=\"%d\" />\n", config.getPoolSize()));
		builder.append(String.format("   <addressing />\n"));
		builder.append(String.format("   <storage history-size=\"%s\" />\n", config.getHistorySize()));
		builder.append(String.format("   <streams>\n"));
		builder.append(String.format("      <stream name=\"input1\">\n"));
		builder.append(String.format("         <source alias=\"source1\" sampling-rate=\"%d\" storage-size=\"%d\">\n", config.getSamplingRate(), config.getStorageSize()));
		builder.append(String.format("            <address wrapper=\"singletontcp\">\n"));
		String fields = "";
		String formats = "";
		for (int i = 1; i <= config.getFieldCount(); i++) {
			if (i > 1) {
				fields += ", ";
				formats += ", ";
			}
			fields += String.format("field%d", i);
			formats += getTransportFormat(config.getFieldType());
		}
		builder.append(String.format("               <predicate key=\"fields\">%s</predicate>\n", fields));
		builder.append(String.format("               <predicate key=\"formats\">%s</predicate>\n", formats));
		builder.append(String.format("               <predicate key=\"bad-values\">%s</predicate>\n", config.getBadValues()));
		builder.append(String.format("               <predicate key=\"timezone\">%s</predicate>\n", config.getTimeZone()));
		builder.append(String.format("               <predicate key=\"port\">%d</predicate>\n", config.getPort()));
		builder.append(String.format("               <predicate key=\"id\">%s</predicate>\n", String.format(Locale.US, "%f", config.getId())));
		builder.append(String.format("            </address>\n"));
		builder.append(String.format("            <query>select * from wrapper</query>\n"));
		builder.append(String.format("         </source>\n"));
		builder.append(String.format("         <query>select * from source1</query>\n"));
		builder.append(String.format("      </stream>\n"));
		builder.append(String.format("   </streams>\n"));
		builder.append(String.format("</virtual-sensor>\n"));
		return builder.toString();
	}

	@Override
	public String getGSNMetadataFile(SingletonTcpListenerConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("sensorName=SomeNameHere\n"));
		String fields = "";
		for (int i = 1; i <= config.getFieldCount(); i++) {
			if (i > 1) {
				fields += ",";
			}
			fields += String.format("field%d", i);
		}
		builder.append(String.format("source=\"http://openiot.eu\"\n"));
		builder.append(String.format("sourceType=lausanne\n"));
		builder.append(String.format("sensorType=\"%s\"\n", config.getType()));
		builder.append(String.format("information=\"A generated sensor\"\n"));
		builder.append(String.format("author=\"FraunhoferIOSB\"\n"));
		// TODO: select from a list of given features.
		builder.append(String.format("feature=\"http://lsm.deri.ie/OpenIoT/opensensefeature\"\n"));

		builder.append(String.format("fields=\"%s\"\n", fields));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			builder.append(String.format("field.field%d.propertyName=\"http://openiot.eu/OpenIoT/Random%d\"\n", i, i));
			builder.append(String.format("field.field%d.unit=Percent\n", i));
		}
		builder.append(String.format("latitude=46.529838\n"));
		builder.append(String.format("longitude=6.596818\n"));
		return builder.toString();
	}

	private String getTransportFormat(FieldType type) {
		String result = "";
		switch (type) {
			case Int:
				result = "numeric";
				break;
			case Double:
				result = "numeric";
				break;
			case String:
				result = "string";
				break;
			default:
		}
		return result;
	}

	@Override
	public Callable<SendResult> sendData(final SingletonTcpListenerConfig config) {
		return new Callable<SendResult>() {

			@Override
			public SendResult call() throws Exception {
				return sendDataInternal(config);
			}
		};
	}

	private SendResult sendDataInternal(SingletonTcpListenerConfig config) {
		final SendResult sr = sendResult;
		if (sr == null) {
			LOGGER.warn("Sending data without sendResult.");
			return sr;
		}

		sr.success = false;
		sr.data = generateData(config);

		synchronized (sr) {
			client.sendLine(sr.data);
			try {
				sr.wait();
			} catch (InterruptedException ex) {
			}
		}
		if (sr.settings != null) {
			for (Map.Entry<String, String> es : sr.settings.entrySet()) {
				config.setSetpointFor(es.getKey(), es.getValue());
			}
		}

		return sr;
	}

	@Subscribe
	public void messageReceived(EventMessageRecieved e) {
		final SendResult sr = sendResult;
		if (sr == null) {
			LOGGER.warn("Received response before or after we expected one.");
			return;
		}
		Message m = e.getMessage();
		if (m instanceof MessageResult) {
			MessageResult mr = (MessageResult) m;
			synchronized (sr) {
				sr.result = mr.getResult();
				sr.queue = mr.getQueueSize();
				sr.settings = mr.getSettings();
				switch (mr.getResult()) {
					case OK:
					case QUEUED:
						sr.success = true;
						break;

					default:
						sr.success = false;
						break;
				}
				sr.notify();
			}
		}
	}

	@Subscribe
	public void connectionEstablished(EventConnectionEstablished e) {
		final SendResult sr = sendResult;
		if (sr == null) {
			LOGGER.warn("connection established when we are not expecting one.");
			return;
		}
		synchronized (sr) {
			sr.success = true;
			sr.notify();
		}
	}

	private String generateData(final SingletonTcpListenerConfig config) {
		/*
		 * ID 1.00000 {"temp"=23,"humid"=97}
		 */
		String result = String.format("ID %s {", String.format(Locale.US, "%f", config.getId()));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			String name = String.format("field%d", i);
			result += String.format("\"%s\"=%s", name, getDataFor(config, name));
			if (i < config.getFieldCount()) {
				result += ",";
			}
		}
		result += ("}");
		return result;
	}

	private String getDataFor(final SingletonTcpListenerConfig config, String field) {
		if (config.getFieldType() == FieldType.Int) {
			int sp = config.getIntSetpointFor(field, 0);
			if (sp == 0) {
				return randomValue(config);
			} else {
				int last = config.getLastIntValueFor(field, 0);
				int next = last + (int) (0.1 * (sp - last));
				config.setLastValueFor(field, next);
				return Integer.toString(next);
			}
		}
		return randomValue(config);
	}

	private boolean connectToServer(SingletonTcpListenerConfig config) {
		final SendResult sr = new SendResult();
		sendResult = sr;

		sr.success = false;
		boolean result;
		client = new NetworkClient(messageFactory);
		client.getEventBus().register(this);
		client.setServer(config.getServer());
		client.setPort(config.getPort());

		synchronized (sr) {
			client.connect();
			try {
				sr.wait();
			} catch (InterruptedException e) {
			}
		}
		result = sr.success;
		return result;
	}

	private boolean disconnectFromServer() {
		sendResult = null;
		boolean result = false;
		try {
			client.close();
			client.getEventBus().unregister(this);
			result = true;
		} catch (IllegalArgumentException e) {
		}
		return result;
	}

	@Override
	public Callable<Boolean> setupSensorConnection(final SingletonTcpListenerConfig config) {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return connectToServer(config);
			}
		};
	}

	@Override
	public Callable<Boolean> teardownSensorConnection(SingletonTcpListenerConfig config) {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return disconnectFromServer();
			}
		};
	}

}
