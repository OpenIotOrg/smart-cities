package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Building;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.PrettyPrint;

public class BuildingListAdapter extends BaseAdapter implements Filterable {

	private final Context context;
	private final List<Building> objects;
	private List<Building> filtered;

	public BuildingListAdapter(final Context context,
			final List<Building> objects) {
		super();
		this.context = context;
		this.objects = objects;
		filtered = objects;
	}

	@Override
	public int getCount() {
		return filtered.size();
	}

	@Override
	public Building getItem(final int position) {
		return filtered.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, final ViewGroup parent) {
		if (view == null) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.building_room_list_item, null);
		}

		final Building building = filtered.get(position);
		final TextView title = (TextView) view
				.findViewById(R.id.building_room_list_item_title);
		title.setText(PrettyPrint.toString(building));

		final TextView details = (TextView) view
				.findViewById(R.id.building_room_list_item_detail);
		details.setText(building.getCampusAreaName());

		return view;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence constraint,
					final FilterResults results) {
				filtered = (List<Building>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(
					final CharSequence constraint) {
				final List<Building> list = new ArrayList<Building>();

				for (final Building b : objects) {
					if (b.getBuildingNumber().contains(constraint)
							|| b.getLabel().contains(constraint)) {
						list.add(b);
					}
				}

				final FilterResults results = new FilterResults();
				results.values = list;
				results.count = list.size();

				return results;
			}
		};
	}
}
