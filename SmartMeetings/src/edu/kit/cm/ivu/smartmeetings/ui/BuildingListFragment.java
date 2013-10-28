package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.services.smartmeetings.model.Building;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

/**
 * This fragment is used to display a list of buildings.
 * 
 * @author David Kulicke
 * 
 */
public class BuildingListFragment extends FrontendFragment implements
		OnItemClickListener, TextWatcher {

	private ListView list;
	private EditText filter;

	private BuildingListAdapter listAdapter;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.building_list, null);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		list = (ListView) view.findViewById(R.id.building_list_list);
		list.setOnItemClickListener(this);

		filter = (EditText) view.findViewById(R.id.editTextFilter);
		filter.addTextChangedListener(this);

		doInBackground(getBuildingsWorker);
	}

	/**
	 * Worker that retrieves and sets the list of buildings
	 */
	private final Worker<Void, List<Building>> getBuildingsWorker = new Worker<Void, List<Building>>() {

		@Override
		public List<Building> doWork(final Void... input) {
			return getBackend().getAllBuildings();
		}

		@Override
		public void handleResult(final List<Building> result) {
			setBuildingList(result);
		}

	};

	/**
	 * Sets the building list to the given list.
	 * 
	 * @param buildings
	 *            the list of buildings to be displayed
	 */
	private void setBuildingList(final List<Building> buildings) {

		listAdapter = new BuildingListAdapter(getActivity(), buildings);
		list.setAdapter(listAdapter);

	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		if (listAdapter != null) {
			final Building item = listAdapter.getItem(position);
			if (item != null) {
				getFrontend().showBuildingDetails(item);
			}
		}
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start,
			final int count, final int after) {
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start,
			final int before, final int count) {
	}

	@Override
	public void afterTextChanged(final Editable s) {
		if (listAdapter != null) {
			listAdapter.getFilter().filter(s);
		}
	}
}
