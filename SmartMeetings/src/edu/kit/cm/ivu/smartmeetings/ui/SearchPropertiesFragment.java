package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.Collection;
import java.util.List;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.DepthItem;

/**
 * This fragment displays the List of properties the user can search for.
 * 
 * @author Michael Zangl
 * @see IFrontend#showSearchDialog()
 */
@TargetApi(11)
public class SearchPropertiesFragment extends FrontendFragment {

	private static final String SELECTED = "selected";

	private final class SearchSubmitListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			submitSearch();
		}
	}

	/**
	 * The list content of the properties list with the checkboxes.
	 */
	private SearchPropertiesAdapter adapter;

	/**
	 * A list of properties that were selected. Stored in the instance state.
	 */
	private Collection<RoomProperty> selected;
	private ListView list;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.search, null);
		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewCreated(final View root, final Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		list = (ListView) root.findViewById(R.id.search_properties);

		final Button button = (Button) root.findViewById(R.id.search_submit);
		button.setOnClickListener(new SearchSubmitListener());

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(SELECTED)) {

			selected = (Collection<RoomProperty>) savedInstanceState
					.getSerializable(SELECTED);
		}

		doInBackground(searchPropertiesWorker);
	}

	private final SearchPropertiesWorker searchPropertiesWorker = new SearchPropertiesWorker();

	private class SearchPropertiesWorker extends
			ListFlatteningWorker<Void, RoomProperty> {

		@Override
		public void handleResult(final List<DepthItem<RoomProperty>> result) {
			adapter = new SearchPropertiesAdapter(getActivity(), result);
			list.setAdapter(adapter);
			if (selected != null) {
				adapter.setSelectedProperties(selected);
			}
		}

		@Override
		protected List<RoomProperty> getTopElements() {
			return getBackend().getAllRoomProperties();
		}

		@Override
		protected List<RoomProperty> getChildren(final RoomProperty node) {
			return node.getChildren();
		}

	}

	/**
	 * Starts the search for the given properties when the user clicks on the
	 * search button.
	 */
	protected void submitSearch() {
		if (adapter != null) {
			getFrontend().searchRoom(adapter.getSelectedProperties());
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (adapter != null) {
			outState.putSerializable(SELECTED, adapter.getSelectedProperties());
		}
	}

}
