package edu.kit.cm.ivu.smartdiscussions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.jdo.PersistenceManager;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 * @author Kirill Rakhman
 */
public class PushHelper {

	private PushHelper() {
	}

	private static final Gson gson = new Gson();
	private static final String GCM_URL = "https://android.googleapis.com/gcm/send";
	private static final String API_KEY = "AIzaSyCbPtJdCjLdZdVoDZ4EBfnGGgsqM4dC9E8";

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

	/**
	 * Analyses the result from GCM and deletes the ids that are no longer
	 * valid.
	 * 
	 * @param result
	 *            Result
	 * @param registrations
	 *            List of registrations that were used to send the push message
	 * @param ids
	 *            array of registration ids (must accord to registrations)
	 * @throws JSONException
	 */
	private static void handleGCMResult(final JSONObject result,
			final List<String> registrations,
			final Map<String, UserInfo> idUsers) throws JSONException {
		final int failures = result.optInt("failure", 0);
		final int canonical = result.optInt("canonical_ids", 0);

		if (failures == 0 && canonical == 0) {
			return;
		}

		final JSONArray resArr = result.getJSONArray("results");
		for (int i = 0; i < resArr.length(); i++) {
			final JSONObject res = resArr.getJSONObject(i);

			if (res.optString("message_id", null) != null) {
				final String registration_id = res.optString("registration_id",
						null);

				if (registration_id != null) {
					// id exists with two names

					final String wrongId = registrations.get(i);
					final List<String> pushIds = idUsers.get(wrongId)
							.getPushIds();

					// remove wrong one
					pushIds.remove(wrongId);

					if (!pushIds.contains(registration_id)) {
						// add correct one
						pushIds.add(registration_id);
					}
				}

			} else {
				final String error = res.optString("error", null);
				if (error != null) {
					final String errorId = registrations.get(i);
					final List<String> pushIds = idUsers.get(errorId)
							.getPushIds();

					switch (error) {
					case "Unavailable":
						// do nothing, alternatively try again
						break;
					case "NotRegistered":
						// remove
						pushIds.remove(errorId);
						break;
					default:
						// remove
						pushIds.remove(errorId);
						break;
					}
				}

			}
		}

	}

	private static void pushToGcm(final JsonObject data,
			final String collapse_key, final List<String> registrationIds,
			final Map<String, UserInfo> idUsers) {
		if (registrationIds.isEmpty()) {
			return;
		}

		final String[] ids = registrationIds.toArray(new String[0]);

		try {
			final JsonObject json = new JsonObject();
			json.add("registration_ids", gson.toJsonTree(ids));
			json.addProperty("collapse_key", collapse_key);

			json.add("data", data);

			// HTTP
			final HttpURLConnection conn = (HttpURLConnection) new URL(GCM_URL)
					.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "key=" + API_KEY);
			conn.setRequestProperty("Content-Type", "application/json");

			System.out.println(json.toString());

			final OutputStream out = conn.getOutputStream();
			out.write(json.toString().getBytes());
			out.flush();
			out.close();

			System.out.println(conn.getResponseMessage());
			final String responseJsonString = readStream(conn.getInputStream());
			System.out.println(responseJsonString);

			final JSONObject responseJson = new JSONObject(responseJsonString);
			handleGCMResult(responseJson, registrationIds, idUsers);

		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	private static String readStream(final InputStream inputStream) {
		final Scanner s = new Scanner(inputStream).useDelimiter("\\A");
		final String responseJsonString = s.next();
		return responseJsonString;
	}

	public static void pushNewPost(final PostContainer post,
			final Discussion discussion) {
		final JsonObject pushJson = new JsonObject();
		pushJson.addProperty("discussionName", discussion.getPublicName());
		pushJson.addProperty("discussionId", discussion.getTopic());
		pushJson.addProperty("userName", post.getUserName());
		pushJson.addProperty("postText", post.getText());
		pushJson.addProperty("date", String.valueOf(post.getDate().getTime()));
		pushJson.addProperty("postId", String.valueOf(post.getId()));

		final List<String> registrationIds = new ArrayList<>();
		final Map<String, UserInfo> idUsers = new HashMap<>();

		final PersistenceManager pm = getPersistenceManager();

		for (final Long userId : discussion.getUserIds()) {
			final UserInfo userInfo = pm.getObjectById(UserInfo.class, userId);

			for (final String pushId : userInfo.getPushIds()) {
				registrationIds.add(pushId);
				idUsers.put(pushId, userInfo);
			}
		}

		pushToGcm(pushJson, String.valueOf(post.getId()), registrationIds,
				idUsers);
	}
}
