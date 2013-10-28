package edu.kit.cm.ivu.smartmeetings.lsm_dummy.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class LSM {

	private static final String LSM_NAMESPACE = "http://lsm.deri.ie/ont/lsm.owl#";

	public static final Property UNIT = createProperty("unit");
	public static final Property VALUE = createProperty("value");
	public static final Property HAS_LOCATION = createProperty("hasLocation");
	public static final Property HAS_AUTHOR = createProperty("hasAuthor");
	public static final Property HAS_SOURCE = createProperty("hasSource");
	public static final Property HAS_SOURCE_TYPE = createProperty("hasSourceType");
	public static final Property HAS_SENSOR_TYPE = createProperty("hasSensorType");
	public static final Property HAS_TIME = createProperty("hasTime");
	public static final Property IS_OBSERVED_PROPERTY_OF = createProperty("isObservedPropertyOf");
	public static final Property HAS_STREET = createProperty("hasStreet");
	public static final Property HAS_CITY = createProperty("hasCity");
	public static final Property HAS_ZIPCODE = createProperty("hasZipcode");

	private LSM() {

	}

	private static Property createProperty(final String name) {
		return ResourceFactory.createProperty(LSM_NAMESPACE, name);
	}
}
