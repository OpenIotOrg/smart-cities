package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Class to execute SPARQL ASK-Querys
 * 
 * @author Valentin Zickner
 */
public class SparqlAsk extends SparqlQuery<Boolean> {

	private static final Logger LOG = Logger.getLogger("SmartDiscussions");

	public SparqlAsk(final String endpointAddress) {
		super(endpointAddress);
	}

	/**
	 * Execute SPARQL ASK-Query on an Endpoint Address.
	 * 
	 * @param queryString
	 *            SPARQL ASK-Query to execute.
	 * @return Result of SPARQL ASK query.
	 */
	@Override
	public Boolean execute(final String queryString) {
		Query query;
		try {
			query = QueryFactory.create(queryString);
		} catch (final JenaException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}

		final QueryEngineHTTP engine = new QueryEngineHTTP(
				this.endpointAddress, query);

		boolean result;
		try {
			result = engine.execAsk();
		} catch (final JenaException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (final NumberFormatException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} finally {
			engine.close();
		}

		return result;
	}
}
