package edu.kit.cm.ivu.smartmeetings.ui.util;

import com.google.api.services.smartmeetings.model.Address;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Room;

/**
 * Utility class which creates string representations of objects that can be
 * displayed by the ui.
 * 
 * @author David Kulicke
 * 
 */
public final class PrettyPrint {

	private PrettyPrint() {

	}

	/**
	 * Returns a string representation of the given room.
	 * 
	 * @param room
	 *            a room
	 * @return a string representation of the given room
	 */
	public static String toString(final Room room) {
		if (room == null) {
			return "";
		} else {
			return room.getLabel() + " " + room.getRoomNumber();
		}
	}

	/**
	 * Returns a string representation of the given building.
	 * 
	 * @param building
	 *            a building
	 * @return a string representation of the given building
	 */
	public static String toString(final Building building) {
		if (building == null) {
			return "";
		} else {
			return building.getBuildingNumber() + " " + building.getLabel();
		}
	}

	/**
	 * Returns a string representation of the given address.
	 * 
	 * @param address
	 *            an address
	 * @return a string representation of the given address
	 */
	public static String toString(final Address address) {
		if (address == null) {
			return "";
		} else {
			return address.getStreet() + ", " + address.getZipCode() + " "
					+ address.getCity();
		}
	}
}
