package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.DepthItem;

/**
 * This list adapter lists all the properties the user can search for.
 * 
 * @author Michael Zangl
 * 
 */
public class SearchPropertiesAdapter extends
		MyListAdapter<DepthItem<RoomProperty>> {

	/**
	 * This is a listener that is called when the ceckbox of a property was
	 * changed.
	 * 
	 * @author Michael Zangl
	 * 
	 */
	private final class CheckedChangeListener implements
			OnCheckedChangeListener {
		private final RoomProperty property;

		private CheckedChangeListener(final RoomProperty property) {
			this.property = property;
		}

		@Override
		public void onCheckedChanged(final CompoundButton buttonView,
				final boolean isChecked) {
			propertyChecked(this.property, isChecked);
		}
	}

	/**
	 * An increasing margin that is added to each property.
	 */
	private static final int MARGIN_PER_DEPTH = 10;

	/**
	 * The properties that are currently selected.
	 */
	private HashSet<RoomProperty> properties;
	/**
	 * The mutex to synchronize {@link #properties} against.
	 */
	private final Object propertiesMutex = new Object();

	/**
	 * Creates a new {@link SearchPropertiesAdapter} with no properties
	 * selected.
	 * 
	 * @param context
	 *            The context we use.
	 * @param roomProperties
	 *            A list of properties to display.
	 */
	public SearchPropertiesAdapter(final Context context,
			final List<DepthItem<RoomProperty>> roomProperties) {
		this(context, roomProperties, Collections.<RoomProperty> emptyList());
	}

	/**
	 * Creates a new {@link SearchPropertiesAdapter} with a given list of
	 * initially selected properties.
	 * 
	 * @param context
	 *            The context we use.
	 * @param roomProperties
	 *            A list of properties to display.
	 * @param initialProperties
	 *            A list of properties that are selected on start.
	 */
	public SearchPropertiesAdapter(final Context context,
			final List<DepthItem<RoomProperty>> roomList,
			final Collection<RoomProperty> initialProperties) {
		super(context, roomList);
		this.properties = new HashSet<RoomProperty>(initialProperties);
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final DepthItem<RoomProperty> property = getItem(position);
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.search_properties_item_checkbox);

		checkBox.setOnCheckedChangeListener(null);
		synchronized (this.propertiesMutex) {
			checkBox.setChecked(this.properties.contains(property.getItem()));
		}

		// TODO nicht jedes Mal den Listener instanziieren.
		checkBox.setOnCheckedChangeListener(new CheckedChangeListener(property
				.getItem()));

		checkBox.setText(getSpaces(MARGIN_PER_DEPTH + property.getDepth()
				* MARGIN_PER_DEPTH / 3)
				+ property.getItem().getName());
		checkBox.setPadding(property.getDepth() * MARGIN_PER_DEPTH, 0, 0, 0);

		// View space = view
		// .findViewById(R.id.search_properties_item_space);
		// space.getLayoutParams().width = property.getDepth() *
		// MARGIN_PER_DEPTH;
	}

	private static String getSpaces(final int numberOfSpaces) {
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < numberOfSpaces; i++) {
			buffer.append(" ");
		}

		return buffer.toString();
	}

	/**
	 * Called when the given property was checked to add it to the list of
	 * selected Properties or remove it from it.
	 * 
	 * @param property
	 *            The property that was changed
	 * @param isChecked
	 *            A boolean stating if it is currently checked.
	 */
	protected void propertyChecked(final RoomProperty property,
			final boolean isChecked) {
		synchronized (this.propertiesMutex) {
			if (isChecked) {
				this.properties.add(property);
			} else {
				this.properties.remove(property);
			}
		}
	}

	@Override
	protected int getListItemLayout() {
		return R.layout.search_properties_item;
	}

	public ArrayList<RoomProperty> getSelectedProperties() {
		return new ArrayList<RoomProperty>(this.properties);
	}

	/**
	 * Sets the list of currently selected properties. Only to be called in
	 * android main thread.
	 * 
	 * @param initialProperties
	 *            The list of current properties.
	 */
	public void setSelectedProperties(
			final Collection<RoomProperty> initialProperties) {
		synchronized (this.propertiesMutex) {
			this.properties = new HashSet<RoomProperty>(initialProperties);
			notifyDataSetChanged();
		}
	}
}
