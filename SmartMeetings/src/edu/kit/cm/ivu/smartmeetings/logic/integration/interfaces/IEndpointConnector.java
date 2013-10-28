package edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces;

import com.google.api.services.smartmeetings.Smartmeetings;

/**
 * Interface providing access to methods relating to the cloud endpoint.
 * 
 * @author Kirill Rakhman
 */
public interface IEndpointConnector {

	/**
	 * Send an auth token to the endpoint which will retrieve and save the full
	 * name of the current user.
	 * 
	 * @param token
	 *            Auth token for the scope
	 *            <code>oauth2:https://www.googleapis.com/auth/userinfo.profile</code>
	 * @return The full user name
	 */
	public String setUsername(String token);

	public String getUserId();

	public String getUserName();

	public void setPushId(String pushId);

	public Smartmeetings getService();
}
