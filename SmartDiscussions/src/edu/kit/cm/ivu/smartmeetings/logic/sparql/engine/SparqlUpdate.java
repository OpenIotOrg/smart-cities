package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

import com.hp.hpl.jena.update.UpdateProcessor;

//TODO: Remove when replaced by LSM Request
public class SparqlUpdate extends SparqlQuery<Void> {

	public SparqlUpdate(final String endpointAddress) {
		super(endpointAddress);
	}

	/**
	 * Execute SPARQL Update Query on the Endpoint Update Address.
	 * 
	 * @param query
	 *            Query String to execute
	 */
	@Override
	public Void execute(final String query) {

		// UpdateRequest request = UpdateFactory.create(query);
		final UpdateRequest request = new UpdateRequest(query);
		final UpdateProcessor update = new UpdateProcessRemoteForm(request,
				this.endpointAddress);
		update.execute();
		return null;
	}
}