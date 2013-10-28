package edu.kit.cm.ivu.smartdiscussions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

/**
 * Endpoint class for discussions. Calls must be authenticated.
 * 
 * @author Kirill Rakhman
 */
@Api(name = "smartmeetings", version = "v1", clientIds = {
		Ids.ANDROID_CLIENT_ID1, Ids.ANDROID_CLIENT_ID2, Ids.ANDROID_CLIENT_ID3,
		Ids.ANDROID_CLIENT_ID4, Ids.ANDROID_CLIENT_ID5, Ids.ANDROID_CLIENT_ID6,
		Ids.WEB_CLIENT_ID,
		com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID }, audiences = { Ids.ANDROID_AUDIENCE })
public class DiscussionService {
	private static final String DEFAULT_LIMIT = "10";

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

	/**
	 * Lists the posts for a given topic.
	 * 
	 * @param topic
	 *            The topic of the discussion. Mustn't be <code>null</code>.
	 * @param older
	 *            Limit results to posts that are older than this. Can be
	 *            <code>null</code>.
	 * @param limit
	 *            Number of results. If <code>null</code>, the default value
	 *            {@value #DEFAULT_LIMIT} will be used.
	 * @param user
	 *            The user.
	 * @return List of postings, sorted from newest to oldest.
	 * @throws OAuthRequestException
	 */
	@SuppressWarnings("unchecked")
	@ApiMethod(name = "discussions.list")
	public List<PostContainer> list(@Named("topic") final String topic,
			@Nullable @Named("older") final Date older,
			@Nullable @Named("limit") String limit, final User user)
			throws OAuthRequestException {

		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		final PersistenceManager pm = getPersistenceManager();
		final Query query = pm.newQuery(Post.class);
		final List<Object> parameters = new ArrayList<Object>();

		String filter = "topic == topicParam";
		String declaredParams = "String topicParam";
		parameters.add(topic);

		if (older != null) {
			filter += " && date < dateParam";
			declaredParams += ", java.util.Date dateParam";
			parameters.add(older);
		}

		if (limit == null) {
			limit = DEFAULT_LIMIT;
		}

		query.declareParameters(declaredParams);
		query.setRange(0, new Long(limit));
		query.setOrdering("date desc");
		query.setFilter(filter);

		final List<Post> result = (List<Post>) pm.newQuery(query)
				.executeWithArray(parameters.toArray());

		addUserTopic(user, topic, pm);

		final List<PostContainer> resultList = new ArrayList<>(result.size());

		for (final Post post : result) {
			final PostContainer container = generatePostContainer(pm, post);
			resultList.add(container);
		}

		return resultList;
	}

	/**
	 * @param pm
	 * @param post
	 * @return
	 */
	private PostContainer generatePostContainer(final PersistenceManager pm,
			final Post post) {
		final String userName = getUserName(post.getUser(), pm);
		final String userId = post.getUser().getEmail();
		final PostContainer container = new PostContainer(post.getId(),
				userName, userId, post.getDate(), post.getText());
		return container;
	}

	// https://www.googleapis.com/auth/userinfo.email

