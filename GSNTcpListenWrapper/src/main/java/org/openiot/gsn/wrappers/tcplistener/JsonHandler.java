/**
 * Copyright (c) 2011-2014, OpenIoT
 *
 * This file is part of OpenIoT.
 *
 * OpenIoT is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * OpenIoT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenIoT. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: OpenIoT mailto: info@openiot.eu
 */
package org.openiot.gsn.wrappers.tcplistener;

import org.openiot.gsn.beans.DataField;
import org.openiot.gsn.utils.CaseInsensitiveComparator;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;

/**
 * A handler for dealing with data in Json format. possible formats for the
 * timestamp fields are available:
 * http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormat.html
 * Possible timezones: http://joda-time.sourceforge.net/timezones.html
 */
public class JsonHandler {

	/**
	 * Parse the given value as a time according to the given time format.
	 *
	 * @param format The format that should be used for parsing.
	 * @param value The value that should be parsed.
	 * @return The resulting time.
	 * @throws IllegalArgumentException
	 */
	public static DateTime parseTimeStamp(String format, String value) throws IllegalArgumentException {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.parseDateTime(value);
	}
	/**
	 * The logger for this class.
	 */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JsonHandler.class);
	/**
	 * The local timezone id.
	 */
	public static final String LOCAL_TIMEZONE_ID = DateTimeZone.getDefault().getID();
	/**
	 * The default timestamp field key.
	 */
	private static final String TIMESTAMP = "timed";
	/**
	 * The json marshaller.
	 */
	private final Gson gson = new Gson();
	/**
	 * The timezone used when interpreting time fields.
	 */
	private DateTimeZone timeZone;
	/**
	 * The names of the data fields.
	 */
	private String[] fields;
	/**
	 * Invalid values that are turned into null.
	 */
	private String[] nulls;
	/**
	 * The formats of the different data fields.
	 */
	private Map<String, String> formats;

	public boolean initialize(String inFields, String inFormats, String nullValues) {
		return initialize(inFields, inFormats, nullValues, LOCAL_TIMEZONE_ID);
	}

	public boolean initialize(String inFields, String inFormats, String nullValues, String timeZone) {

		try {
			this.timeZone = DateTimeZone.forID(timeZone);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Not a recognised time zone: {}.", timeZone);
			LOGGER.trace("", e);
			this.timeZone = DateTimeZone.UTC;
		}
		String[] tempFormats;
		try {
			this.fields = generateFieldIdx(inFields, true);
			tempFormats = generateFieldIdx(inFormats, false);
			this.nulls = generateFieldIdx(nullValues, true);
			////////////////////////
			// TODO: Check that the lengths are the same
			////////////////////////

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
		if (!validateFormats(tempFormats)) {
			return false;
		}
		if (fields.length != tempFormats.length) {
			LOGGER.error("loading the json-wrapper failed as the length of fields({}) doesn't match the length of formats({})", fields.length, tempFormats.length);
			return false;
		}

		formats = new HashMap<>();
		for (int i = 0; i < fields.length; i++) {
			formats.put(fields[i], tempFormats[i]);
		}

		return true;

	}

	public static boolean validateFormats(String[] formats) {
		for (String format : formats) {
			if (format.equalsIgnoreCase("numeric") || format.equalsIgnoreCase("string")) {
				// Nothing to do in this case.
			} else if (isTimeStampFormat(format)) {
				try {
					String tmp = DateTimeFormat.forPattern(getTimeStampFormat(format)).print(System.currentTimeMillis());
				} catch (IllegalArgumentException e) {
					LOGGER.error("Validating the time-format({}) used by the CSV-wrapper is failed. ", format);
					return false;
				}
			} else {
				LOGGER.error("The format ({}) used by the CSV-Wrapper doesn't exist. Acceptable formats: numeric, string", format);
				return false;
			}
		}
		return true;

	}

	/**
	 * Removes the space from the fields. Split the rawFields using comma as the
	 * separator.
	 *
	 * @param rawFields
	 * @param toLowerCase, if false, the case is preserved. if true, the actual
	 * outputs will be in lower-case.
	 * @return
	 * @throws IOException
	 */
	public static String[] generateFieldIdx(String rawFields, boolean toLowerCase) throws IOException {
		String[] toReturn = new CSVReader(new StringReader(rawFields)).readNext();
		if (toReturn == null) {
			return new String[0];
		}
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = toReturn[i].trim();
			if (toLowerCase) {
				toReturn[i] = toReturn[i].toLowerCase();
			}
		}
		return toReturn;
	}

	public TreeMap<String, Serializable> parseValues(String data) {
		Map<String, Object> values = gson.fromJson(data, Map.class);
		TreeMap<String, Serializable> se = convertTo(values);
		return se;
	}

	/**
	 * Convert the data map into a StreamElement compatible data map.
	 *
	 * @param values The data to convert.
	 * @return The StreamElement
	 */
	public TreeMap<String, Serializable> convertTo(Map<String, Object> values) {
		TreeMap<String, Serializable> streamElement = new TreeMap<>(new CaseInsensitiveComparator());
		for (String field : fields) {
			streamElement.put(field, null);
		}
		HashMap<String, String> timeStampFormats = new HashMap<>();
		for (Map.Entry<String, Object> valueSet : values.entrySet()) {
			String field = valueSet.getKey().toLowerCase();
			String value = valueSet.getValue().toString();
			String format = formats.get(field);

			if (format == null) {
				LOGGER.debug("Found unknown key: {}.", field);
				continue;
			}
			if (!isNull(nulls, value)) {

				if (format.equalsIgnoreCase("numeric")) {
					try {
						streamElement.put(field, Double.parseDouble(value));
					} catch (java.lang.NumberFormatException e) {
						LOGGER.error("Parsing to Numeric fails: Value to parse: {}.", value);
						throw e;
					}
				} else if (format.equalsIgnoreCase("string")) {
					streamElement.put(field, value);
				} else if (isTimeStampFormat(format)) {
					String tempValue = "";
					String tempFormat = "";
					if (streamElement.get(field) != null) {
						tempValue = (String) streamElement.get(field);
						tempFormat = timeStampFormats.get(field);
					}
					if (isTimeStampLeftPaddedFormat(format)) {
						value = StringUtils.leftPad(value, getTimeStampFormat(format).length(), '0');
					}

					tempValue += value;
					tempFormat += getTimeStampFormat(format);
					streamElement.put(field, tempValue);
					timeStampFormats.put(field, tempFormat);
				}
			}
		}
		for (String timeField : timeStampFormats.keySet()) {
			String timeFormat = timeStampFormats.get(timeField);
			String timeValue = (String) streamElement.get(timeField);
			try {
				DateTime x = DateTimeFormat.forPattern(timeFormat).withZone(getTimeZone()).parseDateTime(timeValue);
				streamElement.put(timeField, x.getMillis());
			} catch (IllegalArgumentException e) {
				LOGGER.error("Parsing error: TimeFormat=" + timeFormat + " , TimeValue=" + timeValue);
				LOGGER.error(e.getMessage(), e);
				throw e;
			}
		}

		return streamElement;
	}

	public static String getTimeStampFormat(String input) {
		if (input.contains("timestampl(")) {
			return input.substring("timestampl(".length(), input.indexOf(")")).trim();
		} else {
			return input.substring("timestamp(".length(), input.indexOf(")")).trim();
		}
	}

	public static boolean isTimeStampFormat(String input) {
		return (input.toLowerCase().startsWith("timestamp(") || input.toLowerCase().startsWith("timestampl(")) && input.endsWith(")");
	}

	public static boolean isTimeStampLeftPaddedFormat(String input) {
		return input.toLowerCase().startsWith("timestampl(") && input.endsWith(")");
	}

	public static boolean isNull(String[] possibleNullValues, String value) {
		if (value == null || value.length() == 0) {
			return true;
		}
		for (String possibleNullValue : possibleNullValues) {
			if (possibleNullValue.equalsIgnoreCase(value.trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The names of the data fields.
	 */
	public String[] getFields() {
		return fields;
	}

	public DataField[] getDataFields() {
		HashMap<String, String> flds = new HashMap<>();
		for (String field : getFields()) {
			String type = formats.get(field);
			if (isTimeStampFormat(type)) {
				//GSN doesn't support timestamp data type, all timestamp values are supposed to be bigint.
				flds.put(field, "bigint");
			} else if (type.equalsIgnoreCase("numeric")) {
				flds.put(field, "numeric");
			} else {
				flds.put(field, "string");
			}
		}
		DataField[] toReturn = new DataField[flds.size()];
		int i = 0;
		for (String key : flds.keySet()) {
			toReturn[i++] = new DataField(key, flds.get(key));
		}
		return toReturn;
	}

	/**
	 * @return The values that are considered invalid and are turned into nulls.
	 */
	public String[] getNulls() {
		return nulls;
	}

	/**
	 * @return The timezone used when dealing with date/time fields.
	 */
	public DateTimeZone getTimeZone() {
		return timeZone;
	}

}
