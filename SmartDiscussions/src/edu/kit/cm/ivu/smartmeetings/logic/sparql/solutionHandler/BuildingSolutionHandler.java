package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.Address;
import edu.kit.cm.ivu.smartmeetings.backend.rooms.Building;

/**
 * This class implements the {@link IQuerySolutionHandler} interface and is able
 * to create {@link Building} objects from QuerySolutions.
 * 
 * @author David Kulicke
 */
public class BuildingSolutionHandler implements IQuerySolutionHandler<Building> {

	@Override
	public Building createResult(final QuerySolution solution) {
		final Resource buildingResource = solution.getResource("building");
		final Literal label = solution.getLiteral("label");
		final Literal number = solution.getLiteral("buildingNumber");
		final Literal street = solution.getLiteral("street");
		final Literal zipCode = solution.getLiteral("zipCode");
		final Literal city = solution.getLiteral("city");
		final Literal lat = solution.getLiteral("latitude");
		final Literal lon = solution.getLiteral("longitude");
		final Literal area = solution.getLiteral("areaName");
		final Literal year = solution.getLiteral("buildYear");

		if (buildingResource != null && label != null && number != null
				&& street != null && zipCode != null && city != null
				&& area != null) {
			final String uri = buildingResource.getURI();
			final String buildYear = (year != null) ? year.getString() : null;
			final double latitude = (lat != null) ? lat.getDouble() : 0.0d;
			final double longitude = (lon != null) ? lon.getDouble() : 0.0d;
			final Address address = new Address(street.getString(),
					zipCode.getString(), city.getString(), latitude, longitude);

			return new Building(uri, label.getString(), number.getString(),
					address, area.getString(), buildYear);
		} else {
			return null;
		}
	}

}
