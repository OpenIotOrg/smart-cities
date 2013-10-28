package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

/**
 * Create a UpdateRequest with request name, similar to
 * com.hp.hpl.jena.update.UpdateRequest, but only for textual requests.
 * 
 * Created because Androjena UpdateRequest destroy update queries.
 * 
 * @author Valentin Zickner
 */
public class UpdateRequest {
	/**
	 * Request store
	 */
	private String request = "";

	/**
	 * Create new request with SPARQL-String
	 * 
	 * @param request
	 *            Request to store.
	 */
	public UpdateRequest(final String request) {
		this.request = request;
	}

	/**
	 * Get the current request.
	 */
	@Override
	public String toString() {
		return this.request;
	}
}
