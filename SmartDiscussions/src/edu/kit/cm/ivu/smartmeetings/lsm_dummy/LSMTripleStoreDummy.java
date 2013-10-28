package edu.kit.cm.ivu.smartmeetings.lsm_dummy;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import lsm.beans.Observation;
import lsm.beans.ObservedProperty;
import lsm.beans.Place;
import lsm.beans.Sensor;
import lsm.server.LSMServer;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlQuery;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlSelect;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlUpdate;
import edu.kit.cm.ivu.smartmeetings.lsm_dummy.vocabulary.LSM;
import edu.kit.cm.ivu.smartmeetings.lsm_dummy.vocabulary.SSN;

public class LSMTripleStoreDummy implements LSMServer {

	private final ResourceBundle sparql;

	private final String updateAddress;
	private final String endpointAddress;
	private final String resourceNamespace;
	private final String defaultMetaGraph;
	private final String defaultDataGraph;
	private final String prefixes;

	private static final Logger LOG = Logger.getLogger("SmartDiscussions");

	/**
	 * Constructs a new {@link LSMTripleStoreDummy}
	 */
	public LSMTripleStoreDummy() {
		sparql = ResourceBundle
				.getBundle("edu.kit.cm.ivu.smartmeetings.strings.lsmdummy");
		endpointAddress = sparql.getString("endpoint_address");
		updateAddress = sparql.getString("update_address");
		resourceNamespace = sparql.getString("resource_namespace");
		defaultDataGraph = sparql.getString("default_datagraph");
		defaultMetaGraph = sparql.getString("default_metagraph");
		prefixes = sparql.getString("prefixes");
	}

	/**
	 * Deletes all readings of the sensor with the given url
	 * 
	 * @param sensurURL
	 *            the url of the sensor
	 * @return the success status of the operation
	 */
	@Override
	public boolean deleteAllReadings(final String sensurURL) {
		executeUpdate("delete_all_readings", sensurURL);
		return true;
	}

