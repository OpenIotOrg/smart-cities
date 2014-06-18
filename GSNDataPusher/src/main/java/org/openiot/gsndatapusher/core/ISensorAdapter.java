package org.openiot.gsndatapusher.core;

import java.util.Map;
import java.util.concurrent.Callable;
import org.openiot.gsn.wrappers.tcplistener.MessageResult;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public interface ISensorAdapter<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> {

	class SendResult {

		public boolean success;
		public String data;
		public int queue;
		public Map<String, String> settings;
		public MessageResult.RESULT result;
	}

	String getGSNConfigFile(C config);

	String getGSNMetadataFile(C config);

	Callable<SendResult> sendData(C config);

	Callable<Boolean> setupSensorConnection(C config);

	Callable<Boolean> teardownSensorConnection(C config);
}
