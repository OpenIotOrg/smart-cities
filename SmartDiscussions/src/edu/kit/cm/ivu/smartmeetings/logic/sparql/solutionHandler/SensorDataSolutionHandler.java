package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.kit.cm.ivu.smartmeetings.backend.rooms.RoomProperty;

/**
 * An {@link IQuerySolutionHandler} which is able to create {@link RoomProperty}
 * objects from query solutions containing sensor data.
 * 
 * @author David Kulicke
 * 
 */
public class SensorDataSolutionHandler implements
		IQuerySolutionHandler<RoomProperty> {

	@Override
	public RoomProperty createResult(final QuerySolution solution) {
		final Literal timeLiteral = solution.getLiteral("time");
		final Literal propertyNameLiteral = solution.getLiteral("propertyName");
		final Literal valueLiteral = solution.getLiteral("value");
		final Literal unitLiteral = solution.getLiteral("unit");

		if (timeLiteral != null && propertyNameLiteral != null
				&& valueLiteral != null) {
			final long timeVal = timeLiteral.getLong();
			final Date date = new Date(timeVal);

			final DateFormat dateFormat = DateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT, Locale.GERMANY);
			dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			final String dateString = dateFormat.format(date);

			final StringBuilder nameBuilder = new StringBuilder();
			nameBuilder.append(propertyNameLiteral.getString());
			nameBuilder.append(": ");
			nameBuilder.append(valueLiteral.getString());
			if (unitLiteral != null) {
				nameBuilder.append(' ');
				nameBuilder.append(unitLiteral.getString());
			}
			nameBuilder.append(" (");
			nameBuilder.append(dateString);
			nameBuilder.append(')');

			final String id = String.valueOf(System.nanoTime());
			return new RoomProperty(id, nameBuilder.toString());
		} else {
			return null;
		}
	}

}
