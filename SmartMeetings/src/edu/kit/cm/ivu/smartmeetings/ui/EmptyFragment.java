package edu.kit.cm.ivu.smartmeetings.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.kit.cm.ivu.smartmeetings.R;

/**
 * This is an almost empty fragment. It only displays a Text that can be gibven
 * as argument.
 * 
 * @author Michael Zangl
 * @see #ARGUMENT_TEXT
 */
@TargetApi(13)
public class EmptyFragment extends Fragment {
	/**
	 * The key to use for a String argument that can pass text to this fragment.
	 */
	public static final String ARGUMENT_TEXT = "text";

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.empty_fragment, null);
		return view;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final TextView text = (TextView) view
				.findViewById(R.id.empty_fragment_text);
		final Bundle arguments = getArguments();
		if (arguments != null) {
			text.setText(arguments.getString(ARGUMENT_TEXT, ""));
		}
	}
}
