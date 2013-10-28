package edu.kit.cm.ivu.smartmeetings.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import edu.kit.cm.ivu.smartmeetings.R;

public class RoomLogoffFragment extends DialogFragment {

	private View view;

	/**
	 * An interface which can be used to create a callback listener for the
	 * {@link RoomLogoffFragment}.
	 * 
	 * @author David Kulicke
	 * 
	 */
	public interface ResultListener {

		/**
		 * This method is called when the user has touched one of the dialog's
		 * buttons.
		 * 
		 * @param yesSelected
		 *            true if the user has selected "yes", false if they
		 *            selected "no"
		 */
		public void onResult(boolean yesSelected);
	}

	private final ResultListener listener;

	/**
	 * Constructs a new {@link RoomLogoffFragment} with the given listener.
	 * 
	 * @param listener
	 *            the listener which will be used for callbacks
	 */
	public RoomLogoffFragment(final ResultListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException(
					"Result listener must not be null");
		}
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity());
		final OnClickListener buttonClickListener = new OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					listener.onResult(true);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					listener.onResult(false);
				}
			}
		};
		dialogBuilder.setTitle(R.string.room_logoff_confirm);
		dialogBuilder.setPositiveButton(R.string.yes, buttonClickListener);
		dialogBuilder.setNegativeButton(R.string.no, buttonClickListener);

		return dialogBuilder.create();
	}
}
