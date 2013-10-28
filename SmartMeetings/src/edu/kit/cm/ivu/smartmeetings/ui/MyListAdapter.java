package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * Simple implementation of a {@link ListAdapter}. Extending classes need to
 * define the item layout and to populate the item views.
 * 
 * @author Kirill Rakhman
 */
public abstract class MyListAdapter<E> extends ArrayAdapter<E> {

	public MyListAdapter(final Context context, final List<E> objects) {
		super(context, 0, objects);
	}

	@Override
	public View getView(final int position, final View recycleView,
			final ViewGroup parent) {
		View view;
		if (recycleView != null) {
			view = recycleView;
		} else {
			final LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(getListItemLayout(), null);
		}

		fillItemView(position, view);

		return view;
	}

	@Override
	public boolean isEnabled(final int position) {
		return !isReadOnly() && position < getCount();
	}

	/**
	 * This indicates if the list should be read only. This is needed to tell
	 * android if it should disable all items.
	 * <p>
	 * The default implementation returns <code>false</code>.
	 * 
	 * @return <code>true</code> to disable all items.
	 */
	protected boolean isReadOnly() {
		return false;
	}

	protected abstract void fillItemView(int position, View view);

	protected abstract int getListItemLayout();
}
