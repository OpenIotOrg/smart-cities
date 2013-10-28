package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

public abstract class SparqlQuery<Result> {
	/**
	 * Address of SPARQL-Endpoint
	 */
	protected String endpointAddress;

	/**
	 * Create new SparqlQuery with endpoint address.
	 * 
	 * @param endpointAddress
	 *            Address of sparql endpoint
	 */
	public SparqlQuery(final String endpointAddress) {
		setEndpointAddress(endpointAddress);
	}

	/**
	 * Set address of SPARQL-Endpoint
	 * 
	 * @param endpointAddress
	 *            Endpoint address to use
	 */
	public void setEndpointAddress(final String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	/**
	 * Get address of SPARQL-Endpoint
	 * 
	 * @return string
	 */
	public String getEndpointAddress() {
		return this.endpointAddress;
	}

	public abstract Result execute(String query);
}
