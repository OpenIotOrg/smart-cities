package edu.kit.cm.ivu.smartmeetings.backend.rooms;

import java.io.Serializable;
import java.util.Date;

/**
 * This class defines a data object containing the data of a reservation.
 * 
 * @author Andreas Eberle
 * @author David Kulicke
 */
public class Reservation implements Serializable {

	private static final long serialVersionUID = 5348528542408297021L;
	private final String reservationId;
	private final String userId;
	private final Room room;
	private final Date timestamp;
	private final Date startDate;
	private final Date endDate;
	private final boolean loginState;

	/**
	 * Constructs a new {@link Reservation}.
	 * 
	 * @param reservationId
	 *            the id of the reservation
	 * @param userId
	 *            the id of the user performing this reservation
	 * @param room
	 *            the room the reservation belongs to
	 * @param startDate
	 *            the start date of the reservation
	 * @param endDate
	 *            the end date of the reservation
	 * @param registered
	 *            true if the user has already registered at the reservation's
	 *            room, false otherwise
	 */
	public Reservation(final String reservationId, final String userId,
			final Room room, final Date startDate, final Date endDate,
			final Date timestamp, final boolean loginState) {
		this.reservationId = reservationId;
		this.userId = userId;
		this.room = room;
		this.startDate = startDate;
		this.endDate = endDate;
		this.timestamp = timestamp;
		this.loginState = loginState;
	}

	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the id of this reservation.
	 * 
	 * @return the id of this reservation
	 */
	public String getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the id of the reservation's room.
	 * 
	 * @return the id of the reservation's room
	 */
	public String getRoomId() {
		if (room != null) {
			return room.getUri();
		}
		return null;
	}

	/**
	 * Returns the room this reservation belongs to.
	 * 
	 * @return the room this reservation belongs to
	 */
	public Room getRoom() {
		return room;
	}

	/**
	 * Returns the start date of this reservation.
	 * 
	 * @return the start date of this reservation
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Returns the end date of this reservation.
	 * 
	 * @return the end date of this reservation
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Returns the creation timestamp of this reservation.
	 * 
	 * @return the creation timestamp of this reservation
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns true if the user is registered at the reservation's room or false
	 * otherwise.
	 * 
	 * @return true if the user is registered at the reservation's room or false
	 *         otherwise
	 */
	public boolean getLoginState() {
		return loginState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((reservationId == null) ? 0 : reservationId.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Reservation other = (Reservation) obj;
		if (reservationId == null) {
			if (other.reservationId != null) {
				return false;
			}
		} else if (!reservationId.equals(other.reservationId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Reservation with ID: " + reservationId;
	}
}
