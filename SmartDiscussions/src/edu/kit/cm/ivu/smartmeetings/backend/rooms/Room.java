package edu.kit.cm.ivu.smartmeetings.backend.rooms;

/**
 * Represents a KIT room
 * 
 * @author David Kulicke
 * 
 */
public class Room {

	private final String uri;
	private final String label;
	private final String roomNumber;
	private final Integer numberOfSeats;
	private final Integer floorSpace;
	private final Building building;

	/**
	 * Constructs a new room with the given parameters
	 * 
	 * @param uri
	 * @param label
	 * @param roomNumber
	 * @param floorSpace
	 * @param numberOfSeats
	 * @param building
	 */
	public Room(final String uri, final String label, final String roomNumber,
			final Integer floorSpace, final Integer numberOfSeats,
			final Building building) {
		this.uri = uri;
		this.label = label;
		this.roomNumber = roomNumber;
		this.floorSpace = floorSpace;
		this.numberOfSeats = numberOfSeats;
		this.building = building;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the room number
	 */
	public String getRoomNumber() {
		return roomNumber;
	}

	/**
	 * @return the number of seats
	 */
	public Integer getNumberOfSeats() {
		return numberOfSeats;
	}

	/**
	 * @return the floor space
	 */
	public Integer getFloorSpace() {
		return floorSpace;
	}

	/**
	 * @return the building
	 */
	public Building getBuilding() {
		return building;
	}

	@Override
	public String toString() {
		return "Room " + this.getBuilding().getBuildingNumber() + "_"
				+ this.getRoomNumber();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Room)) {
			return false;
		}
		final Room other = (Room) obj;
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
