package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import java.util.Date;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.Reservation;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.Room;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.RoomManagementLogic;

/**
 * This class implements the {@link IQuerySolutionHandler} interface and is able
 * to create {@link Reservation} objects from QuerySolutions.
 * 
 * @author Valentin Zickner
 * @author Michael Zangl
 * @author David Kulicke
 */
public class ReservationSolutionHandler implements
		IQuerySolutionHandler<Reservation> {
	private String reservationId;
	private String roomId;
	private String userId;

	private static final Logger LOG = Logger.getLogger("SmartDiscussions");

	/**
	 * Set the default reservation id for objects created with this solution
	 * handler.
	 * 
	 * @param reservationId
	 *            reservation id to use as default.
	 */
	public void setReservationId(final String reservationId) {
		this.reservationId = reservationId;
	}

	/**
	 * Set the default user id for objects created with this solution handler.
	 * 
	 * @param userId
	 *            user id to use as default.
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Set the default room id for objects created with this solution handler.
	 * 
	 * @param roomId
	 *            room id to use as default.
	 */
	public void setRoomId(final String roomId) {
		this.roomId = roomId;
	}

	/**
	 * Create possible solutions for this result item.
	 * 
	 * @param QuerySolution
	 *            solution Sparql-Query Solution to use
	 * @return IReservation New reservation object.
	 */
	@Override
	public Reservation createResult(final QuerySolution solution) {
		String reservationId = this.reservationId;
		Date timestamp = null;
		Date startDate = null;
		Date endDate = null;
		Room room = null;
		String roomId = this.roomId;
		String userId = this.userId;

		// Save reservation id
		final Resource reservationIdResource = solution
				.getResource("reservation");
		if (reservationIdResource != null) {
			reservationId = reservationIdResource.getURI();
		}

		// Save room
		final Resource roomResource = solution.getResource("room");
		if (roomResource != null) {
			roomId = roomResource.getURI();
		}
		room = RoomManagementLogic.getInstance().getRoomById(roomId);
		if (room == null) {
			LOG.fine("Corresponding room does not exist");
			return null;
		}

		// Save user
		final Literal userLiteral = solution.getLiteral("user");
		if (userLiteral != null) {
			userId = userLiteral.getString();
		}

		// Save creation time, start time and end time
		final Literal startDateLiteral = solution.getLiteral("startDate");
		startDate = new Date(startDateLiteral.getLong());

		final Literal endDateLiteral = solution.getLiteral("endDate");
		endDate = new Date(endDateLiteral.getLong());

		final Literal timestampLiteral = solution.getLiteral("timestamp");
		timestamp = new Date(timestampLiteral.getLong());

		final Literal loginLiteral = solution.getLiteral("loginStatus");
		final boolean registered = loginLiteral == null ? false : loginLiteral
				.getBoolean();
		return new Reservation(reservationId, userId, room, startDate, endDate,
				timestamp, registered);
	}

}
