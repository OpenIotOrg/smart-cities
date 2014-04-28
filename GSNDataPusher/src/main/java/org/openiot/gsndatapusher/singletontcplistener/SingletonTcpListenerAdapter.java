package org.openiot.gsndatapusher.singletontcplistener;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.Callable;
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
	private Socket socket;

	@Override
	public String getGSNConfigFile(SingletonTcpListenerConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("<virtual-sensor name=\"%s\" priority=\"%d\" publish-to-lsm=\"%s\">\n", config.getName(), config.getPriority(), Boolean.toString(config.isPublishToLSM())));
		if (config.isPublishToLSM()) {
			builder.append(String.format("   <processing-class>\n"));
			builder.append(String.format("      <class-name>org.openiot.gsn.vsensor.LSMExporter</class-name>\n"));
			builder.append(String.format("      <init-params>\n"));
			builder.append(String.format("          <param name='allow-nulls'>false</param>\n"));
			builder.append(String.format("          <param name='publish-to-lsm'>true</param>\n"));
			builder.append(String.format("      </init-params>\n"));
			builder.append(String.format("      <output-structure>\n"));
			for (int i = 1; i <= config.getFieldCount(); i++) {
				builder.append(String.format("         <field name=\"field%d\" type=\"%s\" />\n", i, config.getFieldType().toString()));
			}
			builder.append(String.format("      </output-structure>\n"));
			builder.append(String.format("   </processing-class>\n"));
		} else {
			builder.append(String.format("   <processing-class>\n"));
			builder.append(String.format("      <class-name>org.openiot.gsn.vsensor.BridgeVirtualSensor</class-name>\n"));
			builder.append(String.format("      <init-params>\n"));
			builder.append(String.format("         <param name=\"allow-nulls\">false</param>\n"));
			builder.append(String.format("         <param name=\"debug-mode\">false</param>\n"));
			builder.append(String.format("      </init-params>\n"));
			builder.append(String.format("      <output-structure>\n"));
			for (int i = 1; i <= config.getFieldCount(); i++) {
				builder.append(String.format("         <field name=\"field%d\" type=\"%s\" />\n", i, config.getFieldType().toString()));
			}
			builder.append(String.format("      </output-structure>\n"));
			builder.append(String.format("   </processing-class>\n"));
		}

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
		builder.append(String.format("sensorName=opensense_1\n"));
		String fields = "";
		for (int i = 1; i <= config.getFieldCount(); i++) {
			if (i > 1) {
				fields += ",";
			}
			fields += String.format("field%d", i);
		}
		builder.append(String.format("source=\"http://openiot.eu\"\n"));
		builder.append(String.format("sourceType=lausanne\n"));
		builder.append(String.format("sensorType=%s\n", config.getType()));
		builder.append(String.format("information=\"A generated sensor\"\n"));
		builder.append(String.format("author=\"Fraunhofer IOSB\"\n"));
		builder.append(String.format("feature=\"http://lsm.deri.ie/OpenIoT/opensensefeature\"\n"));

		builder.append(String.format("fields=\"%s\"\n", fields));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			builder.append(String.format("field.field%d.propertyName=\"http://lsm.deri.ie/OpenIoT/Random%d\"\n", i, i));
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
		SendResult result = new SendResult();
		result.data = generateData(config);
//        logger.log(Level.FINE);
		try {
			PrintWriter printWriter = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream()));

			printWriter.print(result.data);
			printWriter.flush();
			result.success = true;
		} catch (IOException ex) {
			LOGGER.error("Failed to send data.", ex);
		}
		return result;
	}

	private String generateData(final SingletonTcpListenerConfig config) {
		/*
		 * ID 1.00000 {"temp"=23,"humid"=97}
		 */
		String result = String.format("ID %s {", String.format(Locale.US, "%f", config.getId()));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			result += String.format(("\"field%d\"=%s"), i, randomValue(config));
			if (i < config.getFieldCount()) {
				result += ",";
			}
		}
		result += ("}\n");
		return result;
	}

	private boolean connectToServer(SingletonTcpListenerConfig config) {
		boolean result = false;
		try {
			socket = new Socket();
			socket.setReuseAddress(true);
			socket.setTcpNoDelay(true);
			socket.connect(new InetSocketAddress(config.getServer(), config.getPort()));
			result = socket.isConnected();
		} catch (IOException ex) {
			LOGGER.error("Failed to connect to server.", ex);
		}
		return result;
	}

	private boolean disconnectFromServer() {
		boolean result = false;
		if (socket != null && socket.isConnected()) {
			try {
				socket.close();
				result = socket.isClosed();
			} catch (IOException ex) {
				LOGGER.error("Error while disconnecting from server.", ex);
			}
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
