package edu.kit.cm.ivu.smartdiscussions;

import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable
public class UserInfo {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private User user;

	@Persistent
	private String userName;

	@Persistent
	private Set<String> topics;

	@Persistent
	@Element(dependent = "true")
	private List<String> pushIds;

	// TODO: Change field 'topics' from Set<String> to Set<Discussion>
	// when Google App Engine supports unowned 1:N-relationships - and remove
	// this one.
	@NotPersistent
	private Set<Discussion> topicsList;

	public UserInfo(final User user, final String userName,
			final Set<String> topics) {
		super();
		this.user = user;
		this.userName = userName;
		this.topics = topics;
	}

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public Long getId() {
		return id;
	}

	public Set<String> getTopics() {
		return topics;
	}

	public void setTopics(final Set<String> topics) {
		this.topics = topics;
	}

	public Set<Discussion> getTopicsList() {
		return topicsList;
	}

	public void setTopicsList(final Set<Discussion> topicList) {
		topicsList = topicList;
	}

	public List<String> getPushIds() {
		return pushIds;
	}

	public void setPushIds(final List<String> pushIds) {
		this.pushIds = pushIds;
	}

}
