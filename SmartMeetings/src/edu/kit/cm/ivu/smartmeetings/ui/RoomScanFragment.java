package edu.kit.cm.ivu.smartmeetings.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.services.smartmeetings.model.Reservation;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;
import edu.kit.cm.ivu.smartmeetings.ui.util.JsonDeserializer;

/**
 * This fragment displays a dialog prompting the user to scan a Tag or use the
 * QR-Scanner as alternative.
 * 
 * @author Michael Zangl
 * @see IFrontend#showScanning(IReservation)
 * 
 */
public class RoomScanFragment extends ScanFragment {

	/**
	 * 
	 * @author Kirill Rakhman
	 */
	private final class RegisterWorker implements Worker<Reservation, Boolean> {
		@Override
		public Boolean doWork(final Reservation... input) {
			return getBackend().registerAtRoom(reservation);
		}

		@Override
		public void handleResult(final Boolean success) {

			if (success) {
				Toast.makeText(getActivity(), R.string.login_successful,
						Toast.LENGTH_SHORT).show();
				showEnableSensing();
			} else {
				Toast.makeText(getActivity(), R.string.login_failed,
						Toast.LENGTH_SHORT).show();
				getActivity().onBackPressed();
			}
		}
	}

	private final RegisterWorker registerWorker = new RegisterWorker();

	private Reservation reservation;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.scan, null);
		return root;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final String reservationString = getArguments()
				.getString("reservation");
		reservation = JsonDeserializer.deserialize(reservationString,
				Reservation.class);

		final Button qrButton = (Button) view.findViewById(R.id.scan_qr);
		qrButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				readQrCode();
			}
		});

		final Button abort = (Button) view.findViewById(R.id.scan_abort);
		abort.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				getActivity().onBackPressed();
			}
		});
	}

	@Override
	protected void received(final String data) {
		// TODO beim Einchecken prüfen, was überhaupt gescannt wurde
		if (reservation != null) {
			Log.d(StartScreenFragment.TAG, "Result of scan: " + data + " for "
					+ reservation);

			doInBackground(registerWorker, reservation);
		}

	}

	private void showEnableSensing() {
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		final Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		final EnableSensingFragment enableDialog = new EnableSensingFragment(
				new EnableSensingFragment.ResultListener() {

					@Override
					public void onResult(final boolean yesSelected,
							final int refreshRate) {
						if (yesSelected) {
							final String roomURI = reservation.getRoomId();
							getBackend().startSensing(refreshRate, roomURI);
						}
						// the dialog has to be dismissed as well as the scan
						// fragment
						getActivity().onBackPressed();
						getActivity().onBackPressed();
					}
				});
		enableDialog.show(ft, "dialog");
	}

}