	@ApiMethod(name = "discussions.insert", httpMethod = HttpMethod.POST)
	public Post insert(final Post post, final User user)
			throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}
		if (post.getTopic() == null) {
			throw new IllegalArgumentException("Topic mustn't be null");
		}

		if (post.getText() == null) {
			throw new IllegalArgumentException("Text mustn't be null");
		}

		if (post.getDate() == null) {
			post.setDate(new Date());
		}

		post.setUser(user);

		final PersistenceManager pm = getPersistenceManager();

		pm.makePersistent(post);

		addUserTopic(user, post.getTopic(), pm);

		pm.flush();

		final PostContainer postContainer = generatePostContainer(pm, post);
		final Discussion discussion = pm.getObjectById(Discussion.class,
				post.getTopic());

		PushHelper.pushNewPost(postContainer, discussion);

		return post;
	}

	// TODO Memcache
	private final Map<User, String> nameCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	private String getUserName(final User user, final PersistenceManager pm) {
		final String cachedName = nameCache.get(user);

		if (cachedName != null) {
			return cachedName;
		}

		// PersistenceManager pm = getPersistenceManager();
		final Query query = pm.newQuery(UserInfo.class);

		query.setFilter("user == userParam");
		query.declareParameters("com.google.appengine.api.users.User userParam");

		final List<UserInfo> result = (List<UserInfo>) pm.newQuery(query)
				.execute(user);

		// pm.close();

		String userName;
		if (!result.isEmpty()) {
			final String savedName = result.get(0).getUserName();

			if (savedName != null) {
				userName = savedName;
			} else {
				userName = user.getNickname();
			}

		} else {
			userName = user.getNickname();
		}

		nameCache.put(user, userName);

		return userName;
	}

	@SuppressWarnings("unchecked")
	private static void addUserTopic(final User user, final String topic,
			final PersistenceManager pm) {
		final Query query = pm.newQuery(UserInfo.class);

		query.setFilter("user == userParam");
		query.declareParameters("com.google.appengine.api.users.User userParam");

		final List<UserInfo> result = (List<UserInfo>) pm.newQuery(query)
				.execute(user);

		UserInfo userInfo;
		if (!result.isEmpty()) {
			userInfo = result.get(0);
		} else {
			userInfo = new UserInfo(user, null, null);
			pm.makePersistent(userInfo);
		}

		Set<String> topics = userInfo.getTopics();
		if (topics != null) {
			topics.add(topic);
		} else {
			topics = new HashSet<String>();
			topics.add(topic);
			userInfo.setTopics(topics);
		}

		// save the user id in the discussion
		final Discussion discussion = getDiscussion(topic, null, pm);

		final Set<Long> userIds = discussion.getUserIds();

		final Long userId = userInfo.getId();
		if (userIds != null) {
			userIds.add(userId);
		} else {
			discussion.setUserIds(new HashSet<>(Arrays.asList(userId)));
		}

		pm.flush();

	}

	/**
	 * Generates the list of discussions objects associated with the given user
	 * and adds it to the given object.
	 * 
	 * @param user
	 *            {@link UserInfo} object
	 */
	private static void addTopicList(final UserInfo user) {
		final Set<String> topics = user.getTopics();
		final Set<Discussion> discussionsList = new HashSet<Discussion>();
		if (topics != null && topics.size() > 0) {
			final PersistenceManager pm = getPersistenceManager();
			for (final String scanIdentifier : topics) {
				Discussion discussion;
				try {
					discussion = pm.getObjectById(Discussion.class,
							scanIdentifier);
				} catch (final Exception e) {
					// -- Occurs when no discussion was found in database
					discussion = new Discussion(scanIdentifier, scanIdentifier);
					pm.makePersistent(discussion);
				}
				discussionsList.add(discussion);
			}
		}
		user.setTopicsList(discussionsList);
	}

	@ApiMethod(name = "discussions.getTopicInfo", path = "discussions/getTopicInfo")
	public Discussion getTopicInfo(@Named("topic") final String topic,
			@Named("name") @Nullable final String name) {

		if (topic == null || topic.trim().length() <= 0) {
			throw new IllegalArgumentException("Invalid identifier.");
		}
		final PersistenceManager pm = getPersistenceManager();

		final Discussion discussion = getDiscussion(topic, name, pm);

		pm.close();
		return discussion;
	}

	private static Discussion getDiscussion(final String topic, String name,
			final PersistenceManager pm) {
		Discussion discussion;
		try {
			discussion = pm.getObjectById(Discussion.class, topic);
		} catch (final Exception e) {
			if (name == null) {
				name = topic;
			}

			discussion = new Discussion(topic, name);
			pm.makePersistent(discussion);
		}
		return discussion;
	}

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("SmartDiscussions");

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "discussions.getUserInfo", path = "discussions/getUserInfo")
	public UserInfo getUserInfo(final User user) throws OAuthRequestException {

		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		final PersistenceManager pm = getPersistenceManager();

		final Query query = pm.newQuery(UserInfo.class);

		query.setFilter("user == userParam");
		query.declareParameters("com.google.appengine.api.users.User userParam");

		final List<UserInfo> result = (List<UserInfo>) pm.newQuery(query)
				.execute(user);

		if (!result.isEmpty()) {
			final UserInfo userInfo = result.get(0);
			addTopicList(userInfo);
			return userInfo;
		} else {
			return new UserInfo(user, user.getNickname(), null);
		}

	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "discussions.removeUserTopic", path = "discussions/removeUserTopic/{topic}", httpMethod = HttpMethod.DELETE)
	public UserInfo removeUserTopic(@Named("topic") final String topic,
			final User user) throws OAuthRequestException {

		if (user == null) {
			throw new OAuthRequestException("Invalid user.");
		}

		final PersistenceManager pm = getPersistenceManager();

		final Query query = pm.newQuery(UserInfo.class);

		query.setFilter("user == userParam");
		query.declareParameters("com.google.appengine.api.users.User userParam");

		final List<UserInfo> result = (List<UserInfo>) pm.newQuery(query)
				.execute(user);

		UserInfo userInfo = result.get(0);
		if (!result.isEmpty()) {
			final Set<String> topics = userInfo.getTopics();
			if (topics != null) {
				topics.remove(topic);
			}
		} else {
			userInfo = new UserInfo(user, user.getNickname(), null);
		}
		pm.close();

		return userInfo;

	}

}
