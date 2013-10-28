package edu.kit.cm.ivu.smartmeetings.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.google.api.services.smartmeetings.model.PostContainer;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

public class DiscussionFragment extends FrontendFragment implements
		OnSharedPreferenceChangeListener {

	private boolean scrollDown = true;

	private ListView listView;
	private String topic;
	private String publicName;
	private TextView textViewInput;
	private View buttonSend;

	private DiscussionPostAdapter adapter;
	private final List<PostContainer> posts = new ArrayList<PostContainer>();

	/**
	 * Flag indicating that more elements are bein loaded right now.
	 */
	private boolean loadingMore = true;

	/**
	 * Flag indicating that all available posts have been loaded for this
	 * discussion.
	 */
	private boolean noMoreElements = false;

	private SharedPreferences prefs;

	private MenuItem notificationsToggle;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.discussion_post_layout, container,
				false);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.within_discussion, menu);

		notificationsToggle = menu.findItem(R.id.toggle_notifications);
		setNotificationsMenuItem();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.send_room:
			doInBackground(showRoomsWorker);
			return true;
		case R.id.menu_share_discussion:
			getFrontend().showDiscussionSharing(topic, publicName);
			return true;
		case R.id.toggle_notifications:
			toggleNotifications();
			return true;
		}

		return false;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// TODO bei orientation change landet das fragment links

		listView = (ListView) view.findViewById(R.id.listViewPosts);

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(final AbsListView view,
					final int scrollState) {
			}

			@Override
			public void onScroll(final AbsListView view,
					final int firstVisibleItem, final int visibleItemCount,
					final int totalItemCount) {
				if (!loadingMore && firstVisibleItem == 0) {

					if (!noMoreElements) {
						loadingMore = true;

						if (!posts.isEmpty()) {
							scrollDown = false;
							doInBackground(getPostsWorker, posts.get(0)
									.getDate());
						}
					}

				}
			}
		});

		textViewInput = (TextView) view.findViewById(R.id.editTextInput);

		buttonSend = view.findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				sendText();
			}
		});

		textViewInput.setOnKeyListener(new OnKeyListener() {
			private final InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			@Override
			public boolean onKey(final View v, final int keyCode,
					final KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}

				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					sendText();
					imm.hideSoftInputFromWindow(textViewInput.getWindowToken(),
							0);
					return true;
				}

				return false;
			}
		});

		topic = getArguments().getString("topic_identifier");
		publicName = getArguments().getString("topic_publicname");
		if (publicName == null) {
			publicName = topic;
		}

		((TextView) view.findViewById(R.id.textViewDiscussionTopic))
				.setText(publicName);

		adapter = new DiscussionPostAdapter(getActivity(), posts);
		listView.setAdapter(adapter);

		doInBackground(getPostsWorker);

		prefs.edit().putString("currentDiscussion", topic).commit();

	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		prefs.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerGcmReceiver(getActivity());

		if (prefs != null) {
			prefs.edit().putString("currentDiscussion", topic).commit();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(pushReceiver);
		prefs.edit().putString("currentDiscussion", "").commit();
	}

	/**
	 * Sets the title of the menu item according to the current notification
	 * preference.
	 * 
	 * @param menuItem
	 *            Menu item
	 */
	private void setNotificationsMenuItem() {
		if (notificationsToggle != null) {
			notificationsToggle.setChecked(isNotificationsEnabled());
		}
	}

	/**
	 * Changes the notifications preference from <code>true</code> to
	 * <code>false</code> or otherwise and changes the coresponding menu item.
	 * 
	 * @param menuItem
	 *            Menu item
	 */
	private void toggleNotifications() {
		final boolean wereEnabled = isNotificationsEnabled();
		prefs.edit().putBoolean("notifications:" + topic, !wereEnabled)
				.commit();
		setNotificationsMenuItem();

		final int stringRes = wereEnabled ? R.string.notification_disabled
				: R.string.notification_enabled;

		final Context c = getActivity();
		Toast.makeText(c, c.getString(stringRes, publicName),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * @return <code>true</code>, if the notifications are enabled for this
	 *         conversation.
	 */
	private boolean isNotificationsEnabled() {
		// if value is already set, it will be retrieved, otherwise global
		// setting will be used (which has true as default)
		return prefs.getBoolean("notifications:" + topic,
				prefs.getBoolean("pref_notifications", true));
	}

	private void sendText() {
		buttonSend.setEnabled(false);
		textViewInput.setEnabled(false);

		doInBackground(writePostWorker, textViewInput.getText().toString());
		textViewInput.setText("");
	}

	private final Worker<DateTime, List<PostContainer>> getPostsWorker = new Worker<DateTime, List<PostContainer>>() {

		@Override
		public List<PostContainer> doWork(final DateTime... input) {
			if (input.length > 0) {
				return getBackend().getPosts(topic, input[0]);
			} else {
				return getBackend().getPosts(topic);
			}
		}

		@Override
		public void handleResult(final List<PostContainer> result) {
			if (result.size() == 0) {
				noMoreElements = true;
			} else {
				final int index = listView.getFirstVisiblePosition()
						+ result.size();
				final View v = listView.getChildAt(0);
				final int top = (v == null) ? 0 : v.getTop();

				// beiträge sind in umgekehrter reihenfolge
				// posts.addAll(0, result);
				if (result.size() > 0) {
					for (final PostContainer newPostEntry : result) {
						putPostIntoList(newPostEntry);
					}
				}
				adapter.notifyDataSetChanged();

				if (scrollDown) {
					listView.setSelection(adapter.getCount() - 1);
				} else {
					listView.setSelectionFromTop(index, top);
				}
			}
			loadingMore = false;
		}

		/**
		 * Puts a new PostContainer into the PostContainer-List {@link posts}
		 * 
		 * @param newPostEntry
		 *            New PostContainer-Element
		 */
		private void putPostIntoList(final PostContainer newPostEntry) {
			Boolean addPost = false;
			Integer location = 0;
			if (posts.size() == 0) {
				addPost = true;
			} else {
				if (!posts.contains(newPostEntry)) {
					for (int i = posts.size() - 1; i >= 0; i--) {
						if (newPostEntry.getDate().getValue() >= posts.get(i)
								.getDate().getValue()) {
							addPost = true;
							scrollDown = true;
							location = i + 1;
							break;
						} else if (newPostEntry.getDate().getValue() < posts
								.get(i).getDate().getValue()) {
							if (i > 0) {
								if (newPostEntry.getDate().getValue() < posts
										.get(i - 1).getDate().getValue()) {
									continue;
								}
							}
							addPost = true;
							location = i;
							break;
						}
					}
				}
			}
			if (addPost) {
				posts.add(location, newPostEntry);
			}
		}
	};

	private final Worker<String, PostContainer> writePostWorker = new Worker<String, PostContainer>() {

		@Override
		public PostContainer doWork(final String... input) {
			return getBackend().writePost(topic, input[0]);

		}

		@Override
		public void handleResult(final PostContainer result) {
			doInBackground(getPostsWorker);
			adapter.notifyDataSetChanged();
			listView.setSelection(adapter.getCount() - 1);

			buttonSend.setEnabled(true);
			textViewInput.setEnabled(true);
		}
	};

	private final Worker<Void, List<Room>> showRoomsWorker = new Worker<Void, List<Room>>() {

		@Override
		public void handleResult(final List<Room> result) {
			final ArrayAdapter<Room> arrayAdapter = new ArrayAdapter<Room>(
					getActivity(), android.R.layout.simple_list_item_1, result) {
				@Override
				public View getView(final int position, final View convertView,
						final ViewGroup parent) {
					final TextView view = (TextView) super.getView(position,
							convertView, parent);
					view.setText(getItem(position).getLabel());
					return view;
				}
			};

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity());
			builder.setTitle(R.string.select_room);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss();
						}
					});

			builder.setAdapter(arrayAdapter,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(final DialogInterface dialog,
								final int which) {
							final Room item = arrayAdapter.getItem(which);

							try {
								final String id = URLEncoder.encode(
										item.getUri(), "utf-8");
								textViewInput.setText("smartmeetings://rooms/"
										+ id);
							} catch (final UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					});
			builder.show();
		}

		@Override
		public List<Room> doWork(final Void... input) {
			final List<Reservation> reservations = getBackend()
					.getReservationsOfUser();

			final List<Room> rooms = new ArrayList<Room>();
			for (final Reservation reservation : reservations) {
				rooms.add(reservation.getRoom());
			}
			return rooms;
		}
	};

	@Override
	public void onSharedPreferenceChanged(
			final SharedPreferences sharedPreferences, final String key) {
		if (key.equals("pref_notifications")) {
			setNotificationsMenuItem();
		}
	}

	private void registerGcmReceiver(final Context c) {
		final IntentFilter filter = new IntentFilter();
		filter.addAction("com.google.android.c2dm.intent.RECEIVE");
		filter.addCategory(c.getPackageName());

		c.registerReceiver(pushReceiver, filter,
				"com.google.android.c2dm.permission.SEND", null);

	}

	private final BroadcastReceiver pushReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final Bundle extras = intent.getExtras();
			final String discussionId = extras.getString("discussionId");

			if (discussionId.equals(topic)) {
				doInBackground(getPostsWorker);
			}
		}
	};

}
