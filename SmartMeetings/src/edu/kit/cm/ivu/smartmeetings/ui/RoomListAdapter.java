package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Room;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.PrettyPrint;

public class RoomListAdapter extends MyListAdapter<Room> {

	/**
	 * Creates a new room list adapter
	 * 
	 * @param context
	 *            The context to use.
	 * @param roomList
	 *            The room list to display.
	 */
	public RoomListAdapter(final Context context, final List<Room> roomList) {
		super(context, roomList);
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final Room room = getItem(position);
		final TextView title = (TextView) view
				.findViewById(R.id.building_room_list_item_title);
		title.setText(PrettyPrint.toString(room));

		final TextView details = (TextView) view
				.findViewById(R.id.building_room_list_item_detail);
		details.setText(PrettyPrint.toString(room.getBuilding()));
	}

	@Override
	protected int getListItemLayout() {
		return R.layout.building_room_list_item;
	}

}
