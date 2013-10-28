package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.services.smartmeetings.model.Room;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;

/**
 * This dialog lets the User select the time for a reservation he wants to take.
 * 
 * @see IFrontend#showReserveRoom(IRoom)
 * @author michael
 * 
 */
public class ReserveRoomFragment extends DialogFragment {

	private final class OkListener implements OnClickListener {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			reserveRoom();
		}
	}

	private final class AbortListener implements OnClickListener {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
		}
	}

	private static final String ARGUMENT_ROOM = "room";

	/**
	 * The frontend we can use.
	 */
	private IFrontend frontend;
	/**
	 * This stores the content view have created.
	 */
	private View view;

	private AsyncTask<Void, Void, Boolean> task;

	private final Room room;

	public ReserveRoomFragment(final Room room) {
		this.room = room;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder.setTitle(R.string.reserve_room_headline);
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		view = inflater.inflate(R.layout.reserve_room, null);

		final Calendar startDate = Calendar.getInstance();
		final Calendar endDate = Calendar.getInstance();
		endDate.add(Calendar.HOUR, 1);
		final TimePicker start = (TimePicker) view
				.findViewById(R.id.reserve_room_starttime);
		final DatePicker startDay = (DatePicker) view
				.findViewById(R.id.reserve_room_startdate);
		applyTime(startDate, start, startDay);
		((TimePicker) view.findViewById(R.id.reserve_room_endtime))
				.setIs24HourView(true);
		final TimePicker end = (TimePicker) view
				.findViewById(R.id.reserve_room_endtime);
		final DatePicker endDay = (DatePicker) view
				.findViewById(R.id.reserve_room_enddate);
		applyTime(endDate, end, endDay);
		dialogBuilder.setView(view);
		dialogBuilder.setPositiveButton(R.string.reserve_room_ok,
				new OkListener());
		dialogBuilder.setNegativeButton(R.string.reserve_room_abort,
				new AbortListener());

		return dialogBuilder.create();
	}

	/**
	 * Sets the time of a {@link DatePicker}/ {@link TimePicker}
	 * 
	 * @param date
	 *            The date we shoudl set them to.
	 * @param timePicker
	 *            The time picker to use.
	 * @param datePicker
	 *            The date picker to use.
	 */
	private static void applyTime(final Calendar date,
			final TimePicker timePicker, final DatePicker datePicker) {
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(date.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(date.get(Calendar.MINUTE));
		datePicker.updateDate(date.get(Calendar.YEAR),
				date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		final Calendar max = Calendar.getInstance();
		max.add(Calendar.YEAR, 1);
		datePicker.setMaxDate(max.getTimeInMillis());
		datePicker.setMinDate(new Date().getTime() - 1000);
	}

	/**
	 * Reserves the room with the parameters that were set.
	 */
	protected void reserveRoom() {
		if (frontend != null) {
			final ISyncLogicFacade backend = frontend.createLogicFacade();
			final Date startDate = getDate(R.id.reserve_room_startdate,
					R.id.reserve_room_starttime);
			final Date endDate = getDate(R.id.reserve_room_enddate,
					R.id.reserve_room_endtime);
			// final Room room = (Room) getArguments().getSerializable(
			// ARGUMENT_ROOM);

			// TODO in frontendfragment konvertieren

			task = new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(final Void... params) {

					backend.addReservation(room, startDate, endDate);

					return true;
				}

				@Override
				protected void onPostExecute(final Boolean result) {
					int text = R.string.reserve_room_success;

					if (!result) {
						text = R.string.reserve_room_fail;
					}

					Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT)
							.show();
				};

			}.execute();

		}
	}

	/**
	 * Extracts a date from a {@link DatePicker}/ {@link TimePicker} in our
	 * {@link #view}.
	 * 
	 * @param date
	 *            The {@link DatePicker} id.
	 * @param time
	 *            the {@link TimePicker} id.
	 * @return The date we extracted.
	 */
	private Date getDate(final int date, final int time) {
		final DatePicker startDay = (DatePicker) view.findViewById(date);
		final TimePicker startTime = (TimePicker) view.findViewById(time);
		final Calendar calendar = Calendar.getInstance();
		calendar.set(startDay.getYear(), startDay.getMonth(),
				startDay.getDayOfMonth(), startTime.getCurrentHour(),
				startTime.getCurrentMinute());
		return calendar.getTime();
	}

	@Override
	public void onAttach(final Activity activity) {
		if (activity instanceof IFrontend) {
			frontend = (IFrontend) activity;
		} else {
			throw new IllegalArgumentException(
					"Expected context activity to be a frontend.");
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		if (task != null) {
			task.cancel(false);
		}
		super.onDetach();
	}

	/**
	 * Gets the frontend that belongs to us.
	 * 
	 * @return The frontend
	 */
	protected IFrontend getFrontend() {
		return frontend;
	}

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param room
	 *            The room to create the instance for.
	 * @return A new instance of this class.
	 */
	public static ReserveRoomFragment newInstance(final Room room) {
		return new ReserveRoomFragment(room);
	}

}
