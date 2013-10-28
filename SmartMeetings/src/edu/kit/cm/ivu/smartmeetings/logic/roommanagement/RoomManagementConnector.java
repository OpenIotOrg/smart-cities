package edu.kit.cm.ivu.smartmeetings.logic.roommanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.smartmeetings.Smartmeetings.Roommanagement;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;
import com.google.api.services.smartmeetings.model.RoomSearchCriteria;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IRoomManagementConnector;

public class RoomManagementConnector implements IRoomManagementConnector {

	private final Roommanagement roomManagement;

	public RoomManagementConnector(final IEndpointConnector endpointConnector) {

		roomManagement = endpointConnector.getService().roommanagement();
	}

	@Override
	public List<Building> getAllBuildings() {
		List<Building> buildings = null;
		try {
			buildings = roomManagement.getAllBuildings().execute().getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (buildings == null) {
			buildings = Collections.emptyList();
		}
		return buildings;
	}

	@Override
	public List<Room> getAllRoomsOfBuilding(final Building building) {
		List<Room> rooms = null;
		try {
			rooms = roomManagement.getAllRoomsOfBuilding(building.getUri())
					.execute().getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (rooms == null) {
			rooms = Collections.emptyList();
		}
		return rooms;
	}

	@Override
	public List<Room> getAllRooms() {
		List<Room> rooms = null;
		try {
			rooms = roomManagement.getAllRooms().execute().getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (rooms == null) {
			rooms = Collections.emptyList();
		}
		return rooms;
	}

	@Override
	public List<Room> searchRoom(final Collection<RoomProperty> criteria) {
		final RoomSearchCriteria criteriaContainer = new RoomSearchCriteria();
		criteriaContainer.setCriteria(new ArrayList<RoomProperty>(criteria));
		List<Room> rooms = null;
		try {
			rooms = roomManagement.searchRoom(criteriaContainer).execute()
					.getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (rooms == null) {
			rooms = Collections.emptyList();
		}
		return rooms;
	}

	@Override
	public Room getRoomById(final String id) {
		Room room = null;
		try {
			room = roomManagement.getRoomById(id).execute();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return room;
	}

	@Override
	public List<RoomProperty> getAllRoomProperties() {
		List<RoomProperty> properties = null;
		try {
			properties = roomManagement.getAllRoomProperties().execute()
					.getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (properties == null) {
			properties = Collections.emptyList();
		}
		return properties;
	}

	@Override
	public List<RoomProperty> getPropertiesOfRoom(final Room room) {
		List<RoomProperty> properties = null;
		try {
			properties = roomManagement.getPropertiesOfRoom(room.getUri())
					.execute().getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (properties == null) {
			properties = Collections.emptyList();
		}
		return properties;
	}

	@Override
	public Reservation getReservationById(final String id) {
		Reservation reservation = null;
		try {
			reservation = roomManagement.getReservationById(id).execute();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return reservation;
	}

	@Override
	public List<Reservation> getReservationsOfUser() {
		List<Reservation> reservations = null;
		try {
			reservations = roomManagement.getReservationsOfUser().execute()
					.getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (reservations == null) {
			reservations = Collections.emptyList();
		}
		return reservations;
	}

	@Override
	public List<Reservation> getReservationsOfRoom(final Room room) {
		List<Reservation> reservations = null;
		try {
			reservations = roomManagement.getReservationsOfRoom(room.getUri())
					.execute().getItems();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (reservations == null) {
			reservations = Collections.emptyList();
		}
		return reservations;
	}

	@Override
	public boolean addReservation(final Room room, final Date startDate,
			final Date endDate) {
		final DateTime start = new DateTime(startDate);
		final DateTime end = new DateTime(endDate);
		boolean success = false;
		try {
			success = roomManagement.addReservation(end, room.getUri(), start)
					.execute().getSuccess();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public boolean removeReservation(final Reservation reservation) {
		boolean success = false;
		try {
			success = roomManagement
					.removeReservation(reservation.getReservationId())
					.execute().getSuccess();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public boolean registerAtRoom(final Reservation reservation) {
		boolean success = false;
		try {
			success = roomManagement
					.registerAtRoom(reservation.getReservationId()).execute()
					.getSuccess();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public boolean unregisterFromRoom(final Reservation reservation) {
		boolean success = false;
		try {
			success = roomManagement
					.unregisterFromRoom(reservation.getReservationId())
					.execute().getSuccess();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return success;
	}

}
