package edu.kit.cm.ivu.smartmeetings.ui.util;

import java.io.IOException;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Utility class to deserialize objects of type {@link GenericJson} from String.
 * 
 * @author Kirill Rakhman
 */
public class JsonDeserializer {
	private JsonDeserializer() {
	}

	private static GsonFactory gsonFactory = new GsonFactory();

	public static <T> T deserialize(final String serialized,
			final Class<T> clazz) {
		final JsonParser parser = gsonFactory.createJsonParser(serialized);
		try {
			return parser.parseAndClose(clazz, null);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
