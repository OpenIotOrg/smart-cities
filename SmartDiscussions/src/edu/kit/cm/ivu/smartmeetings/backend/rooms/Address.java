package edu.kit.cm.ivu.smartmeetings.backend.rooms;

/**
 * Represents a building address
 * 
 * @author David Kulicke
 * 
 */
public class Address {

	private final String street;
	private final String zipCode;
	private final String city;

	private final Double latitude;
	private final Double longitude;

	/**
	 * Constructs a new address with the given street, zipcode, city and
	 * coordinates
	 * 
	 * @param street
	 * @param zipCode
	 * @param city
	 * @param latitude
	 * @param longitude
	 */
	public Address(final String street, final String zipCode,
			final String city, final double latitude, final double longitude) {
		this.street = street;
		this.zipCode = zipCode;
		this.city = city;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
}
