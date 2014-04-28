package org.openiot.gsndatapusher.core;

/**
 *
 * @author admin-jacoby
 */
public enum FieldType {

	Int,
	Double,
	String;

	public String toString() {
		String result = "";
		switch (this) {
			case Int:
				result = "int";
				break;
			case Double:
				result = "double";
				break;
			case String:
				result = "string";
				break;
			default:

		}
		return result;
	}
}
