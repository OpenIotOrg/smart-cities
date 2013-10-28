package edu.kit.cm.ivu.smartdiscussions;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Discussion {

	@PrimaryKey
	@Persistent
	private String topic;

	@Persistent
	private String publicName;

	@Persistent
	private Set<Long> userIds;

	public Discussion(final String topic, final String publicName) {
		this.topic = topic;
		this.publicName = publicName;
		userIds = new HashSet<>();
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(final String topic) {
		this.topic = topic;
	}

	public String getPublicName() {
		return publicName;
	}

	public void setPublicName(final String publicName) {
		this.publicName = publicName;
	}

	public Set<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(final Set<Long> userIds) {
		this.userIds = userIds;
	}

}
