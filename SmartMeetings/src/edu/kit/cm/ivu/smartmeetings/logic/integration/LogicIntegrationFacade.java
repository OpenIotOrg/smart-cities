package edu.kit.cm.ivu.smartmeetings.logic.integration;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;

import com.google.api.client.util.DateTime;
import com.google.api.services.smartmeetings.Smartmeetings;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.PostContainer;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IGoogleConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IInternalSensorConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.ISmartDiscussionsConnector;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;
import edu.kit.cm.ivu.smartmeetings.logic.roommanagement.RoomManagementConnector;

/**
 * This class acts as a facade of all synchronous domain logic operations.
 * Therefore this facade does not implement any behavior. It just delegates the
 * calls to their real implementation.
 * 
 * @author Andreas Eberle
 * 
 */
public class LogicIntegrationFacade implements ISyncLogicFacade {

	private final RoomManagementConnector roomManagementConnector;
	private final IGoogleConnector googleConnector;
	private final IEndpointConnector endpointConnector;
	private final ISmartDiscussionsConnector smartDiscussionsConnector;
	private final IInternalSensorConnector internalSensorConnector;

	public LogicIntegrationFacade(
			final RoomManagementConnector roomManagementConnector,
			final IGoogleConnector googleConnector,
			final ISmartDiscussionsConnector smartDiscussionsConnector,
			final IInternalSensorConnector internalSensorConnector,
			final IEndpointConnector endpointConnector) {
		this.roomManagementConnector = roomManagementConnector;
		this.googleConnector = googleConnector;
		this.smartDiscussionsConnector = smartDiscussionsConnector;
		this.internalSensorConnector = internalSensorConnector;
		this.endpointConnector = endpointConnector;
	}

	// =========================================== RoomManagementConnector calls
	// follow here =====================

	@Override
	public List<Building> getAllBuildings() {
		return roomManagementConnector.getAllBuildings();
	}

	@Override
	public List<Room> getAllRoomsOfBuilding(final Building building) {
		return roomManagementConnector.getAllRoomsOfBuilding(building);
	}

	@Override
	public List<Room> getAllRooms() {
		return roomManagementConnector.getAllRooms();
	}

	@Override
	public List<Room> searchRoom(final Collection<RoomProperty> criteria) {
		return roomManagementConnector.searchRoom(criteria);
	}

	@Override
	public Room getRoomById(final String id) {
		return roomManagementConnector.getRoomById(id);
	}

	@Override
	public List<RoomProperty> getAllRoomProperties() {
		return roomManagementConnector.getAllRoomProperties();
	}

	@Override
	public List<RoomProperty> getPropertiesOfRoom(final Room room) {
		return roomManagementConnector.getPropertiesOfRoom(room);
	}

	@Override
	public List<Reservation> getReservationsOfUser() {
		return roomManagementConnector.getReservationsOfUser();
	}

	@Override
	public List<Reservation> getReservationsOfRoom(final Room room) {
		return roomManagementConnector.getReservationsOfRoom(room);
	}

	@Override
	public boolean addReservation(final Room room, final Date startDate,
			final Date endDate) {
		return roomManagementConnector.addReservation(room, startDate, endDate);
	}

	@Override
	public boolean removeReservation(final Reservation reservation) {
		return roomManagementConnector.removeReservation(reservation);
	}

	@Override
	public boolean registerAtRoom(final Reservation reservation) {
		return roomManagementConnector.registerAtRoom(reservation);
	}

	@Override
	public boolean unregisterFromRoom(final Reservation reservation) {
		return roomManagementConnector.unregisterFromRoom(reservation);
	}

	@Override
	public Reservation getReservationById(final String id) {
		return roomManagementConnector.getReservationById(id);
	}

	// =========================================== IGoogleConnector calls follow
	// here =====================

	@Override
	public boolean inviteFriendsToReservation(final Reservation reservation) {
		return googleConnector.inviteFriendsToReservation(reservation);
	}

	// =========================================== ISmartDiscussionsConnector
	// calls follow
	// here =====================

	@Override
	public Discussion getTopicInformation(final String topic) {
		return smartDiscussionsConnector.getTopicInformation(topic);
	}

	@Override
	public Discussion getTopicInformation(final String topic, final String name) {
		return smartDiscussionsConnector.getTopicInformation(topic, name);
	}

	@Override
	public List<Discussion> getUserTopics() {
		return smartDiscussionsConnector.getUserTopics();
	}

	@Override
	public List<PostContainer> getPosts(final String topic) {
		return smartDiscussionsConnector.getPosts(topic);
	}

	@Override
	public List<PostContainer> getPosts(final String topic, final DateTime older) {
		return smartDiscussionsConnector.getPosts(topic, older);
	}

	@Override
	public PostContainer writePost(final String topic, final String message) {
		return smartDiscussionsConnector.writePost(topic, message);
	}

	@Override
	public String setUsername(final String token) {
		return endpointConnector.setUsername(token);
	}

	@Override
	public List<String> removeUserTopic(final String topic) {
		return smartDiscussionsConnector.removeUserTopic(topic);
	}

	@Override
	public PostContainer getLatestPost(final String topic) {
		return smartDiscussionsConnector.getLatestPost(topic);
	}

	@Override
	public void registerSensingStatusReceiver(final BroadcastReceiver receiver) {
		internalSensorConnector.registerSensingStatusReceiver(receiver);
	}

	@Override
	public void startSensing(final int refreshRate, final String roomURI) {
		internalSensorConnector.startSensing(refreshRate, roomURI);
	}

	@Override
	public void stopSensing() {
		internalSensorConnector.stopSensing();
	}

	/**
	 * @return
	 * @see edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector#getUserId()
	 */
	@Override
	public String getUserId() {
		return endpointConnector.getUserId();
	}

	/**
	 * @return
	 * @see edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector#getUserName()
	 */
	@Override
	public String getUserName() {
		return endpointConnector.getUserName();
	}

	/**
	 * @return
	 * @see edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector#getService()
	 */
	@Override
	public Smartmeetings getService() {
		return endpointConnector.getService();
	}

	@Override
	public void setPushId(final String pushId) {
		endpointConnector.setPushId(pushId);
	}

}
