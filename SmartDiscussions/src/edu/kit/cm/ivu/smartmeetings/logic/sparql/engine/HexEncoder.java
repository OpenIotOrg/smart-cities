package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

import java.util.Formatter;

/**
 * This class implements an encoding mechanism that takes the given string,
 * converts it to a byte array and then builds a string of hex characters
 * representing the byte array.
 * 
 * @author Andreas Eberle
 * 
 */
public final class HexEncoder {
	/**
	 * Private constructor, because this is a utility class that does not
	 * support objects of itself.
	 */
	private HexEncoder() {
	}

	/**
	 * Encodes any string to this encoding.
	 * 
	 * @param string
	 *            The string that shall be encoded.
	 * @return Returns the encoded string.
	 */
	public static String encode(final String string) {
		final byte[] bytes = string.getBytes();

		final StringBuilder sb = new StringBuilder(bytes.length * 2);

		final Formatter formatter = new Formatter(sb);
		for (final byte b : bytes) {
			formatter.format("%02x", b);
		}
		formatter.close();

		return sb.toString();
	}

	/**
	 * Decodes a string that has been encoded with {@link #encode(String)}.
	 * 
	 * @param encoded
	 *            The encoded string that needs to be decoded.
	 * @return Returns the decoded string.
	 */
	public static String decode(final String encoded) {
		final int len = encoded.length();
		final byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(encoded.charAt(i), 16) << 4) + Character
					.digit(encoded.charAt(i + 1), 16));
		}

		return new String(data);
	}
}