	/**
	 * Deletes all readings of the sensor with the given url in a certain period
	 * of time. TODO: Check the actual meaning of the dateOperator, fromTime and
	 * toTime parameters when the LSM documentation is available
	 */
	@Override
	public boolean deleteAllReadings(final String sensorURL,
			final String dateOperator, final Date fromTime, final Date toTime) {
		if (fromTime != null && toTime != null) {
			executeUpdate("delete_readings_interval", sensorURL,
					fromTime.getTime(), toTime.getTime());
			return true;
		} else if (fromTime != null && dateOperator != null) {
			executeUpdate("delete_readings_compare", sensorURL, dateOperator,
					fromTime.getTime());
			return true;
		} else if (toTime != null && dateOperator != null) {
			executeUpdate("delete_readings_compare", sensorURL, dateOperator,
					toTime.getTime());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Deletes the given triples from the given graph url
	 * 
	 * @param graphURL
	 *            the graph url
	 * @param triples
	 *            the triples to be deleted
	 * @return the success status of the operation
	 */
	@Override
	public boolean deleteTriples(final String graphURL, final String triples) {
		executeUpdate("delete_triples", graphURL, triples);
		return true;
	}

	/**
	 * Deletes all triples from the given graph url
	 * 
	 * @param graphURL
	 *            the graph url
	 * @return the success status of the operation
	 */
	@Override
	public boolean deleteTriples(final String graphURL) {
		executeUpdate("delete_all_triples", graphURL);
		return true;
	}

	/**
	 * Gets the sensor with the given url
	 * 
	 * @param sensorURL
	 *            the url of the sensor
	 * @return the sensor with the given url
	 */
	@Override
	public Sensor getSensorById(final String sensorURL) {
		// get all infos and create solution set
		final ResultSet results = executeQuery(
				new SparqlSelect(endpointAddress), "get_sensor_info_by_id",
				sensorURL);
		if (results == null || !results.hasNext()) {
			return null;
		}
		final QuerySolution solution = results.nextSolution();
		if (solution == null) {
			return null;
		}
		final Sensor sensor = new Sensor();
		sensor.setId(sensorURL);

		final Literal name = solution.getLiteral("name");
		if (name != null) {
			sensor.setName(name.getString());
		}
		final Literal author = solution.getLiteral("author");
		if (author != null) {
			sensor.setAuthor(author.getString());
		}
		// TODO: Place ebenfalls abfragen
		final Literal time = solution.getLiteral("time");
		if (time != null) {
			sensor.setTimes(new Date(time.getLong()));
		}
		final Literal source = solution.getLiteral("source");
		if (source != null) {
			sensor.setSource(source.getString());
		}
		final Literal sourceType = solution.getLiteral("sourceType");
		if (sourceType != null) {
			sensor.setSourceType(sourceType.getString());
		}
		return sensor;
	}

	/**
	 * Returns the sensor with the given source
	 * 
	 * @param sensorsource
	 *            the sensor's source
	 * @return the sensor with the given source
	 */
	@Override
	public Sensor getSensorBySource(final String sensorsource) {
		final ResultSet res = executeQuery(new SparqlSelect(endpointAddress),
				"get_sensor_id_by_source", sensorsource);
		final QuerySolution solution = res.nextSolution();
		if (solution == null) {
			return null;
		}
		final Literal id = solution.getLiteral("id");
		if (id == null) {
			return null;
		} else {
			return getSensorById(id.getString());
		}
	}

	/**
	 * Adds the given sensor to the store
	 * 
	 * @param sensor
	 *            the sensor to be added
	 * @return the success status of the operation
	 */
	@Override
	public boolean sensorAdd(final Sensor sensor) {
		// create a model to store the semantic sensor data and to convert it to
		// rdf triples afterwards
		final Model model = ModelFactory.createDefaultModel();
		// creates the sensor resource and adds the type "Sensor"
		final Resource sensorRes = model.createResource(sensor.getId());
		sensorRes.addProperty(RDF.type, SSN.SENSOR);

		final String name = sensor.getName();
		if (name != null && !name.isEmpty()) {
			sensorRes.addProperty(RDFS.label, name);
		}
		final String author = sensor.getAuthor();
		if (author != null && !author.isEmpty()) {
			sensorRes.addProperty(LSM.HAS_AUTHOR, author);
		}
		final String source = sensor.getSource();
		if (source != null && !source.isEmpty()) {
			sensorRes.addProperty(LSM.HAS_SOURCE, source);
		}
		final String sourceType = sensor.getSourceType();
		if (sourceType != null && !sourceType.isEmpty()) {
			sensorRes.addProperty(LSM.HAS_SOURCE_TYPE, sourceType);
		}
		final String sensorType = sensor.getSensorType();
		if ((sensorType != null) && !sensorType.isEmpty()) {
			final Resource sensorTypeResource = model
					.createResource(sensorType);
			sensorRes.addProperty(LSM.HAS_SENSOR_TYPE, sensorTypeResource);
		}
		final Date times = sensor.getTimes();
		if (times != null) {
			final long timeVal = times.getTime();
			sensorRes.addLiteral(LSM.HAS_TIME, timeVal);
		}
		final Place place = sensor.getPlace();
		if (place != null) {
			final Resource placeResource = model.createResource(place.getId());
			sensorRes.addProperty(LSM.HAS_LOCATION, placeResource);
			// TODO: Place ebenfalls hinzufuegen
		}
		final StringWriter writer = new StringWriter();
		model.write(writer, "N-TRIPLE");

		String metaGraph = sensor.getMetaGraph();
		if (metaGraph == null || metaGraph.isEmpty()) {
			metaGraph = defaultMetaGraph;
		}
		return pushRDF(metaGraph, writer.toString());
	}

	@Override
	public boolean sensorAdd(final String triple) {
		throw new UnsupportedOperationException(
				"Not provided by this dummy implementation");
	}

	@Override
	public boolean sensorDataUpdate(final String triples) {
		throw new UnsupportedOperationException(
				"Not provided by this dummy implementation");
	}

	/**
	 * Adds new sensor data to the store
	 * 
	 * @param observation
	 *            the {@link Observation} containing the sensor data
	 * @return the success status of the operation
	 */
	@Override
	public boolean sensorDataUpdate(final Observation observation) {
		final Model model = ModelFactory.createDefaultModel();

		final String observationURI = resourceNamespace + observation.getId();
		final Resource observationRes = model.createResource(observationURI);

		observationRes.addProperty(RDF.type, SSN.OBSERVATION);

		// add the observation's metadata to the model
		final String foi = observation.getFeatureOfInterest();
		if (foi != null && !foi.isEmpty()) {
			final Resource foiRes = model.createResource(foi);
			observationRes.addProperty(SSN.FEATURE_OF_INTEREST, foiRes);
		}
		// TODO: check if sensor exists
		final String sensorID = observation.getSensor();
		if (sensorID != null && !sensorID.isEmpty()) {
			final Resource sensorRes = model.createResource(sensorID);
			observationRes.addProperty(SSN.OBSERVED_BY, sensorRes);
		}
		final Date times = observation.getTimes();
		if (times != null) {
			observationRes.addLiteral(SSN.OBSERVATION_RESULT_TIME,
					times.getTime());
		}

		// add the readings
		final List<ObservedProperty> readings = observation.getReadings();

		for (final ObservedProperty currentReading : readings) {
			// create unique id for the reading
			final String readingID = resourceNamespace + System.nanoTime();
			final Resource readingRes = model.createResource(readingID);
			readingRes.addProperty(RDF.type, SSN.PROPERTY);

			// add reference to the observation property
			readingRes.addProperty(LSM.IS_OBSERVED_PROPERTY_OF, observationRes);

			// add property name, unit and value
			final String name = currentReading.getPropertyName();
			if (name != null && !name.isEmpty()) {
				readingRes.addProperty(RDFS.label, name);
			}
			final String unitName = currentReading.getUnit();
			if (unitName != null && !unitName.isEmpty()) {
				readingRes.addProperty(LSM.UNIT, unitName);
			}
			final String valueString = currentReading.getValue();
			if (valueString != null && !valueString.isEmpty()) {
				readingRes.addProperty(LSM.VALUE, valueString);
			}
		}

		final StringWriter writer = new StringWriter();
		model.write(writer, "N-TRIPLE");
		String dataGraph = observation.getDataGraph();
		if (dataGraph == null || dataGraph.isEmpty()) {
			dataGraph = defaultDataGraph;
		}
		return pushRDF(dataGraph, writer.toString());
	}

	/**
	 * Deletes the sensor with the given url
	 * 
	 * @param sensorURL
	 *            the url of the sensor
	 * @return the success status of the operation
	 */
	@Override
	public boolean sensorDelete(final String sensorURL) {
		executeUpdate("delete_sensor", sensorURL);
		return true;
	}

	/**
	 * Uploads the given triples to the given graph url
	 * 
	 * @param graphURL
	 *            the graph url
	 * @param triples
	 *            the triples to be uploaded
	 * @return the success status of the operation
	 */
	@Override
	public boolean pushRDF(final String graphURL, final String triples) {
		executeUpdate("insert_triples", graphURL, triples);
		return true;
	}

	/**
	 * Executes the query with the given Sparql Engine and the given query
	 * parameters.
	 * 
	 * @param engine
	 *            the Sparql Query Engine to use.
	 * @param queryId
	 *            The id of the query.
	 * @param parameters
	 *            Additional parameters for the query that are injected with
	 *            String.format(query, queryParameters)
	 * @return The retrived result of the query.
	 * 
	 * @author Andreas Eberle
	 * @author Valentin Zickner
	 * @author David Kulicke
	 */
	private <Result> Result executeQuery(final SparqlQuery<Result> engine,
			final String queryId, final Object... parameters) {
		final String queryString = prefixes.concat(sparql.getString(queryId));

		LOG.fine("Query to execute without parameters: " + queryString);

		// Execute SPARQL-Query
		final String query = String.format(queryString, parameters);
		LOG.info("executing query: \"" + queryId + "\": " + query);

		return engine.execute(query);
	}

	private void executeUpdate(final String queryId, final Object... parameters) {
		executeQuery(new SparqlUpdate(updateAddress), queryId, parameters);
	}
}
