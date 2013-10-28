package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.content.Context;

import com.google.api.services.smartmeetings.model.Address;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ILogicFacadeFactory;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.IUser;
import edu.kit.cm.ivu.smartmeetings.ui.util.IScanCallback;

/**
 * This interface defines the basic methods the frontend for the GUI needs to
 * have. The methods are used to change the state of the GUI and to call to
 * external libraries. In the GUI state diagram, they relate to the state
 * transitions.
 * 
 * @author michael
 * 
 */
public interface IFrontend extends ILogicFacadeFactory {

	/**
	 * Shows the start screen of our app.
	 */
	public void showStartScreen();

	/**
	 * Shows a search result list with the given properties.
	 * 
	 * @param forWhat
	 *            The things the user wanted to search for
	 */
	public void searchRoom(List<RoomProperty> forWhat);

	/**
	 * Shows a search dialog where the user can select the properties he wants
	 * to search for.
	 */
	public void showSearchDialog();

	/**
	 * Shows the list of all buildings.
	 */
	public void showBuildingList();

	/**
	 * Shows the details and rooms of the given building.
	 * 
	 * @param building
	 *            the building whose details and rooms will be shown.
	 */
	public void showBuildingDetails(Building building);

	/**
	 * Shows the details of a given room.
	 * 
	 * @param room
	 *            The room we want to show details of.
	 */
	public void showRoomDetails(Room room);

	/**
	 * Starts the navigation to an address.
	 * 
	 * @param address
	 *            The address to navigate to.
	 */
	public void navigateTo(Address address);

	/**
	 * Gets the current user that is using the application.
	 * 
	 * @return The user or <code>null</code> if no account is registered.
	 */
	public IUser getUser();

	/**
	 * Shows the details to a given reservation.
	 * 
	 * @param item
	 *            The reservation we want to display the details for.
	 */
	public void showReservationDetails(Reservation item);

	/**
	 * Asks the user for a reservation of the given room by displaying an
	 * appropriate dialog.
	 * 
	 * @param room
	 *            The room to reserve.
	 */
	public void showReserveRoom(Room room);

	/**
	 * Shows the screen which enables the user to log into a room
	 * 
	 * @param activeReservation
	 *            the corresponding reservation
	 */
	public void showRoomLogin(Reservation activeReservation);

	/**
	 * Shows the screen which enables the user to log off from a room
	 * 
	 * @param activeReservation
	 *            the corresponding reservation
	 */
	public void showRoomLogoff(Reservation activeReservation);

	/**
	 * Force a QR code reading. This starts the QR scanner.
	 * 
	 * 
	 */
	void readQRCode();

	/**
	 * Sets the listener that is listening to QR events.
	 * 
	 * @param qrListener
	 *            The listener or <code>null</code> to disable QR recognition.
	 */
	void setQRListener(IScanCallback qrListener);

	/**
	 * Sets the listener that is listening to NFC events.
	 * 
	 * @param nfcListener
	 *            The listener or <code>null</code> to disable NFC recognition.
	 */
	void setNfcListener(IScanCallback nfcListener);

	/**
	 * Shows the process indicator
	 */
	public void showProgress();

	/**
	 * Hides the process indicator
	 */
	public void hideProgress();

	/**
	 * Shows the list of user discussions.
	 */
	public void showDiscussions();

	/**
	 * Shows a specific discussion identified by the topic paramter.
	 * 
	 * @param topic
	 *            The instance of a specific discussion.
	 */
	public void showDiscussion(Discussion topic);

	/**
	 * Displays an interface which helps the user to scan an object to discuss.
	 */
	public void showDiscussionScanning();

	/**
	 * Displays an interface which allows the user to create a new discussion.
	 */
	public void showDiscussionCreation();

	/**
	 * Displays an interface which allows the user to share a discussion.
	 */
	public void showDiscussionSharing(String id, String name);

	public Context getContext();

}
