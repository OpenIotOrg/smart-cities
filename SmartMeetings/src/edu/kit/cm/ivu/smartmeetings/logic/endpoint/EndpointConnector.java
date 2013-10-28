package edu.kit.cm.ivu.smartmeetings.logic.endpoint;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.smartmeetings.Smartmeetings;
import com.google.api.services.smartmeetings.model.UserInfo;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;

/**
 * 
 * @author Kirill Rakhman
 */
public class EndpointConnector implements IEndpointConnector {
	// public static final String CLIENT_ID =
	// "125113308430-tad0lbo9igbqhu73vmtl2hda5ve3fq9i.apps.googleusercontent.com";
	public static final String CLIENT_ID = "826403670175-59nlp42fst6ph2o2kvgv9j9e2bri3fdo.apps.googleusercontent.com";
	private final Context context;
	private String userName;
	private String userId;
	private final Smartmeetings service;

	public EndpointConnector(final Context c) {
		context = c;

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String account = prefs.getString("account", "");

		GoogleAccountCredential credential = null;
		if (!account.isEmpty()) {
			credential = GoogleAccountCredential.usingAudience(context,
					"server:client_id:" + CLIENT_ID);
			credential.setSelectedAccountName(account);
		}

		final Smartmeetings.Builder builder = new Smartmeetings.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(),
				credential);
		service = builder.build();

		userName = prefs.getString("account_userName", account);
		userId = prefs.getString("account_userId", account);
	}

	@Override
	public String setUsername(final String token) {
		Log.d("SmartDiscussionsConnector", "Setting user name");
		String username = null;
		if (!token.isEmpty()) {
			try {
				final UserInfo userInfo = service.backend().setUsername(token)
						.execute();
				username = userInfo.getUserName();
				final String userId = userInfo.getUser().getEmail();

				Log.d("SmartDiscussionsConnector", "User name set: " + username
						+ " (#" + userId + ")");

				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				final Editor edit = prefs.edit();
				edit.putString("account_userName", username);
				edit.putString("account_userId", userId);
				edit.commit();

				userName = username;
				this.userId = userId;

			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return username;
	}

	@Override
	public void setPushId(final String pushId) {
		try {
			service.backend().setPushId(pushId).execute();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public Smartmeetings getService() {
		return service;
	}
}
