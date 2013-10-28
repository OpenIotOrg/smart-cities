package edu.kit.cm.ivu.smartmeetings.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.PostContainer;

import edu.kit.cm.ivu.smartmeetings.R;

public class DiscussionPostAdapter extends MyListAdapter<PostContainer> {

	private final DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
	private final String userId;
	private final Pattern roomPattern = Pattern
			.compile("smartmeetings://.+/.+");

	public DiscussionPostAdapter(final Context context,
			final List<PostContainer> objects) {
		super(context, objects);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		userId = prefs.getString("account_userId", "");
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final LinearLayout layout = (LinearLayout) view;
		final TextView tvPoster = (TextView) layout
				.findViewById(R.id.textViewPosterName);
		final TextView tvText = (TextView) layout
				.findViewById(R.id.textViewPostText);
		final TextView tvDate = (TextView) layout
				.findViewById(R.id.textViewPostDate);

		final PostContainer post = getItem(position);

		tvPoster.setText(post.getUserName());
		tvText.setText(post.getText());
		Linkify.addLinks(tvText, roomPattern, "smartmeetings://");
		tvDate.setText(sdf.format(new Date(post.getDate().getValue())));
		// TODO datum nicht auf dem hauptthread formatieren

		boolean myPost = false;
		if (userId != null && post.getUserId() != null) {
			myPost = post.getUserId().equals(userId);
		}
		final int gravity = myPost ? Gravity.RIGHT : Gravity.LEFT;
		layout.setGravity(gravity);
		tvText.setGravity(gravity);
		tvPoster.setGravity(gravity);
		tvDate.setGravity(gravity);

	}

	@Override
	protected int getListItemLayout() {
		return R.layout.discussion_post_item_layout;
	}

}
