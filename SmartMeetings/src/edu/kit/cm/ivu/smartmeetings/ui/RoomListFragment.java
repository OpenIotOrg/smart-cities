package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;
import edu.kit.cm.ivu.smartmeetings.ui.util.JsonDeserializer;

/**
 * This is a fragment that can display a list of rooms.
 * <p>
 * It expects a parameter with the things to search for.
 * 
 * @author michael
 * 
 */
public class RoomListFragment extends FrontendFragment implements
		OnItemClickListener {

	/**
	 * The room list adapter we are using.
	 */
	private RoomListAdapter roomListAdapter;
	/**
	 * The properties the user searched for and the list should therefore be
	 * filtered by. This is safed in the instance state.
	 */
	private Collection<RoomProperty> searchFor;
	private View root;

	private final class ShowSearchDialogListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			getFrontend().showSearchDialog();
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.room_list, null);
		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewCreated(final View root, final Bundle savedInstanceState) {
		this.root = root;
		super.onViewCreated(root, savedInstanceState);

		final String[] searchForStrings = getArguments().getStringArray(
				"searchFor");
		searchFor = new ArrayList<RoomProperty>();
		for (final String s : searchForStrings) {
			searchFor.add(JsonDeserializer.deserialize(s, RoomProperty.class));
		}

		if (searchFor == null) {
			doInBackground(getAllRoomsWorker);
			setTitle(root,
					root.getContext().getString(R.string.room_list_headline));
			addButton(root, R.string.search, new ShowSearchDialogListener());
		} else {
			doInBackground(searchRoomsWorker, searchFor);
			setTitle(root,
					root.getContext().getString(R.string.results_headline));
		}
	}

	private final Worker<Void, List<Room>> getAllRoomsWorker = new Worker<Void, List<Room>>() {

		@Override
		public List<Room> doWork(final Void... input) {
			return getBackend().getAllRooms();
		}

		@Override
		public void handleResult(final List<Room> result) {
			setRoomList(root, result);
		}

	};

	private final Worker<Collection<RoomProperty>, List<Room>> searchRoomsWorker = new Worker<Collection<RoomProperty>, List<Room>>() {

		@Override
		public List<Room> doWork(final Collection<RoomProperty>... input) {
			if (input.length > 0) {
				return getBackend().searchRoom(input[0]);
			} else {
				return getBackend().getAllRooms();
			}
		}

		@Override
		public void handleResult(final List<Room> result) {
			setRoomList(root, result);
		}

	};

	private static void addButton(final View root, final int text,
			final OnClickListener clickListener) {
		final LinearLayout line = (LinearLayout) root
				.findViewById(R.id.room_list_buttons);

		final Button b = new Button(root.getContext());
		b.setText(text);
		b.setOnClickListener(clickListener);
		line.addView(b);
	}

	/**
	 * Sets the room list to the given list.
	 * 
	 * @param root
	 *            The root view of our layout, where we find the room list view
	 *            in.
	 * @param rooms
	 *            The list of rooms we should display in that view.
	 */
	private void setRoomList(final View root, final List<Room> rooms) {
		final ListView list = (ListView) root.findViewById(R.id.room_list_list);
		roomListAdapter = new RoomListAdapter(root.getContext(), rooms);
		list.setAdapter(roomListAdapter);
		list.setOnItemClickListener(this);
	}

	private static void setTitle(final View root, final String title) {
		final TextView headline = (TextView) root
				.findViewById(R.id.room_list_headline);
		headline.setText(title);
	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, final View arg1,
			final int pos, final long arg3) {
		if (roomListAdapter != null) {
			final Room item = roomListAdapter.getItem(pos);
			if (item != null) {
				getFrontend().showRoomDetails(item);
			}
		}
	}
}
