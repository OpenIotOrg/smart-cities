package edu.kit.cm.ivu.smartmeetings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.MenuItem;

/**
 * An activity which displays settings for the app.
 * 
 * @author Kirill Rakhman
 */
public class PreferenceActivity extends android.preference.PreferenceActivity
		implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private static final int REQUEST_PICK_RINGTONE = 5;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		setPreferenceListeners(getPreferenceScreen());

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Sets this activity as the listener for all preference changes and clicks
	 * of the given {@link PreferenceGroup} recursively.
	 * 
	 * @param prefGroup
	 */
	private void setPreferenceListeners(final PreferenceGroup prefGroup) {
		final int count = prefGroup.getPreferenceCount();
		for (int i = 0; i < count; i++) {
			final Preference pref = prefGroup.getPreference(i);

			if (pref instanceof PreferenceGroup) {
				setPreferenceListeners((PreferenceGroup) pref);
			} else {
				pref.setOnPreferenceChangeListener(this);
				pref.setOnPreferenceClickListener(this);
			}
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		final String key = pref.getKey();

		if (key.equals("pref_notificationSound")) {
			final SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			final Intent intent = new Intent(
					RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_NOTIFICATION);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
					getString(R.string.pref_notificationSound_intent));

			// Alten Klingelton suchen
			final Uri defaultSound = RingtoneManager
					.getActualDefaultRingtoneUri(this,
							RingtoneManager.TYPE_NOTIFICATION);
			final String oldUri = prefs.getString("pref_notificationSound",
					null);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					oldUri != null ? Uri.parse(oldUri) : defaultSound);

			this.startActivityForResult(intent, REQUEST_PICK_RINGTONE);
			return true;
		}

		return true;
	}

	@Override
	public boolean onPreferenceChange(final Preference preference,
			final Object newValue) {
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		if (requestCode == REQUEST_PICK_RINGTONE
				&& resultCode == Activity.RESULT_OK) {
			// Notification sound was chosen
			final Uri uri = intent
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

			String chosenRingtone = null;
			if (uri != null) {
				chosenRingtone = uri.toString();
			}

			final Editor editor = PreferenceManager
					.getDefaultSharedPreferences(this).edit();
			editor.putString("pref_notificationSound", chosenRingtone);
			editor.commit();
			return;
		}

	}
}
