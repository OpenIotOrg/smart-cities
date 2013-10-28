package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Address;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Room;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;
import edu.kit.cm.ivu.smartmeetings.ui.util.JsonDeserializer;
import edu.kit.cm.ivu.smartmeetings.ui.util.PrettyPrint;

/**
 * Displays the details and rooms of a building
 * 
 * @author David Kulicke
 * 
 */
public class BuildingDetailsFragment extends FrontendFragment implements
		OnItemClickListener {

	private Building building;
	private RoomListAdapter roomListAdapter;
	private View view;

	/**
	 * Constructs a new {@link BuildingDetailsFragment} for the given building.
	 * 
	 * @param building
	 */

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View details = inflater.inflate(R.layout.building_details, null);
		return details;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.view = view;

		final String buildingString = getArguments().getString("building");
		building = JsonDeserializer.deserialize(buildingString, Building.class);

		// display building number and label
		final TextView headline = (TextView) view
				.findViewById(R.id.building_details_headline);
		headline.setText(PrettyPrint.toString(building));

		// display build year (if available), campus area name and address
		final StringBuilder infoBuilder = new StringBuilder();
		final String buildYear = building.getBuildYear();
		if (buildYear != null && !buildYear.isEmpty()) {
			infoBuilder.append(getString(R.string.build_year, buildYear));
		}
		infoBuilder.append(building.getCampusAreaName());
		infoBuilder.append('\n');
		final Address address = building.getAddress();
		infoBuilder.append(PrettyPrint.toString(address));
		final TextView infoView = (TextView) view
				.findViewById(R.id.building_info_view);
		infoView.setText(infoBuilder.toString());

		// list rooms
		doInBackground(getRoomsWorker);

		final Button navigateToButton = (Button) view
				.findViewById(R.id.building_navigate_button);
		if (address.getLatitude() != null && address.getLongitude() != null) {
			navigateToButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					getFrontend().navigateTo(address);
				}
			});
		} else {
			navigateToButton.setEnabled(false);
		}

	}

	/**
	 * Sets the room list to the given list.
	 * 
	 * @param view
	 *            the root view of the layout
	 * @param rooms
	 *            the list of rooms we should display in that view.
	 */
	private void setRoomList(List<Room> rooms) {
		if (rooms == null) {
			rooms = Collections.emptyList();
		}

		final ListView list = (ListView) view
				.findViewById(R.id.building_room_list);
		roomListAdapter = new RoomListAdapter(view.getContext(), rooms);
		list.setAdapter(roomListAdapter);
		list.setOnItemClickListener(this);
	}

	private final Worker<Void, List<Room>> getRoomsWorker = new Worker<Void, List<Room>>() {

		@Override
		public List<Room> doWork(final Void... input) {
			return getBackend().getAllRoomsOfBuilding(building);
		}

		@Override
		public void handleResult(final List<Room> result) {
			setRoomList(result);
		}

	};

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		if (roomListAdapter != null) {
			final Room item = roomListAdapter.getItem(position);
			if (item != null) {
				getFrontend().showRoomDetails(item);
			}
		}
	}

}
