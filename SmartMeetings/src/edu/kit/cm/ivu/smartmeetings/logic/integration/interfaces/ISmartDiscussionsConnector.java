package edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces;

import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.PostContainer;

public interface ISmartDiscussionsConnector {

	public Discussion getTopicInformation(String topic);

	public Discussion getTopicInformation(String topic, String name);

	public List<Discussion> getUserTopics();

	public List<PostContainer> getPosts(String topic);

	public List<PostContainer> getPosts(String topic, DateTime older);

	public PostContainer writePost(String topic, String message);

	public List<String> removeUserTopic(String topic);

	public PostContainer getLatestPost(String topic);

}
