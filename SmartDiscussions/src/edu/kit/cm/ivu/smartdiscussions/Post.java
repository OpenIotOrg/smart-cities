package edu.kit.cm.ivu.smartdiscussions;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable
public class Post {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private User user;

	@Persistent
	private Date date;

	@Persistent
	private String topic;

	@Persistent
	private String text;

	public Post(final User user, final Date date, final String topic,
			final String text) {
		super();
		this.user = user;
		this.date = date;
		this.topic = topic;
		this.text = text;
	}

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(final String topic) {
		this.topic = topic;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public Long getId() {
		return id;
	}

}
