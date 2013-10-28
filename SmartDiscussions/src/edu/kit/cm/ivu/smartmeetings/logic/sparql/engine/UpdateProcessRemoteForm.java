/**
 * UpdateProcessRemoteForm class original from Apache Jena Project
 * not porteted to androjena by androjena developer team.
 * 
 * Orginal package: com.hp.hpl.jena.sparql.modify
 * 
 * @author Valentin Zickner
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.cm.ivu.smartmeetings.logic.sparql.engine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;

/**
 * UpdateProcess that send the request to a SPARQL endpoint by using an HTML
 * form and POST.
 * 
 * @author Valentin Zickner
 */
public class UpdateProcessRemoteForm implements UpdateProcessor {

	private static final Logger log = Logger.getLogger("SmartDiscussions");

	/**
	 * Update request to execute.
	 */
	private final UpdateRequest request;

	/**
	 * Endpoint to use for execution
	 */
	private final String endpoint;

	public UpdateProcessRemoteForm(final UpdateRequest request,
			final String endpoint) {
		this.request = request;
		this.endpoint = endpoint;
	}

	public void setInitialBinding(final QuerySolution binding) {
		throw new ARQException(
				"Initial bindings for a remote update execution request not supported");
	}

	@Override
	public GraphStore getGraphStore() {
		return null;
	}

	/**
	 * Execute a SPARQL-Query on an remote host by using Form-Attributes.
	 */
	@Override
	public void execute() {
		if (this.endpoint == null) {
			throw new ARQException("Null endpoint for remote update by form");
		}
		final String requestString = this.request.toString();

		// Format the content of the query
		log.fine("Query to execute: " + requestString);

		String query = "";
		try {
			query = "update=" + URLEncoder.encode(requestString, "utf-8");
		} catch (final UnsupportedEncodingException e) {
			log.warning("Execution Query failed with UnsupportedEncodingException: "
					+ e.getMessage());
			return;
		}

		log.fine("Query url encoded: " + query);

		// Start connection
		try {
			final URL endpointUrl = new URL(this.endpoint);

			final HttpURLConnection urlConnection = (HttpURLConnection) endpointUrl
					.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");

			urlConnection.setUseCaches(false);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			// Set body of query
			final DataOutputStream wr = new DataOutputStream(
					urlConnection.getOutputStream());
			wr.writeBytes(query);
			wr.flush();
			wr.close();

			// Get result and print in log
			InputStream is = null;
			boolean isError = false;
			try {
				is = urlConnection.getInputStream();
			} catch (final IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
				// Use error result instead for debugging purpose.
				is = urlConnection.getErrorStream();
				isError = true;
			}

			final BufferedReader rd = new BufferedReader(new InputStreamReader(
					is));
			String line;
			final StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			final String executionOutput = response.toString();
			if (executionOutput != "" && isError) {
				log.severe(executionOutput);
			} else {
				log.fine("Output of query execution: " + executionOutput);
			}

			urlConnection.disconnect();
		} catch (final MalformedURLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (final IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}
}
