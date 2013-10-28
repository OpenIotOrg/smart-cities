package edu.kit.cm.ivu.smartmeetings.backend.sensors.phone;

/**
 * Represents phone sensor data.
 * 
 * @author Andreas Bender
 */
public class SensorData {

	private Integer temperature;

	private Integer pressure;

	private Integer humidity;

	private String roomURI;

	public Integer getTemperature() {
		return temperature;
	}

	public SensorData setTemperature(final Integer temperature) {
		this.temperature = temperature;
		return this;
	}

	public Integer getPressure() {
		return pressure;
	}

	public SensorData setPressure(final Integer pressure) {
		this.pressure = pressure;
		return this;
	}

	public Integer getHumidity() {
		return humidity;
	}

	public SensorData setHumidity(final Integer humidity) {
		this.humidity = humidity;
		return this;
	}

	public String getRoomURI() {
		return roomURI;
	}

	public SensorData setRoomURI(final String roomURI) {
		this.roomURI = roomURI;
		return this;
	}

}
