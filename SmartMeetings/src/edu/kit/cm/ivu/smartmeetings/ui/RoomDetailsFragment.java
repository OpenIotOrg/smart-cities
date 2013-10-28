package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Address;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.DepthItem;
import edu.kit.cm.ivu.smartmeetings.ui.util.JsonDeserializer;
import edu.kit.cm.ivu.smartmeetings.ui.util.PrettyPrint;

/**
 * This is the room details view.
 * 
 * @author Michael Zangl
 * @author David Kulicke
 * 
 */
public class RoomDetailsFragment extends FrontendFragment {

	/**
	 * The room we are displaying.
	 */
	private Room room;
	private ListView list;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.room_context, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_discuss:
			GetTopicInfoWorker.asTask(getFrontend()).execute(
					GetTopicInfoWorker.getDiscussionUri(room.getUri(),
							room.getLabel()));
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View details = inflater.inflate(R.layout.room_details, null);

		return details;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final String roomString = getArguments().getString("room");
		room = JsonDeserializer.deserialize(roomString, Room.class);

		// display label and room number
		final TextView headline = (TextView) view
				.findViewById(R.id.room_details_headline);
		headline.setText(PrettyPrint.toString(room));

		// display building
		final TextView buildingView = (TextView) view
				.findViewById(R.id.room_details_building_name);
		final Building building = room.getBuilding();
		buildingView.setText(PrettyPrint.toString(building));

		// display seats and floor space, if available
		final TextView spaceInfoView = (TextView) view
				.findViewById(R.id.room_details_space);
		final StringBuilder spaceInfoBuilder = new StringBuilder(room
				.getNumberOfSeats().toString());
		spaceInfoBuilder.append(" " + getString(R.string.number_of_seats));
		if (room.getFloorSpace() != null) {
			spaceInfoBuilder.append(", " + room.getFloorSpace()
					+ getString(R.string.square_meters));
		}
		spaceInfoBuilder.append(".");
		spaceInfoView.setText(spaceInfoBuilder.toString());

		list = (ListView) view.findViewById(R.id.room_details_properties);

		doInBackground(roomDetailsWorker);

		final Button bookButton = (Button) view
				.findViewById(R.id.room_details_book);
		bookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				getFrontend().showReserveRoom(room);
			}
		});
		final Button navigateToButton = (Button) view
				.findViewById(R.id.room_details_navigate_to);
		final Address roomAddress = room.getBuilding().getAddress();
		if (roomAddress.getLatitude() != null
				&& roomAddress.getLongitude() != null) {
			navigateToButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					getFrontend().navigateTo(roomAddress);
				}
			});
		} else {
			navigateToButton.setEnabled(false);
		}

	}

	private final RoomDetailsWorker roomDetailsWorker = new RoomDetailsWorker();

	private class RoomDetailsWorker extends
			ListFlatteningWorker<Void, RoomProperty> {

		@Override
		public void handleResult(final List<DepthItem<RoomProperty>> result) {
			list.setAdapter(new RoomPropertiesAdapter(getActivity(), result));
		}

		@Override
		protected List<RoomProperty> getTopElements() {
			return getBackend().getPropertiesOfRoom(room);
		}

		@Override
		protected List<RoomProperty> getChildren(final RoomProperty node) {
			return node.getChildren();
		}

	}

}
