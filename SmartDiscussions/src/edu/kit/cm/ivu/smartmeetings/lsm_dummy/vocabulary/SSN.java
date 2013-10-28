package edu.kit.cm.ivu.smartmeetings.lsm_dummy.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class SSN {

	private static final String SSN_NAMESPACE = "http://purl.oclc.org/NET/ssnx/ssn#";

	public static final Property IS_PROPERTY_OF = createProperty("isPropertyOf");
	public static final Property OBSERVED_BY = createProperty("observedBy");
	public static final Property OBSERVATION_RESULT_TIME = createProperty("observationResultTime");
	public static final Property FEATURE_OF_INTEREST = createProperty("featureOfInterest");

	public static final Resource SENSOR = createResource("Sensor");
	public static final Resource OBSERVATION = createResource("Observation");
	public static final Resource PROPERTY = createResource("Property");

	private SSN() {

	}

	private static Property createProperty(final String name) {
		return ResourceFactory.createProperty(SSN_NAMESPACE, name);
	}

	private static Resource createResource(final String name) {
		return ResourceFactory.createProperty(SSN_NAMESPACE, name);
	}
}
