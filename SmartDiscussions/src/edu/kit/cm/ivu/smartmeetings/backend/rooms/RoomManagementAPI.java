package edu.kit.cm.ivu.smartmeetings.backend.rooms;

import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import edu.kit.cm.ivu.smartdiscussions.Ids;

/**
 * This is an AppEngine endpoint class which provides the interfaces to the room
 * management services.
 * 
 * @author David Kulicke
 * 
 */
@Api(name = "smartmeetings", version = "v1", clientIds = {
		Ids.ANDROID_CLIENT_ID1, Ids.ANDROID_CLIENT_ID2, Ids.ANDROID_CLIENT_ID3,
		Ids.ANDROID_CLIENT_ID4, Ids.ANDROID_CLIENT_ID5, Ids.ANDROID_CLIENT_ID6,
		Ids.WEB_CLIENT_ID,
		com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID }, audiences = { Ids.ANDROID_AUDIENCE })
public class RoomManagementAPI {

	private static final RoomManagementLogic LOGIC = RoomManagementLogic
			.getInstance();

	/**
	 * Returns a list of all available buildings.
	 * 
	 * @return a list of all available buildings.
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getAllBuildings", path = "roommanagement/getAllBuildings")
	public List<Building> getAllBuildings() {
		return LOGIC.getAllBuildings();
	}

	/**
	 * Returns a list of all rooms.
	 * 
	 * @return a list of all rooms.
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getAllRooms", path = "roommanagement/getAllRooms")
	public List<Room> getAllRooms() {
		return LOGIC.getAllRooms();
	}

	/**
	 * Returns a list of all rooms located in the given building.
	 * 
	 * @param buildingID
	 *            the id of the corresponding building
	 * @return a list of all rooms located in the given building.
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getAllRoomsOfBuilding", path = "roommanagement/getAllRoomsOfBuilding")
	public List<Room> getAllRoomsOfBuilding(
			@Named("buildingID") final String buildingID) {
		return LOGIC.getAllRoomsOfBuilding(buildingID);
	}

	/**
	 * Returns a list which contains all rooms which match the given criteria.
	 * 
	 * @param criteria
	 *            the criteria which are used for the selection of the rooms
	 * @return a list which contains all rooms which match the given criteria
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.searchRoom", path = "roommanagement/searchRoom")
	public List<Room> searchRoom(final RoomSearchCriteria criteria) {
		return LOGIC.searchRoom(criteria.getCriteria());
	}

	/**
	 * Returns the room with the given id.
	 * 
	 * @param id
	 *            the id of the room
	 * @return the room with the given id
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getRoomById", path = "roommanagement/getRoomById")
	public Room getRoomById(@Named("id") final String id) {
		return LOGIC.getRoomById(id);
	}

	/**
	 * Returns a list of all available room properties and their subclasses.
	 * 
	 * @return a list of all available room properties and their subclasses
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getAllRoomProperties", path = "roommanagement/getAllRoomProperties")
	public List<RoomProperty> getAllRoomProperties() {
		return LOGIC.getAllRoomProperties();
	}

	/**
	 * Returns a list of all room properties the given room features. The
	 * properties' details are retrieved as well.
	 * 
	 * @param roomId
	 *            the room whose properties should be selected
	 * @return a list of all room properties the given room features
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getPropertiesOfRoom", path = "roommanagement/getPropertiesOfRoom")
	public List<RoomProperty> getPropertiesOfRoom(
			@Named("roomId") final String roomId) {
		return LOGIC.getPropertiesOfRoom(roomId);
	}

	/**
	 * Returns the reservation which has the given id.
	 * 
	 * @param reservationId
	 *            the id of the desired reservation
	 * @return the reservation which has the given id
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getReservationById", path = "roommanagement/getReservationById")
	public Reservation getReservationById(
			@Named("id") final String reservationId) {
		return LOGIC.getReservationById(reservationId);
	}

	/**
	 * Returns all reservations the given user has created.
	 * 
	 * @param user
	 *            the user performing this request and whose reservations should
	 *            be returned
	 * @return all reservations the given user has created
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getReservationsOfUser", path = "roommanagement/getReservationsOfUser")
	public List<Reservation> getReservationsOfUser(final User user)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		// TODO: replace getEmail with getUserId when app engine bug is fixed
		// (currently the latter always
		// returns null)
		return LOGIC.getReservationsOfUser(user.getEmail());
	}

	/**
	 * Returns all reservations belonging to the given room.
	 * 
	 * @param user
	 *            the user performing this request
	 * @param roomId
	 *            the room whose reservations should be returned
	 * @return all reservations belonging to the given room
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.getReservationsOfRoom", path = "roommanagement/getReservationsOfRoom")
	public List<Reservation> getReservationsOfRoom(final User user,
			@Named("roomId") final String roomId) throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}
		return LOGIC.getReservationsOfRoom(roomId);
	}

	/**
	 * Adds a new reservation for the given room with the given start and end
	 * date.
	 * 
	 * @param user
	 *            the user performing this request
	 * @param roomId
	 *            the room which should be reserved
	 * @param startDate
	 *            the start date of the reservation
	 * @param endDate
	 *            the end date of the reservation
	 * @return a ReturnStatus with the success value true if the reservation was
	 *         added successfully or false otherwise
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.addReservation", path = "roommanagement/addReservation")
	public ReturnStatus addReservation(final User user,
			@Named("roomId") final String roomId,
			@Named("startDate") final Date startDate,
			@Named("endDate") final Date endDate) throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		// TODO: replace getEmail with getUserId when app engine bug is fixed
		// (currently the latter always
		// returns null)
		return new ReturnStatus(LOGIC.addReservation(user.getEmail(), roomId,
				startDate, endDate));
	}

	/**
	 * Removes the given reservation.
	 * 
	 * @param user
	 *            the user performing this request
	 * @param reservationId
	 *            the id of the reservation which should be removed
	 * @return a ReturnStatus with the success value true if the reservation was
	 *         removed successfully or false otherwise
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.removeReservation", path = "roommanagement/removeReservation")
	public ReturnStatus removeReservation(final User user,
			@Named("reservationId") final String reservationId)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}
		return new ReturnStatus(LOGIC.removeReservation(user.getEmail(),
				reservationId));

	}

	/**
	 * Registers at the room of the given reservation.
	 * 
	 * @param user
	 *            the user performing this request
	 * @param reservationId
	 *            the id of the appropriate reservation
	 * @return a ReturnStatus with the success value true if the registration
	 *         was performed successfully or false otherwise
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.registerAtRoom", path = "roommanagement/registerAtRoom")
	public ReturnStatus registerAtRoom(final User user,
			@Named("reservationId") final String reservationId)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		// TODO: replace getEmail with getUserId when app engine bug is fixed
		// (currently the latter always
		// returns null)
		return new ReturnStatus(LOGIC.registerAtRoom(user.getEmail(),
				reservationId));
	}

	/**
	 * Unregisters from the room of the given reservation.
	 * 
	 * @param user
	 *            the user performing this request
	 * @param reservationId
	 *            the id of the appropriate reservation
	 * @return a ReturnStatus with the success value true if the unregistration
	 *         was performed successfully or false otherwise
	 * @throws OAuthRequestException
	 *             if the given user is invalid
	 * @author David Kulicke
	 */
	@ApiMethod(name = "roommanagement.unregisterFromRoom", path = "roommanagement/unregisterFromRoom")
	public ReturnStatus unregisterFromRoom(final User user,
			@Named("reservationId") final String reservationId) {
		// TODO: replace getEmail with getUserId when app engine bug is fixed
		// (currently the latter always
		// returns null)
		return new ReturnStatus(LOGIC.unregisterFromRoom(user.getEmail(),
				reservationId));
	}
}
