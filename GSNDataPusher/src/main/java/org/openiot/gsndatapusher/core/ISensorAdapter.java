package org.openiot.gsndatapusher.core;

import java.util.concurrent.Callable;

/**
 *
 * @author admin-jacoby
 */
public interface ISensorAdapter<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> {

	class SendResult {

		public boolean success;
		public String data;
	}

	String getGSNConfigFile(C config);

	String getGSNMetadataFile(C config);

	Callable<SendResult> sendData(C config);

	Callable<Boolean> setupSensorConnection(C config);

	Callable<Boolean> teardownSensorConnection(C config);
}
