package edu.kit.cm.ivu.smartmeetings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import edu.kit.cm.ivu.smartmeetings.ui.GetTopicInfoWorker;

/**
 * 
 * @author Kirill Rakhman
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context c, final Intent intent) {
		Log.v(getClass().getSimpleName(), "Received push message");

		final Bundle extras = intent.getExtras();
		final String discussionId = extras.getString("discussionId");

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);

		final boolean isCurrentDiscussions = discussionId.equals(prefs
				.getString("currentDiscussion", ""));
		final boolean notificationsEnabled = prefs.getBoolean("notifications:"
				+ discussionId, prefs.getBoolean("pref_notifications", true));

		if (!isCurrentDiscussions && notificationsEnabled) {

			final String userName = extras.getString("userName");
			final String postText = extras.getString("postText");
			final String discussionName = extras.getString("discussionName");

			// Basic notification
			final Builder builder = new NotificationCompat.Builder(c);
			builder.setSmallIcon(R.drawable.ic_stat_notify).setContentTitle(
					c.getString(R.string.notification_new_post_title,
							discussionName));

			// Preview post
			final String text = userName + ": " + postText;
			builder.setContentText(text);

			// Activity Intent
			final Intent resultIntent = new Intent(c,
					SmartMeetingsStartActivity.class);
			resultIntent.setAction(Intent.ACTION_VIEW);
			resultIntent.setData(Uri.parse(GetTopicInfoWorker.getDiscussionUri(
					discussionId, discussionName)));

			// PendingIntent pendingIntent = PendingIntent.getActivity(c, 0,
			// resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			final PendingIntent pendingIntent = PendingIntent.getActivity(c, 0,
					resultIntent, 0);
			builder.setContentIntent(pendingIntent);

			// Sound & Vibration
			final String soundUriString = prefs.getString(
					"pref_notificationSound", null);
			if (soundUriString != null) {
				builder.setSound(Uri.parse(soundUriString));
			}

			if (prefs.getBoolean("pref_vibrate", false)) {
				builder.setVibrate(new long[] { 0L, 500L });
			}

			// Notify!
			final NotificationManager mNotificationManager = (NotificationManager) c
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(getNotificationId(), builder.build());

		}
	}

	/**
	 * Could be used later to create different notifications for different
	 * discussions
	 * 
	 * @return An id.
	 */
	private static int getNotificationId() {
		return 0;
	}
}
