package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.RoomProperty;

/**
 * An {@link IQuerySolutionHandler} which is able to create {@link RoomProperty}
 * objects from query solutions containing sensor data.
 * 
 * @author David Kulicke
 * 
 */
public class SensorSolutionHandler implements
		IQuerySolutionHandler<RoomProperty> {

	@Override
	public RoomProperty createResult(final QuerySolution solution) {
		final Resource sensorResource = solution.getResource("sensor");
		final Literal sensorNameLiteral = solution.getLiteral("sensorName");

		if (sensorResource != null && sensorNameLiteral != null) {
			final String id = sensorResource.getURI();
			final String name = sensorNameLiteral.getString();

			return new RoomProperty(id, name);
		} else {
			return null;
		}
	}

}
