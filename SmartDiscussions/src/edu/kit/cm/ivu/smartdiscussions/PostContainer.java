package edu.kit.cm.ivu.smartdiscussions;

import java.util.Date;

public class PostContainer {

	private String userName;

	private Date date;

	private String text;

	private final String userId;

	private final Long id;

	public PostContainer(final Long id, final String userName,
			final String userId, final Date date, final String text) {
		this.id = id;
		this.userName = userName;
		this.date = date;
		this.text = text;
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}
}
