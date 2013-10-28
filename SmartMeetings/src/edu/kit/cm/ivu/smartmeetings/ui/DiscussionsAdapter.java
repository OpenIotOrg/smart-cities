package edu.kit.cm.ivu.smartmeetings.ui;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.PostContainer;

import edu.kit.cm.ivu.smartmeetings.R;

public class DiscussionsAdapter extends MyListAdapter<Discussion> {

	private final Map<Discussion, PostContainer> latestPosts = new HashMap<Discussion, PostContainer>();
	private final SimpleDateFormat sdf = new SimpleDateFormat();

	public DiscussionsAdapter(final Context context) {
		this(context, null);
	}

	public DiscussionsAdapter(final Context context,
			final List<Discussion> objects) {
		super(context, objects);
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final TextView tvName = (TextView) view
				.findViewById(R.id.textViewDiscussionName);
		final TextView tvPreview = (TextView) view
				.findViewById(R.id.textViewLastPostPreview);
		final TextView tvDate = (TextView) view
				.findViewById(R.id.textViewLastPostDate);

		final Discussion topic = getItem(position);
		tvName.setText(topic.getPublicName());

		final PostContainer latest = latestPosts.get(topic);

		if (latest != null) {
			tvPreview.setText(latest.getUserName() + ": " + latest.getText());
			final Date date = new Date(latest.getDate().getValue());
			tvDate.setText(sdf.format(date));
		} else {
			tvPreview.setText("...");
			tvDate.setText("...");
		}
	}

	public void setLatestPost(final Discussion topic, final PostContainer post) {
		latestPosts.put(topic, post);
	}

	public void orderTopics() {
		this.sort(new Comparator<Discussion>() {
			@Override
			public int compare(final Discussion lhs, final Discussion rhs) {
				final PostContainer latestPostLhs = latestPosts.get(lhs);
				final PostContainer latestPostRhs = latestPosts.get(rhs);

				if (latestPostLhs == null) {
					return 1;
				}
				if (latestPostRhs == null) {
					return -1;
				}

				if (latestPostLhs.getDate().getValue() < latestPostRhs
						.getDate().getValue()) {
					return 1;
				}
				if (latestPostLhs.getDate().getValue() > latestPostRhs
						.getDate().getValue()) {
					return -1;
				}
				return 0;
			}
		});
	}

	@Override
	protected int getListItemLayout() {
		return R.layout.discussion_list_item_layout;
	}

}
