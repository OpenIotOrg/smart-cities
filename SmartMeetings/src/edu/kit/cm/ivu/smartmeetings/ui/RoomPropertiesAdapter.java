package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.DepthItem;

public class RoomPropertiesAdapter extends
		MyListAdapter<DepthItem<RoomProperty>> {

	private static final int MARGIN_PER_DEPTH = 15;

	/**
	 * Creates a new room properties adapter.
	 * 
	 * @param context
	 *            The context to use.
	 * @param propertiesList
	 *            The list of properties to display.
	 */
	public RoomPropertiesAdapter(final Context context,
			final List<DepthItem<RoomProperty>> propertiesList) {
		super(context, propertiesList);
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final DepthItem<RoomProperty> property = getItem(position);
		final TextView description = (TextView) view
				.findViewById(R.id.room_properties_item_name);
		description.setText(property.getItem().getName());
		description.setPadding(property.getDepth() * MARGIN_PER_DEPTH, 0, 0, 0);
	}

	@Override
	protected int getListItemLayout() {
		return R.layout.room_properties_item;
	}

	@Override
	protected boolean isReadOnly() {
		return true;
	}
}
