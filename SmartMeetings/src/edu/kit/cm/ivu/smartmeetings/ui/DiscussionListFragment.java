package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.PostContainer;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

public class DiscussionListFragment extends FrontendFragment {
	private ListView listView;
	private List<Discussion> discussions;
	private DiscussionsAdapter adapter;

	private final AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
			final MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.selected_discussion_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode,
				final Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(final ActionMode mode,
				final MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_discussion_remove:
				final SparseBooleanArray checked = listView
						.getCheckedItemPositions();

				final Discussion[] topics = new Discussion[checked.size()];
				for (int i = 0; i < checked.size(); i++) {
					final int pos = checked.keyAt(i);
					final Discussion topic = discussions.get(pos);
					Log.d("DiscussionListFragment", "topic " + topic);
					topics[i] = topic;
				}

				doInBackground(removeDiscussionsWorker, topics);

				mode.finish();
				return true;
			default:
				return false;
			}

		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {
		}

		@Override
		public void onItemCheckedStateChanged(final ActionMode mode,
				final int position, final long id, final boolean checked) {
			Log.d("DiscussionListFragment", "item " + position + " checked "
					+ checked);
		}

	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	};

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.discussion_list_context, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan_exercise:
			getFrontend().showDiscussionScanning();
			return true;
		case R.id.menu_create_discussion:
			getFrontend().showDiscussionCreation();
			return true;
		case R.id.menu_reload:
			doInBackground(getDiscussionsWorker);
			return true;
		}

		return false;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.discussion_list_layout, container,
				false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setRetainInstance(true);

		listView = (ListView) view.findViewById(R.id.listViewDiscussions);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				getFrontend().showDiscussion(discussions.get(position));
			}
		});

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(multiChoiceModeListener);

		doInBackground(getDiscussionsWorker);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private final Worker<Void, List<Discussion>> getDiscussionsWorker = new Worker<Void, List<Discussion>>() {

		@Override
		public List<Discussion> doWork(final Void... input) {
			return getBackend().getUserTopics();
		}

		@Override
		public void handleResult(final List<Discussion> result) {
			discussions = result;
			adapter = new DiscussionsAdapter(getActivity(), result);

			// load latest topics
			doInBackground(getLatestPostsWork);
		}
	};

	private final Worker<Discussion, Void> removeDiscussionsWorker = new Worker<Discussion, Void>() {

		@Override
		public Void doWork(final Discussion... input) {
			for (final Discussion topic : input) {
				getBackend().removeUserTopic(topic.getTopic());
			}

			return null;
		}

		@Override
		public void handleResult(final Void result) {
			doInBackground(getDiscussionsWorker);
		}

	};

	private final Worker<Void, Map<Discussion, PostContainer>> getLatestPostsWork = new Worker<Void, Map<Discussion, PostContainer>>() {

		@Override
		public Map<Discussion, PostContainer> doWork(final Void... input) {
			final ISyncLogicFacade backend = getBackend();
			final Map<Discussion, PostContainer> result = new HashMap<Discussion, PostContainer>();

			for (final Discussion topic : discussions) {
				final PostContainer latestPost = backend.getLatestPost(topic
						.getTopic());

				result.put(topic, latestPost);
			}

			return result;
		}

		@Override
		public void handleResult(final Map<Discussion, PostContainer> result) {
			for (final Discussion topic : result.keySet()) {
				adapter.setLatestPost(topic, result.get(topic));
			}
			adapter.orderTopics();
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	};
}
