package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.smartmeetings.model.Reservation;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.IUser;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

/**
 * This is the start screen fragment that is displayed to the user when the
 * application starts.
 * 
 * @author Michael Zangl
 * @see IFrontend#showStartScreen()
 */
public class StartScreenFragment extends FrontendFragment {
	/**
	 * A tag for logging.
	 */
	static final String TAG = "StartScreenFragment";

	/**
	 * The view of the room reservation list.
	 */
	private ListView list;
	/**
	 * The list of reservations we are displaying.
	 */
	private ReservationListAdapter roomListAdapter;

	/**
	 * The action mode we are currently in.
	 */
	private ActionMode actionMode;
	/**
	 * The reservation the user selected to display the action mode menu.
	 */
	private Reservation activeReservation;

	/**
	 * A listener that starts the action mode when the user does a long press on
	 * a reservation.
	 * 
	 * @author Michael Zangl
	 * 
	 */
	private final class ShowReservationContextMenuListener implements
			OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(final AdapterView<?> arg0,
				final View arg1, final int pos, final long arg3) {
			if (actionMode == null) {
				activeReservation = roomListAdapter.getItem(pos);
				if (activeReservation != null) {
					roomListAdapter.setActiveHint(activeReservation);
					actionMode = getActivity().startActionMode(
							new ActionModeCallbacks());
				}
			}
			return true;
		}
	}

	/**
	 * This listener requests the Frontend to display the reservation details
	 * when the user presses on a reservation.
	 * 
	 * @author Michael Zangl
	 * 
	 */
	private final class ShowReservationDetailsListener implements
			OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> arg0, final View arg1,
				final int arg2, final long arg3) {
			final Reservation item = roomListAdapter.getItem(arg2);
			getFrontend().showReservationDetails(item);
		}
	}

	private final class ShowSearchDialogListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			getFrontend().showSearchDialog();
		}
	}

	private final class ShowBuildingsListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			getFrontend().showBuildingList();
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.startscreen, null);
		return root;
	}

	/**
	 * The {@link Callback}s used in action mode. They handle the context menu
	 * for reservations.
	 * 
	 * @author michael
	 * 
	 */
	private class ActionModeCallbacks implements Callback {

		@Override
		public boolean onActionItemClicked(final ActionMode mode,
				final MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_reservation_remove:
				if (activeReservation != null) {
					doInBackground(deleteReservationWorker);
					getBackend().stopSensing();
				}
				mode.finish();
				return true;
			case R.id.menu_reservation_login:
				if (activeReservation != null) {
					getFrontend().showRoomLogin(activeReservation);
				}
				mode.finish();
				return true;
			case R.id.menu_reservation_share:
				if (activeReservation != null) {
					doInBackground(shareWorker, activeReservation);
				}
				mode.finish();
				return true;

			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
			final MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.reservation, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {
			actionMode = null;
			loadListContent();
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode,
				final Menu menu) {
			return false;
		}

	}

	private final Worker<Reservation, Boolean> shareWorker = new Worker<Reservation, Boolean>() {

		@Override
		public Boolean doWork(final Reservation... input) {
			return getBackend().inviteFriendsToReservation(input[0]);
		}

		@Override
		public void handleResult(final Boolean success) {
			int text = R.string.share_reservation_success;

			if (!success) {
				text = R.string.share_reservation_fail;
			}

			Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
		}

	};

	@Override
	public void onViewCreated(final View root, final Bundle savedInstanceState) {
		list = (ListView) root.findViewById(R.id.startscreen_bookings);
		loadListContent();
		final TextView emptyListMessage = new TextView(getActivity());
		emptyListMessage.setText(R.string.startscreen_no_reservations);
		list.setEmptyView(emptyListMessage);
		list.setOnItemClickListener(new ShowReservationDetailsListener());
		list.setOnItemLongClickListener(new ShowReservationContextMenuListener());

		final Button allButton = (Button) root
				.findViewById(R.id.startscreen_all);
		allButton.setOnClickListener(new ShowBuildingsListener());
		final Button searchButton = (Button) root
				.findViewById(R.id.startscreen_search);
		searchButton.setOnClickListener(new ShowSearchDialogListener());
	}

	private final Worker<Void, List<Reservation>> reservationListWorker = new Worker<Void, List<Reservation>>() {

		@Override
		public List<Reservation> doWork(final Void... input) {
			final IUser user = getFrontend().getUser();

			if (user != null) {
				return getBackend().getReservationsOfUser();
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public void handleResult(final List<Reservation> result) {
			roomListAdapter = new ReservationListAdapter(getActivity(),
					getFrontend(), result);
			list.setAdapter(roomListAdapter);

		}

	};

	private final Worker<Void, Void> deleteReservationWorker = new Worker<Void, Void>() {

		@Override
		public Void doWork(final Void... input) {
			getBackend().removeReservation(activeReservation);
			return null;
		}

		@Override
		public void handleResult(final Void result) {
			doInBackground(reservationListWorker);

		}

	};

	/**
	 * (re)loads the content list of the reservation list.
	 */
	private void loadListContent() {
		doInBackground(reservationListWorker);
	}
}
