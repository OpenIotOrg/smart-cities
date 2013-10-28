package edu.kit.cm.ivu.smartmeetings.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import edu.kit.cm.ivu.smartdiscussions.Ids;
import edu.kit.cm.ivu.smartdiscussions.PMF;
import edu.kit.cm.ivu.smartdiscussions.UserInfo;

//
/**
 * 
 * @author Kirill Rakhman
 */
@Api(name = "smartmeetings", version = "v1", clientIds = {
		Ids.ANDROID_CLIENT_ID1, Ids.ANDROID_CLIENT_ID2, Ids.ANDROID_CLIENT_ID3,
		Ids.ANDROID_CLIENT_ID4, Ids.ANDROID_CLIENT_ID5, Ids.ANDROID_CLIENT_ID6,
		Ids.WEB_CLIENT_ID,
		com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID }, audiences = { Ids.ANDROID_AUDIENCE })
public class SmartMeetingsBackendService {
	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

	private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";

	@ApiMethod(name = "backend.setUsername", path = "discussions/setUserName/{token}")
	public UserInfo setUsername(@Named("token") final String token,
			final User user) throws MalformedURLException, IOException,
			JSONException, OAuthRequestException {

		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		InputStream inputStream;
		final URLConnection conn = new URL(USER_INFO_URL + token)
				.openConnection();

		inputStream = conn.getInputStream();
		final java.util.Scanner s = new java.util.Scanner(inputStream)
				.useDelimiter("\\A");
		final String jsonResult = s.next();
		s.close();
		inputStream.close();

		final JSONObject jsonObject = new JSONObject(jsonResult);

		final String userName = jsonObject.optString("given_name") + " "
				+ jsonObject.optString("family_name");

		// TODO pr�fen ob username leer ist

		final PersistenceManager pm = getPersistenceManager();
		final UserInfo userInfo = getUserInfo(user, pm);

		userInfo.setUserName(userName);
		pm.close();

		return userInfo;

	}

	@ApiMethod(name = "backend.setPushId", path = "discussions/setPushId/")
	public void setPushId(@Named("pushId") final String pushId, final User user)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		final PersistenceManager pm = getPersistenceManager();

		final UserInfo userInfo = getUserInfo(user, pm);
		List<String> pushIds = userInfo.getPushIds();

		if (pushId == null) {
			pushIds = new ArrayList<>();
			userInfo.setPushIds(pushIds);
		}

		pushIds.add(pushId);

		pm.close();
	}

	/**
	 * 
	 * Gets the {@link UserInfo} object associated to the given user from the
	 * data store.
	 * 
	 * @param user
	 *            User
	 * @param pm
	 *            {@link PersistenceManager}
	 * @return The {@link UserInfo} object stored. If none exists, one is
	 *         created and stored.
	 */
	@SuppressWarnings("unchecked")
	private static UserInfo getUserInfo(final User user,
			final PersistenceManager pm) {
		final Query query = pm.newQuery(UserInfo.class);

		query.setFilter("user == userParam");
		query.declareParameters("com.google.appengine.api.users.User userParam");

		final List<UserInfo> result = (List<UserInfo>) pm.newQuery(query)
				.execute(user);

		UserInfo userInfo;
		if (!result.isEmpty()) {
			userInfo = result.get(0);
		} else {
			userInfo = new UserInfo(user, null, new HashSet<String>());
			pm.makePersistent(userInfo);
		}
		return userInfo;
	}

}
