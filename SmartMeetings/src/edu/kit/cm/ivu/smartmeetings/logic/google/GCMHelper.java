package edu.kit.cm.ivu.smartmeetings.logic.google;

import java.io.IOException;
import java.sql.Timestamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.kit.cm.ivu.smartmeetings.BuildConfig;
import edu.kit.cm.ivu.smartmeetings.ui.IFrontend;

/**
 * Helper class which handles the registration of this client with GCM and
 * initiated the registration with the backend.
 * 
 * @author Kirill Rakhman
 */
public class GCMHelper {

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

	/**
	 * Default lifespan (7 days) of a reservation until it is considered
	 * expired.
	 */
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	private static final String SENDER_ID = "826403670175";

	/**
	 * Tag used on log messages.
	 */
	private static final String TAG = GCMHelper.class.getSimpleName();

	private final GoogleCloudMessaging gcm;
	private final IFrontend frontend;
	private final Context context;
	private String regid;

	public GCMHelper(final IFrontend frontend) {
		this.frontend = frontend;
		context = frontend.getContext().getApplicationContext();

		regid = getRegistrationId(context);

		gcm = GoogleCloudMessaging.getInstance(context);

		// debug versions need to get a new id every time
		if (regid.length() == 0 || BuildConfig.DEBUG) {
			registerBackground();
		}

	}

	public String getRegistrationId() {
		return regid;
	}

	/**
	 * Gets the current registration id for application on GCM service.
	 * <p>
	 * If result is empty, the registration has failed.
	 * 
	 * @return registration id, or empty string if the registration is not
	 *         complete.
	 */
	private String getRegistrationId(final Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		final String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Log.v(TAG, "Registration not found.");
			return "";
		}
		// check if app was updated; if so, it must clear registration id to
		// avoid a race condition if GCM sends a message
		final int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		final int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion || isRegistrationExpired()) {
			Log.v(TAG, "App version changed or registration expired.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGCMPreferences(final Context context) {
		return context.getSharedPreferences(GCMHelper.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(final Context context) {
		try {
			final PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (final NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Checks if the registration has expired.
	 * 
	 * <p>
	 * To avoid the scenario where the device sends the registration to the
	 * server but the server loses it, the app developer may choose to
	 * re-register after REGISTRATION_EXPIRY_TIME_MS.
	 * 
	 * @return true if the registration has expired.
	 */
	private boolean isRegistrationExpired() {
		final SharedPreferences prefs = getGCMPreferences(context);
		// checks if the information is not stale
		final long expirationTime = prefs.getLong(
				PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
		return System.currentTimeMillis() > expirationTime;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's shared preferences.
	 */
	private void registerBackground() {

		Log.v(getClass().getSimpleName(), "Registering new GCM ID");
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(final Void... params) {
				try {
					regid = gcm.register(SENDER_ID);
					setRegistrationId(context, regid);
					frontend.createLogicFacade().setPushId(regid);

					return true;
				} catch (final IOException ex) {
					ex.printStackTrace();
					return false;
				}
			}

			@Override
			protected void onPostExecute(final Boolean success) {
				Log.v(TAG, "Registering GCM success: " + success);
			}
		}.execute();
	}

	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration id
	 */
	private static void setRegistrationId(final Context context,
			final String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		final int appVersion = getAppVersion(context);
		Log.v(TAG, "Saving regId on app version " + appVersion);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		final long expirationTime = System.currentTimeMillis()
				+ REGISTRATION_EXPIRY_TIME_MS;

		Log.v(TAG, "Setting registration expiry time to "
				+ new Timestamp(expirationTime));
		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}

}
