package edu.kit.cm.ivu.smartmeetings.logic.smartdiscussion;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.smartmeetings.Smartmeetings.Discussions;
import com.google.api.services.smartmeetings.Smartmeetings.Discussions.GetTopicInfo;
import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.Post;
import com.google.api.services.smartmeetings.model.PostContainer;
import com.google.api.services.smartmeetings.model.PostContainerCollection;
import com.google.api.services.smartmeetings.model.UserInfo;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.ISmartDiscussionsConnector;

public class SmartDiscussionsConnector implements ISmartDiscussionsConnector {

	private final Discussions discussions;
	private final IEndpointConnector endpointConnector;

	public SmartDiscussionsConnector(final IEndpointConnector endpointConnector) {
		this.endpointConnector = endpointConnector;
		discussions = endpointConnector.getService().discussions();
	}

	@Override
	public List<Discussion> getUserTopics() {
		List<Discussion> result = null;
		try {

			final UserInfo userInfo = discussions.getUserInfo().execute();
			result = userInfo.getTopicsList();

		} catch (final IOException e) {
			e.printStackTrace();
		}

		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

	@Override
	public List<PostContainer> getPosts(final String topic) {
		return getPosts(topic, null);
	}

	@Override
	public List<PostContainer> getPosts(final String topic, final DateTime older) {
		List<PostContainer> result = null;
		if (!topic.isEmpty()) {
			try {
				final Discussions.List listing = discussions.list(topic);
				if (older != null) {
					listing.set("older", older);
				}
				final PostContainerCollection posts = listing.execute();
				result = posts.getItems();

			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		if (result == null) {
			result = Collections.emptyList();
		}

		Collections.reverse(result);
		return result;
	}

	@Override
	public PostContainer writePost(final String topic, final String message) {
		Post result = null;
		if (!message.isEmpty()) {
			final Post post = new Post();
			post.setText(message);
			post.setTopic(topic);

			try {
				result = discussions.insert(post).execute();
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		Log.d("SmartDiscussionsConnector", "Writing post " + result);

		if (result != null) {
			return new PostContainer().setDate(result.getDate())
					.setId(result.getId()).setText(result.getText())
					.setUserId(endpointConnector.getUserId())
					.setUserName(endpointConnector.getUserName());
		} else {
			return null;
		}
	}

	@Override
	public List<String> removeUserTopic(final String topic) {

		Log.d("SmartDiscussionsConnector", "removeUserTopic " + topic);

		List<String> result = null;
		if (!topic.isEmpty()) {
			try {
				final UserInfo userInfo = discussions.removeUserTopic(topic)
						.execute();
				result = userInfo.getTopics();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return result;

	}

	@Override
	public PostContainer getLatestPost(final String topic) {
		Log.d("SmartDiscussionsConnector", "getLatestPost " + topic);

		PostContainer result = null;

		if (!topic.isEmpty()) {
			try {
				final List<PostContainer> items = discussions.list(topic)
						.set("limit", "1").execute().getItems();

				if (items != null && !items.isEmpty()) {
					result = items.get(0);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	@Override
	public Discussion getTopicInformation(final String topic, final String name) {
		Log.d("SmartDiscussionsConnector", "getTopicInformation " + topic);

		Discussion discussion = null;
		if (!topic.isEmpty()) {
			try {
				final GetTopicInfo topicInfo = discussions.getTopicInfo(topic);

				if (name != null) {
					topicInfo.set("name", name);
				}

				discussion = topicInfo.execute();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return discussion;
	}

	@Override
	public Discussion getTopicInformation(final String topic) {
		return getTopicInformation(topic, null);
	}

}
