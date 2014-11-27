/*
 * Copyright (c) 2011-2014, OpenIoT
 *
 * This file is part of OpenIoT.
 *
 * OpenIoT is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * OpenIoT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenIoT. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: OpenIoT mailto: info@openiot.eu
 *
 */
package org.openiot.gsn.wrappers.tcplistener;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.OperationNotSupportedException;
import org.openiot.gsn.beans.DataField;
import org.openiot.gsn.beans.DataTypes;
import org.openiot.gsn.beans.InputStream;
import org.openiot.gsn.beans.StreamElement;
import org.openiot.gsn.beans.StreamSource;
import org.openiot.gsn.beans.VSensorConfig;
import org.openiot.gsn.metadata.LSM.LSMFieldMetaData;
import org.openiot.gsn.metadata.LSM.LSMRepository;
import org.openiot.gsn.metadata.LSM.LSMSensorMetaData;
import org.openiot.gsn.metadata.LSM.SensorAnnotator;
import org.openiot.gsn.vsensor.AbstractVirtualSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combines the BridgeVirtualSensor and LSMExporter with web interaction.
 *
 * @author Hylke van der Schaaf
 */
public class BridgeLsmWebVS extends AbstractVirtualSensor {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BridgeLsmWebVS.class);

	private final List<String> fields = new ArrayList<>();
	private String sensorName;
	private boolean allow_nulls = false;
	private boolean publish_to_lsm = false;
	private final Map<String, String> fieldUris = new HashMap<>();

	@Override
	public boolean initialize() {
		LSMSensorMetaData metadata;
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		try {
			metadata = LSMRepository.getInstance().loadMetadata(vsensor);
		} catch (FileNotFoundException e) {
			LOGGER.error("No LSM metadata available for loading vsensor {}", vsensor.getName(), e);
			return false;
		}

		TreeMap<String, String> params = vsensor.getMainClassInitialParams();
		sensorName = vsensor.getName();

		LOGGER.info("Sensor has {} outputfields.", vsensor.getOutputStructure().length);
		for (DataField df : vsensor.getOutputStructure()) {
			LOGGER.info("Property: {}--{}", df.getName(), df.getProperty());
			if (df.getProperty() != null) {
				fieldUris.put(df.getName().toUpperCase(), df.getProperty());
			} else {
				for (LSMFieldMetaData md : metadata.getFields().values()) {
					if (md.getGsnFieldName().equals(df.getName())) {
						fieldUris.put(df.getName().toUpperCase(), md.getLsmPropertyName());
					}
				}
			}
		}

		String allow_nulls_str = params.get("allow-nulls");
		if (allow_nulls_str != null) {
			allow_nulls = allow_nulls_str.equalsIgnoreCase("true");
		}

		LOGGER.info("Allow nulls: {}.", allow_nulls);

		String publishLsmStr = params.get("publish-to-lsm");
		if (publishLsmStr != null) {
			publish_to_lsm = publishLsmStr.equalsIgnoreCase("true");
		}

		if (publish_to_lsm) {
			try {
				loadMetadata(vsensor);
			} catch (Exception e) {
				LOGGER.error("Could not load vsensor LSM metadata for {}.", vsensor.getName());
				LOGGER.error("", e);
				return false;
			}
		}

		// for each field in output structure
		for (int i = 0; i < vsensor.getOutputStructure().length; i++) {
			fields.add(vsensor.getOutputStructure()[i].getName());
			LOGGER.info(fields.get(i));
		}

		return true;
	}

	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		if (allow_nulls) {
			dataProduced(data);
		} else {
			if (isAnyFieldNull(data)) {
				LOGGER.debug("Nulls received for timestamp {}, discarded. Data: {}.", data.getTimeStamp(), data);
			} else {
				dataProduced(data);
			}
		}

		if (publish_to_lsm) {
			Long t = data.getTimeStamp();
			Date d = new Date(t);
			for (int i = 0; i < fields.size(); i++) {
				String field = fields.get(i);
				Object val;
				if (data.getFieldTypes()[i].equals(DataTypes.VARCHAR)) {
					val = (String) data.getData(field);
				} else {
					val = (Double) data.getData(field);
				}
				String fieldName = data.getFieldNames()[i];
				LOGGER.debug("{} : t={} v={}", fieldName, d, val);

				if (val == null) {
					// Not sending null values to LSM.
					continue;
				}
				SensorAnnotator.updateSensorDataOnLSM(sensorName, fieldName, fieldUris.get(fieldName), val, d);
			}
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean dataFromWeb(String command, String[] paramNames, Serializable[] paramValues) {
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		boolean ok;

		for (InputStream is : vsensor.getInputStreams()) {
			for (StreamSource s : is.getSources()) {
				try {
					LOGGER.debug("Trying stream {}, source {}.", is, s.getAlias());
					ok = s.getWrapper().sendToWrapper(command, paramNames, paramValues);
					if (ok) {
						return true;
					}
				} catch (OperationNotSupportedException e) {
				}
			}
		}
		LOGGER.warn("The virtual sensor '{}' wants to send data to a stream source which doesn't support receiving data.", vsensor.getName());
		return false;
	}

	private LSMSensorMetaData loadMetadata(VSensorConfig vsConfig) throws Exception {
		LSMRepository lsm = LSMRepository.getInstance();
		return lsm.loadMetadata(vsConfig);
	}

	private boolean isAnyFieldNull(StreamElement data) {
		for (Serializable d : data.getData()) {
			if (d == null) {
				return true;
			}
		}
		return false;
	}
}
