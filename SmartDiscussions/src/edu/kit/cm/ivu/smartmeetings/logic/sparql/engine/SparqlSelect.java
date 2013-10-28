package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Class to execute SPARQL-Querys
 * 
 * @author Valentin Zickner
 */
public class SparqlSelect extends SparqlQuery<ResultSet> {

	private static final Logger LOG = Logger.getLogger("SmartDiscussions");

	public SparqlSelect(final String endpointAddress) {
		super(endpointAddress);
	}

	/**
	 * Execute SPARQL SELECT-Query on an Endpoint Address.
	 * 
	 * @param queryString
	 *            SPARQL SELECT-Query to execute.
	 * @return ResultSet Result of SPARQL SELECT query.
	 */
	@Override
	public ResultSet execute(final String queryString) {
		ResultSet resultSet;
		Query query;
		try {
			query = QueryFactory.create(queryString);
		} catch (final JenaException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
			return null;
		}

		final QueryEngineHTTP engine = new QueryEngineHTTP(
				this.endpointAddress, query);

		try {
			resultSet = engine.execSelect();
		} catch (final JenaException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
			return null;
		} catch (final NumberFormatException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
			return null;
		} finally {
			engine.close();
		}

		return resultSet;
	}
}
