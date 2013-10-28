package edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

public interface IRoomManagementConnector {

	/**
	 * Returns a list of all available buildings.
	 * 
	 * @return a list of all available buildings
	 */
	public List<Building> getAllBuildings();

	/**
	 * Returns a list of all available rooms.
	 * 
	 * @return A list of all available rooms.
	 */
	public List<Room> getAllRooms();

	/**
	 * Returns a list of all rooms located in the given building.
	 * 
	 * @param building
	 *            the building
	 * @return a list of all rooms located in the given building
	 */
	public List<Room> getAllRoomsOfBuilding(Building building);

	/**
	 * 
	 * @param criteria
	 *            If these criteria are available in a room, it will be
	 *            returned.
	 * @return Returns a list of all rooms fulfilling the given criteria.
	 */
	public List<Room> searchRoom(Collection<RoomProperty> criteria);

	/**
	 * 
	 * @param id
	 *            Id of the searched room.
	 * @return Returns a list containing only the room with the given id or<br>
	 *         an empty list if no room with the given id has been found.
	 */
	public Room getRoomById(String id);

	/**
	 * Gets a list of all available properties.
	 * 
	 * @return Returns a List that will be filled with all available properties
	 *         and their child properties.
	 */
	public List<RoomProperty> getAllRoomProperties();

	/**
	 * Gets a list of properties for a given room.
	 * 
	 * @param room
	 *            The room to get the properties for.
	 * @return Returns a List that will be filled with the found room details
	 *         and their child properties.
	 */
	public List<RoomProperty> getPropertiesOfRoom(Room room);

	/**
	 * Get a list of Reservation by a given reservation id.
	 * 
	 * @param id
	 *            Id of the reservation to search for.
	 * @return IReservation object of reservation, otherwise null.
	 */
	public Reservation getReservationById(String id);

	/**
	 * Get a list of reservations of the current user
	 * 
	 * @return List<IReservation> List of reservations of the current user.
	 */
	public List<Reservation> getReservationsOfUser();

	/**
	 * Get a list of reservations of a room
	 * 
	 * @param Room
	 *            room Room object to search for
	 * @return List<IReservation> List of reservations of the given room.
	 */
	public List<Reservation> getReservationsOfRoom(Room room);

	/**
	 * Add a new reservation for a given room and date.
	 * 
	 * @param Room
	 *            room Room to reservate.
	 * @param Date
	 *            Start of reservation time.
	 */
	public boolean addReservation(Room room, Date startDate, Date endDate);

	/**
	 * Remove reservation from SPARQL-Endpoint
	 * 
	 * @param IReservation
	 *            reservation Reservation to remove
	 */
	public boolean removeReservation(Reservation reservation);

	/**
	 * Registers at the room of the given reservation.
	 * 
	 * @param reservation
	 *            the reservation of the corresponding room
	 * @return Returns true if the user has been registered successfully,<br>
	 *         false otherwise.
	 */
	public boolean registerAtRoom(Reservation reservation);

	/**
	 * Unregisters from the room of the given reservation.
	 * 
	 * @param reservation
	 *            the reservation of the corresponding room
	 * @return Returns true if the user has been unregistered successfully,<br>
	 *         false otherwise.
	 */
	public boolean unregisterFromRoom(Reservation reservation);
}
