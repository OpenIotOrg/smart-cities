package edu.kit.cm.ivu.smartmeetings.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import edu.kit.cm.ivu.smartmeetings.R;

/**
 * A {@link DialogFragment} subclass which asks the user whether they want to
 * enable the sensor data retrieval from the phone's internal sensors.
 * 
 * @author David Kulicke
 * 
 */
public class EnableSensingFragment extends DialogFragment {

	private View view;

	private EditText editRefreshRate;

	/**
	 * An interface which can be used to create a callback listener for the
	 * {@link EnableSensingFragment}.
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
		 * @param refreshRate
		 *            contains the desired refresh rate in milliseconds if the
		 *            user has chosen "yes"
		 */
		public void onResult(boolean yesSelected, int refreshRate);
	}

	private final ResultListener listener;

	/**
	 * Constructs a new {@link EnableSensingFragment} which will perform
	 * callbacks to the given {@link ResultListener}.
	 * 
	 * @param listener
	 *            the listener which will be used for callbacks when the user
	 *            has touched one of the dialog's buttons
	 */
	public EnableSensingFragment(final ResultListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException(
					"ResultListener must not be null.");
		}
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity());
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		// the layout of the dialog
		view = inflater.inflate(R.layout.enable_sensing_layout, null);

		dialogBuilder.setView(view);
		editRefreshRate = (EditText) view.findViewById(R.id.editRefreshRate);

		dialogBuilder.setNegativeButton(R.string.no, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				listener.onResult(false, 0);
			}
		});
		dialogBuilder.setPositiveButton(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// the entered value represents an amount of seconds but the
				// return value for the refreshRate must be in milliseconds
				final int refreshRate = 1000 * Integer.parseInt(editRefreshRate
						.getText().toString());
				listener.onResult(true, refreshRate);
			}
		});
		return dialogBuilder.create();
	}

}
