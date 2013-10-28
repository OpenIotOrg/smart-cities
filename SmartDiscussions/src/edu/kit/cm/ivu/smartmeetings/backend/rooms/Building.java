package edu.kit.cm.ivu.smartmeetings.backend.rooms;


/**
 * Represents a KIT building
 * 
 * @author David Kulicke
 * 
 */
public class Building {

	private final String uri;
	private final String label;
	private final String buildingNumber;
	private final String campusAreaName;
	private final String buildYear;
	private final Address address;

	/**
	 * Constructs a new {@link Building} with the given parameters
	 * 
	 * @param uri
	 * @param label
	 * @param buildingNumber
	 * @param address
	 * @param campusAreaName
	 */
	public Building(final String uri, final String label,
			final String buildingNumber, final Address address,
			final String campusAreaName, final String buildYear) {
		this.uri = uri;
		this.label = label;
		this.buildingNumber = buildingNumber;
		this.campusAreaName = campusAreaName;
		this.address = address;
		this.buildYear = buildYear;
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
	 * @return the buildingNumber
	 */
	public String getBuildingNumber() {
		return buildingNumber;
	}

	/**
	 * @return the name of the campus area
	 */
	public String getCampusAreaName() {
		return campusAreaName;
	}

	/**
	 * @return the buildYear
	 */
	public String getBuildYear() {
		return buildYear;
	}

	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "Building " + buildingNumber;
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
		if (!(obj instanceof Building)) {
			return false;
		}
		final Building other = (Building) obj;
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
