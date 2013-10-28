package edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces;

import com.google.api.services.smartmeetings.model.Reservation;

import edu.kit.cm.ivu.smartmeetings.logic.integration.LogicIntegrationFacade;

/**
 * This interface defines all the methods a connector to the google services
 * must supply for the {@link LogicIntegrationFacade} to work.
 * 
 * @author Andreas Eberle
 * 
 */
public interface IGoogleConnector {

	/**
	 * Sends an invitation for a reservation to friends.
	 * 
	 * @param reservation
	 *            The reservation that shall be sent to the friends.
	 * 
	 * @return Returns true if the operation succeeded, <br>
	 *         false otherwise.
	 */
	boolean inviteFriendsToReservation(Reservation reservation);
}
