/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.singletontcplistener;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openiot.gsndatapusher.core.AbstractSensorAdapter;
import org.openiot.gsndatapusher.core.FieldType;
import static org.openiot.gsndatapusher.core.FieldType.Double;
import static org.openiot.gsndatapusher.core.FieldType.Int;
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
		builder.append(String.format(("<virtual-sensor name=\"%s\" priority=\"%d\" publish-to-lsm=\"%s\">%s"), config.getName(), config.getPriority(), String.valueOf(config.isPublishToLSM()), System.lineSeparator()));
		builder.append(String.format(("   <processing-class>%s"), System.lineSeparator()));
		builder.append(String.format(("      <class-name>org.openiot.gsn.vsensor.BridgeVirtualSensor</class-name>%s"), System.lineSeparator()));
		builder.append(String.format(("      <init-params>%s"), System.lineSeparator()));
		builder.append(String.format(("         <param name=\"allow-nulls\">false</param>%s"), System.lineSeparator()));
		builder.append(String.format(("         <param name=\"debug-mode\">false</param>%s"), System.lineSeparator()));
		builder.append(String.format(("      </init-params>%s"), System.lineSeparator()));
		builder.append(String.format(("      <output-structure>%s"), System.lineSeparator()));
		for (int i = 1; i <= config.getFieldCount(); i++) {
			builder.append(String.format(("         <field name=\"field%d\" type=\"%s\" />%s"), i, config.getFieldType().toString(), System.lineSeparator()));
		}
		builder.append(String.format(("      </output-structure>%s"), System.lineSeparator()));
		builder.append(String.format(("   </processing-class>%s"), System.lineSeparator()));
		builder.append(String.format(("   <life-cycle pool-size=\"%d\" />%s"), config.getPoolSize(), System.lineSeparator()));
		builder.append(String.format(("   <addressing />%s"), System.lineSeparator()));
		builder.append(String.format(("   <storage history-size=\"%s\" />%s"), config.getHistorySize(), System.lineSeparator()));
		builder.append(String.format(("   <streams>%s"), System.lineSeparator()));
		builder.append(String.format(("      <stream name=\"input1\">%s"), System.lineSeparator()));
		builder.append(String.format(("         <source alias=\"source1\" sampling-rate=\"%d\" storage-size=\"%d\">%s"), config.getSamplingRate(), config.getStorageSize(), System.lineSeparator()));
		builder.append(String.format(("            <address wrapper=\"singletontcp\">%s"), System.lineSeparator()));
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
		builder.append(String.format(("               <predicate key=\"fields\">%s</predicate>%s"), fields, System.lineSeparator()));
		builder.append(String.format(("               <predicate key=\"formats\">%s</predicate>%s"), formats, System.lineSeparator()));
		builder.append(String.format(("               <predicate key=\"bad-values\">%s</predicate>%s"), config.getBadValues(), System.lineSeparator()));
		builder.append(String.format(("               <predicate key=\"timezone\">%s</predicate>%s"), config.getTimeZone(), System.lineSeparator()));
		builder.append(String.format(("               <predicate key=\"port\">%d</predicate>%s"), config.getPort(), System.lineSeparator()));
		builder.append(String.format(("               <predicate key=\"id\">%s</predicate>%s"), String.format(Locale.US, "%f", config.getId()), System.lineSeparator()));
		builder.append(String.format(("            </address>%s"), System.lineSeparator()));
		builder.append(String.format(("            <query>select * from wrapper</query>%s"), System.lineSeparator()));
		builder.append(String.format(("         </source>%s"), System.lineSeparator()));
		builder.append(String.format(("         <query>select * from source1</query>%s"), System.lineSeparator()));
		builder.append(String.format(("      </stream>%s"), System.lineSeparator()));
		builder.append(String.format(("   </streams>%s"), System.lineSeparator()));
		builder.append(String.format(("</virtual-sensor>%s"), System.lineSeparator()));
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
