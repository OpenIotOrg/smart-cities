package edu.kit.cm.ivu.smartmeetings.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * A ListView that scrolls down when a keyboard is opened.
 * 
 * @author Kirill Rakhman
 */
public class ScrollListView extends ListView {

	public ScrollListView(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScrollListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollListView(final Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (h < oldh) {
			setSelection(getCount());
		}
	}

}
