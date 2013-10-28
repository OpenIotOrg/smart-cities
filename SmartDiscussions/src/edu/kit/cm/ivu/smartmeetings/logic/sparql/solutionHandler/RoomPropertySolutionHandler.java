package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.RoomProperty;

/**
 * This class implements the {@link IQuerySolutionHandler} interface and is able
 * to create {@link RoomProperty} objects from QuerySolutions.
 * 
 * @author Andreas Eberle
 * @author Valentin Zickner
 */
public class RoomPropertySolutionHandler implements
		IQuerySolutionHandler<RoomProperty> {

	/**
	 * Handle existing Jena QuerySolutions and create objects
	 * 
	 * @param QuerySolution
	 *            solution Jena query solution to use for creation.
	 * @return RoomProperty Created RoomProperty object.
	 */
	@Override
	public RoomProperty createResult(final QuerySolution solution) {
		final Resource propertyClass = solution.getResource("propertyClass");
		final Literal propertyName = solution.getLiteral("propertyName");

		if (propertyClass != null && propertyName != null) {
			final RoomProperty newProperty = new RoomProperty(
					propertyClass.getURI(), propertyName.getString());
			return newProperty;
		} else {
			return null;
		}
	}
}
