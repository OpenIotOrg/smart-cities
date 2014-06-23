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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import javax.naming.OperationNotSupportedException;
import org.openiot.gsn.beans.InputStream;
import org.openiot.gsn.beans.StreamElement;
import org.openiot.gsn.beans.StreamSource;
import org.openiot.gsn.beans.VSensorConfig;
import org.openiot.gsn.metadata.LSM.LSMRepository;
import org.openiot.gsn.metadata.LSM.LSMSensorMetaData;
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

	@Override
	public boolean initialize() {
		VSensorConfig vsensor = getVirtualSensorConfiguration();

		TreeMap<String, String> params = vsensor.getMainClassInitialParams();
		sensorName = vsensor.getName();

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
			if (!areAnyFieldsNull(data)) {
				dataProduced(data);
			} else {
				LOGGER.debug("Nulls received for timestamp {}, discarded", data.getTimeStamp());
			}
		}

		Long t = data.getTimeStamp();
		for (int i = 0; i < fields.size(); i++) {
			String field = fields.get(i);
			Double v = (Double) data.getData(field);
			Date d = new Date(t);
			String fieldName = data.getFieldNames()[i];
			LOGGER.debug(fieldName + " : t=" + d + " v=" + v);

			if (!allow_nulls && v == null) {
				continue; // skipping null values if allow_nulls flag is not st to true
			}
			if (publish_to_lsm) {
				LSMRepository.getInstance().publishSensorDataToLSM(sensorName, fieldName, v, d);
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

	public boolean areAnyFieldsNull(StreamElement data) {
		boolean allFieldsNull = false;
		for (Serializable d : data.getData()) {
			if (d == null) {
				allFieldsNull = true;
				break;
			}
		}

		return allFieldsNull;
	}
}
