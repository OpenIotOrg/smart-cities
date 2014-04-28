package org.openiot.gsndatapusher.core;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author admin-jacoby
 * @param <A>
 * @param <C>
 */
public abstract class AbstractSensorAdapter<A extends ISensorAdapter<A, C>, C extends ISensorConfig<A, C>> implements ISensorAdapter<A, C> {

	private static final String validCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	protected Random random;
	private static long nextHour = 0;

	public AbstractSensorAdapter() {
		random = new Random();
	}

	@Override
	public abstract String getGSNConfigFile(C config);

	@Override
	public abstract String getGSNMetadataFile(C config);

	protected String randomValue(C config) {
		String result = "";
		switch (config.getFieldType()) {
			case Int:
				long now = System.currentTimeMillis() / 1000;
				long span = nextHour - now;
				if (span < 0) {
					Calendar nextHourCal = Calendar.getInstance();
					nextHourCal.set(Calendar.MILLISECOND, 0);
					nextHourCal.set(Calendar.SECOND, 0);
					nextHourCal.set(Calendar.MINUTE, 0);
					nextHourCal.add(Calendar.HOUR_OF_DAY, 1);
					nextHour = nextHourCal.getTimeInMillis() / 1000;
					span = nextHour - now;
				}
				result = String.valueOf(span);
				break;
			case Double:
				result = String.format(Locale.US, "%f", random.nextDouble());
				break;
			case String:
				result = randomString(20);
				break;
			default:
		}
		return result;
	}

	private String randomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(validCharacters.charAt(random.nextInt(validCharacters.length())));
		}
		return sb.toString();
	}

}
