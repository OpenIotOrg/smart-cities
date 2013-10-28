package edu.kit.cm.ivu.smartmeetings.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.services.smartmeetings.model.Reservation;

import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.ui.util.PrettyPrint;

/**
 * This is a special list adapter that displays a list of reservations.
 * 
 * @author Michael Zangl
 * 
 */
public class ReservationListAdapter extends MyListAdapter<Reservation> {

	/**
	 * The reservation that is highlighted, probably because there is currently
	 * an open context menu.
	 */
	private Reservation activeReservation;
	/**
	 * The frontend we can use.
	 */
	private final IFrontend frontend;

	/**
	 * Creates a new reservation list adapter.
	 * 
	 * @param context
	 *            The Android context to use.
	 * @param frontend
	 *            The frontend we can use for user actions.
	 * @param reservationList
	 *            The list of reservations we display.
	 */
	public ReservationListAdapter(final Context context,
			final IFrontend frontend, final List<Reservation> reservationList) {
		super(context, reservationList);
		this.frontend = frontend;
	}

	@Override
	protected void fillItemView(final int position, final View view) {
		final Reservation item = getItem(position);
		final TextView name = (TextView) view
				.findViewById(R.id.reservation_item_title);
		name.setText(PrettyPrint.toString(item.getRoom()));

		final String description = getItemDescription(item);
		final TextView details = (TextView) view
				.findViewById(R.id.reservation_item_detail);
		details.setText(description);

		final Button button = (Button) view
				.findViewById(R.id.reservation_item_button);

		if (item.getLoginState() == true) {
			button.setText(R.string.reservation_item_logoff);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					Log.i("ReservationListAdapter", "Logoff");
					frontend.showRoomLogoff(item);
				}
			});
		} else {
			button.setText(R.string.reservation_item_login);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					Log.i("ReservationListAdapter", "Login");
					frontend.showRoomLogin(item);
				}
			});
		}

		view.setBackgroundColor(item.equals(activeReservation) ? 0xff42b3ff
				: 0x0);
	}

	/**
	 * Creates the item description String for a reservation.
	 * 
	 * @param item
	 *            The item to create the String for.
	 * @return The String.
	 */
	private String getItemDescription(final Reservation item) {
		final SimpleDateFormat date = new SimpleDateFormat(getContext()
				.getResources().getText(R.string.date_format).toString());
		final SimpleDateFormat time = new SimpleDateFormat(getContext()
				.getResources().getText(R.string.time_format).toString());

		final Date startDate = new Date(item.getStartDate().getValue());
		final Date endDate = new Date(item.getEndDate().getValue());
		final StringBuffer text = new StringBuffer();
		text.append(date.format(startDate));
		text.append(" ");
		text.append(time.format(startDate));
		text.append(" - ");
		text.append(date.format(endDate));
		text.append(" ");
		text.append(time.format(endDate));

		return text.toString();
	}

	@Override
	protected int getListItemLayout() {
		return R.layout.booked_item;
	}

	/**
	 * Sets the reservation that shoudl be highlighted.
	 * 
	 * @param activeReservation
	 *            The reservation to highlight or <code>null</code> to disable
	 *            highlighting.
	 */
	public void setActiveHint(final Reservation activeReservation) {
		this.activeReservation = activeReservation;
		notifyDataSetChanged();
	}

}
