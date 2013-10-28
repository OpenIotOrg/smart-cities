package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.Building;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.Room;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.RoomManagementLogic;

/**
 * This class implements the {@link IQuerySolutionHandler} interface and is able
 * to create {@link Room} objects from QuerySolutions.
 * 
 * @author Andreas Eberle
 * @author David Kulicke
 */
public class RoomSolutionHandler implements IQuerySolutionHandler<Room> {

	private final Map<String, Building> buildingCache;

	/**
	 * Constructs a new {@link RoomSolutionHandler}.
	 */
	public RoomSolutionHandler() {
		// init cache
		buildingCache = new HashMap<String, Building>();
	}

	@Override
	public Room createResult(final QuerySolution solution) {
		final Resource roomResource = solution.getResource("room");
		final Resource buildingResource = solution.getResource("building");

		final Literal label = solution.getLiteral("label");
		final Literal roomNumber = solution.getLiteral("roomNumber");
		final Literal numberOfSeats = solution.getLiteral("numberOfSeats");
		final Literal floorSpace = solution.getLiteral("floorSpace");

		if (roomResource != null && buildingResource != null && label != null
				&& roomNumber != null) {
			final String roomID = roomResource.getURI();
			final String buildingID = buildingResource.getURI();

			// do cache lookup first - only if the building wasn't fetched yet,
			// it must be retrieved from the database.
			Building building = buildingCache.get(buildingID);
			if (building == null) {
				building = RoomManagementLogic.getInstance().getBuildingById(
						buildingID);
				if (building == null) {
					return null;
				}
				buildingCache.put(buildingID, building);
			}
			final Integer floorSpaceVal = (floorSpace != null) ? floorSpace
					.getInt() : null;
			final Integer numberOfSeatsVal = (numberOfSeats != null) ? numberOfSeats
					.getInt() : null;
			return new Room(roomID, label.getString(), roomNumber.getString(),
					floorSpaceVal, numberOfSeatsVal, building);
		} else {
			return null;
		}
	}

}
