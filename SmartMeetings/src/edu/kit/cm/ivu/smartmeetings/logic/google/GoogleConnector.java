package edu.kit.cm.ivu.smartmeetings.logic.google;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ShareCompat;

import com.google.api.services.smartmeetings.model.Reservation;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IGoogleConnector;

/**
 * This class implements all the calls needed by SmartMeetings to interact with
 * the google services.
 * 
 * @author Andreas Eberle
 * 
 */
public class GoogleConnector implements IGoogleConnector {

	private final Activity activity;

	public GoogleConnector(final Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean inviteFriendsToReservation(final Reservation reservation) {
		return inviteFriendsToReservationViaGooglePlus(reservation);
	}

	// TODO: Deeplinks?
	private boolean inviteFriendsToReservationViaGooglePlus(
			final Reservation reservation) {

		final String message = makeMessage(reservation);

		final Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
				.setType("text/plain").setText(message).getIntent()
				.setPackage("com.google.android.apps.plus");
		activity.startActivity(shareIntent);
		return true;
	}

	private static String makeMessage(final Reservation reservation) {
		// TODO: More dynamic message
		return "I just made Reservations for a Meeting! " + "\nRoom: "
				+ reservation.getRoom().getLabel() + "\nTime: from "
				+ reservation.getStartDate() + " to "
				+ reservation.getEndDate();
	}
}
