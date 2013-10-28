package edu.kit.cm.ivu.smartmeetings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.services.smartmeetings.model.Address;
import com.google.api.services.smartmeetings.model.Building;
import com.google.api.services.smartmeetings.model.Discussion;
import com.google.api.services.smartmeetings.model.Reservation;
import com.google.api.services.smartmeetings.model.Room;
import com.google.api.services.smartmeetings.model.RoomProperty;

import edu.kit.cm.ivu.smartmeetings.logic.endpoint.EndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.google.GCMHelper;
import edu.kit.cm.ivu.smartmeetings.logic.google.GoogleConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.LogicIntegrationFacade;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.IUser;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.InternalSensorConnector;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.SensorDataRetrievalService;
import edu.kit.cm.ivu.smartmeetings.logic.phonesensors.StatusReceiver;
import edu.kit.cm.ivu.smartmeetings.logic.roommanagement.RoomManagementConnector;
import edu.kit.cm.ivu.smartmeetings.logic.smartdiscussion.SmartDiscussionsConnector;
import edu.kit.cm.ivu.smartmeetings.ui.BuildingDetailsFragment;
import edu.kit.cm.ivu.smartmeetings.ui.BuildingListFragment;
import edu.kit.cm.ivu.smartmeetings.ui.CreateDiscussionFragment;
import edu.kit.cm.ivu.smartmeetings.ui.DiscussionFragment;
import edu.kit.cm.ivu.smartmeetings.ui.DiscussionListFragment;
import edu.kit.cm.ivu.smartmeetings.ui.EmptyFragment;
import edu.kit.cm.ivu.smartmeetings.ui.ExerciseScanFragment;
import edu.kit.cm.ivu.smartmeetings.ui.FrontendFragment;
import edu.kit.cm.ivu.smartmeetings.ui.GetTopicInfoWorker;
import edu.kit.cm.ivu.smartmeetings.ui.IFrontend;
import edu.kit.cm.ivu.smartmeetings.ui.ReserveRoomFragment;
import edu.kit.cm.ivu.smartmeetings.ui.RoomDetailsFragment;
import edu.kit.cm.ivu.smartmeetings.ui.RoomListFragment;
import edu.kit.cm.ivu.smartmeetings.ui.RoomLogoffFragment;
import edu.kit.cm.ivu.smartmeetings.ui.RoomLogoffFragment.ResultListener;
import edu.kit.cm.ivu.smartmeetings.ui.RoomScanFragment;
import edu.kit.cm.ivu.smartmeetings.ui.SearchPropertiesFragment;
import edu.kit.cm.ivu.smartmeetings.ui.ShareDiscussionFragment;
import edu.kit.cm.ivu.smartmeetings.ui.StartScreenFragment;
import edu.kit.cm.ivu.smartmeetings.ui.util.GoogleUser;
import edu.kit.cm.ivu.smartmeetings.ui.util.ScanningActivity;

/**
 * This is the main activity class. It handles the generation of the
 * {@link IAsyncLogicFacade} and it handles the content fragments. It implements
 * {@link IFrontend} and is passed on to all the fragments a sdrontend, so that
 * they can interact with the activity and change their content if they want to.
 * 
 * @author Michael Zangl
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SmartMeetingsStartActivity extends ScanningActivity implements
		IFrontend {

	private static final String USERINFO_SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	/**
	 * A constant indicating that the help text should be left unchanged when
	 * the fragments change.
	 */
	private static final int HELP_TEXT_NOCHANGE = -1;
	private static final int REQUEST_GOOGLE_ACCOUNT = 1;
	protected static final int REQUEST_AUTHORIZATION = 2;

	/**
	 * The logic fasade we are using.
	 */
	private ISyncLogicFacade logicFacade;

	/**
	 * The helper which registers the app with GCM.
	 */
	@SuppressWarnings("unused")
	private GCMHelper gcmHelper;

	/**
	 * The current help text that is displayed.
	 */
	private String helpText;

	/**
	 * The progressCounter counts the amount of method calls of the method
	 * showProgress
	 */
	private int progressCounter = 0;

	/**
	 * Which category the app is currently in. 0 for reservations, 1 for
	 * discussions.
	 */
	private int current_category = -1;

	/**
	 * The status receiver for the sensor retrieval service
	 */
	private StatusReceiver sensingStatusReceiver;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prepareGoogleAccount();
		createLogicFacade();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.layout_split);
		prepareActionbar();
		registerSensingListener();

		if (savedInstanceState == null) {

		} else {
			helpText = savedInstanceState.getString("helpText");
			showHelpOnRightSide();
			// current_category = savedInstanceState.getInt("current_category");
			getActionBar().setSelectedNavigationItem(current_category);
		}

		handleIntent(getIntent());

		final NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(this);
		if (defaultAdapter != null) {
			defaultAdapter.setOnNdefPushCompleteCallback(null, this);
		}

		final ActionBar actionBar = getActionBar();
		final FragmentManager fm = getFragmentManager();

		fm.addOnBackStackChangedListener(new OnBackStackChangedListener() {

			@Override
			public void onBackStackChanged() {
				final boolean backEnabled = fm.getBackStackEntryCount() > 0;
				actionBar.setDisplayHomeAsUpEnabled(backEnabled);
				actionBar.setHomeButtonEnabled(backEnabled);
			}
		});

		gcmHelper = new GCMHelper(this);
	}

	private void prepareActionbar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		final String[] categories = new String[] {
				getString(R.string.category_reservations),
				getString(R.string.category_discussions) };

		actionBar.setListNavigationCallbacks(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, categories),
				new OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(
							final int itemPosition, final long itemId) {
						invalidateOptionsMenu();

						if (current_category == itemPosition) {
							return true;
						}

						current_category = itemPosition;

						emptyBackStack();

						if (!isGoogleAccountPreparing) {
							switch (itemPosition) {
							case 0:
								showStartScreen();
								break;
							case 1:
								showDiscussions();
								break;
							}
						}

						return true;
					}
				});
	}

	/**
	 * Flag indicating whether the Google account selection is ongoing.
	 */
	private boolean isGoogleAccountPreparing = true;

	/**
	 * Checks if a Google account is saved in the preferences. Otherwise checks
	 * if there is only one Google account available. If that's the case the
	 * Google account gets saved in the preferences. If there is more than one
	 * Google account available an account chooser dialog is launched.
	 */
	private void prepareGoogleAccount() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String account = prefs.getString("account", null);
		if (account == null) {

			final Account[] googleAccounts = AccountManager.get(this)
					.getAccountsByType("com.google");

			if (googleAccounts.length == 1) {
				account = googleAccounts[0].name;
				saveGoogleAccount(account);
				isGoogleAccountPreparing = false;
			} else if (googleAccounts.length > 1) {
				// Show account chooser
				final Intent accountIntent = AccountPicker
						.newChooseAccountIntent(
								null,
								null,
								new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE },
								true, null, null, null, null);

				startActivityForResult(accountIntent, REQUEST_GOOGLE_ACCOUNT);
			}
		} else {
			isGoogleAccountPreparing = false;
		}

		if (!isGoogleAccountPreparing) {
			final String userName = prefs.getString("account_userName", null);

			if (userName == null) {
				sendAuthTokenToServer(account);
			}
		}
	}

	/**
	 * Write the name of the Google account to the preferences.
	 * 
	 * @param accountName
	 */
	private void saveGoogleAccount(final String accountName) {
		if (accountName == null) {
			return;
		}

		final Editor editor = PreferenceManager.getDefaultSharedPreferences(
				this).edit();

		editor.putString("account", accountName);
		editor.commit();

		logicFacade = null;

		sendAuthTokenToServer(accountName);
		// "https://www.googleapis.com/auth/userinfo.profile"

	}

	private void sendAuthTokenToServer(final String accountName) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(final Void... params) {
				try {
					AccountManager.get(SmartMeetingsStartActivity.this)
							.invalidateAuthToken("com.google", null);

					final String token = GoogleAuthUtil.getToken(
							SmartMeetingsStartActivity.this, accountName,
							USERINFO_SCOPE);
					createLogicFacade().setUsername(token);
					// TODO was passiert, wenn der name nicht gesetzt wird
				} catch (final UserRecoverableAuthException e) {
					e.printStackTrace();
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (final IOException e) {
					e.printStackTrace();
				} catch (final GoogleAuthException e) {
					e.printStackTrace();
				}
				return null;
			}

		}.execute();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
		case REQUEST_GOOGLE_ACCOUNT:
			// Account chooser result
			if (intent != null && intent.getExtras() != null) {
				final String accountName = intent.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					saveGoogleAccount(accountName);
					if (isGoogleAccountPreparing) {
						isGoogleAccountPreparing = false;
						logicFacade = null;
						createLogicFacade();
						showStartScreen();
					}
				}

			}
			break;
		case REQUEST_AUTHORIZATION:
			final SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			final String account = prefs.getString("account", null);
			sendAuthTokenToServer(account);
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("helpText", helpText);
		outState.putInt("current_category", current_category);
	}

	/**
	 * Checks whether we use two column layout or not. The decision is taken by
	 * Android by loading the right layout file.
	 * 
	 * @return <code>true</code> if we use the two column layout,
	 *         <code>false</code> there is only one column (the left one)
	 */
	private boolean useTwoColumnLayout() {
		return findViewById(R.id.layout_split_right) != null;
	}

	/**
	 * Shows a fragment on the screen.
	 * 
	 * @param fragment
	 *            The fragment to display.
	 * @param onLeftSide
	 *            A flag indicating if it should be placed on the left side in
	 *            the two column layout.
	 * @param helpTextId
	 *            The id of the help text that should be used. May be
	 *            {@link #HELP_TEXT_NOCHANGE} to indicate that the help text
	 *            should not be changed, or 0 for no help text.
	 * @param addToBackstack
	 *            If <code>true</code>, the back key will return to the previous
	 *            fragment.
	 */
	private void showFragment(final Fragment fragment,
			final boolean onLeftSide, final int helpTextId,
			final boolean addToBackstack) {
		showFragment(fragment, onLeftSide, helpTextId, addToBackstack, null);
	}

	/**
	 * Shows a fragment on the screen.
	 * 
	 * @param fragment
	 *            The fragment to display.
	 * @param onLeftSide
	 *            A flag indicating if it should be placed on the left side in
	 *            the two column layout.
	 * @param helpTextId
	 *            The id of the help text that should be used. May be
	 *            {@link #HELP_TEXT_NOCHANGE} to indicate that the help text
	 *            should not be changed, or 0 for no help text.
	 * @param addToBackstack
	 *            If <code>true</code>, the back key will return to the previous
	 *            fragment.
	 * @param tag
	 *            Tag to identify fragment
	 */
	private void showFragment(final Fragment fragment,
			final boolean onLeftSide, final int helpTextId,
			final boolean addToBackstack, final String tag) {

		Log.d("SmartMeetingsStartActivity", "showFragment " + fragment
				+ " left " + onLeftSide + " backstack " + addToBackstack);

		final boolean displayOnRightSide = !onLeftSide && useTwoColumnLayout();

		if (helpTextId == 0) {
			helpText = null;
		} else if (helpTextId != HELP_TEXT_NOCHANGE) {
			helpText = getResources().getString(helpTextId);
		}

		final FragmentManager fragmentManager = getFragmentManager();
		final FragmentTransaction transaction = fragmentManager
				.beginTransaction();
		if (displayOnRightSide) {
			transaction.replace(R.id.layout_split_right, fragment, "right");
		} else {
			transaction.replace(R.id.layout_split_left, fragment, "left");

			if (useTwoColumnLayout()) {
				final EmptyFragment rfragment = new EmptyFragment();
				final Bundle args = new Bundle();
				if (helpText != null) {
					args.putString(EmptyFragment.ARGUMENT_TEXT, helpText);
				}
				rfragment.setArguments(args);
				transaction
						.replace(R.id.layout_split_right, rfragment, "right");
			}

			if (addToBackstack) {
				transaction.addToBackStack("_");
			}
		}
		transaction.commit();

		fragmentManager.executePendingTransactions();
	}

	private void showDialogFragment(final DialogFragment dialogFragment) {
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		final Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		dialogFragment.show(ft, "dialog");
	}

	private void showHelpOnRightSide() {
		if (useTwoColumnLayout()) {
			final FragmentManager fragmentManager = getFragmentManager();
			final FragmentTransaction transaction = fragmentManager
					.beginTransaction();
			final EmptyFragment rfragment = new EmptyFragment();
			final Bundle args = new Bundle();
			if (helpText != null) {
				args.putString(EmptyFragment.ARGUMENT_TEXT, helpText);
			}
			rfragment.setArguments(args);
			transaction.replace(R.id.layout_split_right, rfragment, "right");
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.activity_smart_meetings_start, menu);

		if (useTwoColumnLayout()) {
			menu.findItem(R.id.menu_help).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			if (helpText != null) {
				final EmptyFragment fragment = new EmptyFragment();
				final Bundle args = new Bundle();
				args.putString(EmptyFragment.ARGUMENT_TEXT, helpText);
				fragment.setArguments(args);
				showFragment(fragment, true, 0, true);
			}
			return true;
		case android.R.id.home:
			getFragmentManager().popBackStack();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, PreferenceActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public ISyncLogicFacade createLogicFacade() {
		if (logicFacade == null) {
			final EndpointConnector endpointConnector = new EndpointConnector(
					this);
			logicFacade = new LogicIntegrationFacade(
					new RoomManagementConnector(endpointConnector),
					new GoogleConnector(this), new SmartDiscussionsConnector(
							endpointConnector), new InternalSensorConnector(
							this, endpointConnector), endpointConnector);
		}

		return logicFacade;
	}

	@Override
	public void showStartScreen() {
		final FrontendFragment startScreen = new StartScreenFragment();

		showFragment(startScreen, true, R.string.help_start, false);
	}

	@Override
	public void showSearchDialog() {
		final FrontendFragment propertiesFragment = new SearchPropertiesFragment();

		showFragment(propertiesFragment, true, R.string.help_search, true);
	}

	@Override
	public void showBuildingList() {
		final BuildingListFragment listFragment = new BuildingListFragment();
		showFragment(listFragment, true, R.string.help_buildings, true);
	}

	@Override
	public void showBuildingDetails(final Building building) {
		final Bundle bundle = new Bundle();
		bundle.putString("building", building.toString());
		final BuildingDetailsFragment listFragment = new BuildingDetailsFragment();
		listFragment.setArguments(bundle);

		showFragment(listFragment, true, R.string.help_room_details, true);
	}

	@Override
	public void searchRoom(final List<RoomProperty> forWhat) {
		final Bundle bundle = new Bundle();
		final String[] argArr = new String[forWhat.size()];

		for (int i = 0; i < forWhat.size(); i++) {
			argArr[i] = forWhat.get(i).toString();
		}
		bundle.putStringArray("searchFor", argArr);
		final RoomListFragment listFragment = new RoomListFragment();
		listFragment.setArguments(bundle);
		showFragment(listFragment, true, R.string.help_room_details, true);
	}

	@Override
	public void showRoomDetails(final Room room) {
		final Bundle bundle = new Bundle();
		bundle.putString("room", room.toString());
		final FrontendFragment propertiesFragment = new RoomDetailsFragment();
		propertiesFragment.setArguments(bundle);
		showFragment(propertiesFragment, false, HELP_TEXT_NOCHANGE, true);
	}

	@Override
	public void showReservationDetails(final Reservation item) {
		// Currently this only displays the room details.
		showRoomDetails(item.getRoom());
	}

	@Override
	public void showReserveRoom(final Room room) {
		final DialogFragment newFragment = ReserveRoomFragment
				.newInstance(room);
		showDialogFragment(newFragment);
	}

	@Override
	public void navigateTo(final Address address) {
		// Start google maps intent.
		final String latlon = address.getLatitude() + ","
				+ address.getLongitude();

		try {
			final Intent intent = new Intent(
					android.content.Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + latlon));
			startActivity(intent);
		} catch (final ActivityNotFoundException e) {
			// for devices without geo app
			final Intent intent = new Intent(
					android.content.Intent.ACTION_VIEW,
					Uri.parse("http://maps.google.com/maps?daddr=" + latlon));
			startActivity(intent);
		}
	}

	@Override
	public IUser getUser() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String account = prefs.getString("account", null);

		if (account != null) {
			return new GoogleUser(account);
		} else {
			return null;
		}
	}

	@Override
	public void showRoomLogin(final Reservation reservation) {
		final Bundle bundle = new Bundle();
		bundle.putString("reservation", reservation.toString());
		final RoomScanFragment fragment = new RoomScanFragment();
		fragment.setArguments(bundle);

		showFragment(fragment, true, 0, true);
	}

	@Override
	public void showRoomLogoff(final Reservation reservation) {
		final RoomLogoffFragment fragment = new RoomLogoffFragment(
				new ResultListener() {

					@Override
					public void onResult(final boolean yesSelected) {
						if (yesSelected) {
							new AsyncTask<Void, Void, Boolean>() {

								@Override
								protected Boolean doInBackground(
										final Void... params) {
									return createLogicFacade()
											.unregisterFromRoom(reservation);
								}

								@Override
								protected void onPostExecute(
										final Boolean success) {
									if (success) {
										createLogicFacade().stopSensing();
										Toast.makeText(
												SmartMeetingsStartActivity.this,
												R.string.logoff_successful,
												Toast.LENGTH_SHORT).show();
										showStartScreen();
									} else {
										Toast.makeText(
												SmartMeetingsStartActivity.this,
												R.string.logoff_failed,
												Toast.LENGTH_SHORT).show();
									}
								};
							}.execute();
						}
					}
				});
		showDialogFragment(fragment);
	}

	@Override
	public void showDiscussionScanning() {
		showFragment(new ExerciseScanFragment(), false, 0, true, "scan");
	}

	@Override
	public void showDiscussionCreation() {
		showFragment(new CreateDiscussionFragment(), false, 0, true,
				"create_discussion");
	}

	@Override
	public void showDiscussionSharing(final String id, final String name) {
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name != null ? name : id);
		final ShareDiscussionFragment fragment = new ShareDiscussionFragment();
		fragment.setArguments(bundle);

		showFragment(fragment, true, 0, true);

	}

	@Override
	public synchronized void showProgress() {
		// TODO progress after orientation change
		progressCounter++;
		setProgress();
	}

	@Override
	public synchronized void hideProgress() {
		progressCounter--;
		setProgress();
	}

	private void setProgress() {
		setProgressBarIndeterminateVisibility(progressCounter > 0);
	}

	@Override
	public void showDiscussions() {
		showFragment(new DiscussionListFragment(), true, 0, false);
	}

	@Override
	public void showDiscussion(final Discussion discussion) {
		// workaround to prevent onNavigationItemSelected being fired after
		// displaying the discussion fragment and it being overlayed by the
		// discussion list fragment
		emptyBackStack();
		showDiscussions();
		current_category = 1;
		getActionBar().setSelectedNavigationItem(1);

		final DiscussionFragment discussionFragment = new DiscussionFragment();
		final Bundle bundle = new Bundle();
		bundle.putString("topic_identifier", discussion.getTopic());
		bundle.putString("topic_publicname", discussion.getPublicName());
		discussionFragment.setArguments(bundle);
		showFragment(discussionFragment, false, 0, true);

	}

	private void registerSensingListener() {
		if (sensingStatusReceiver == null) {
			sensingStatusReceiver = new StatusReceiver() {

				@Override
				public void onStatusChanged(final int status) {
					switch (status) {
					case SensorDataRetrievalService.STARTED:
						Toast.makeText(SmartMeetingsStartActivity.this,
								R.string.sensing_started, Toast.LENGTH_SHORT)
								.show();
						break;
					case SensorDataRetrievalService.TERMINATED:
						Toast.makeText(SmartMeetingsStartActivity.this,
								R.string.sensing_terminated, Toast.LENGTH_SHORT)
								.show();
						break;
					case SensorDataRetrievalService.NO_SENSORS:
						Toast.makeText(SmartMeetingsStartActivity.this,
								R.string.no_sensors, Toast.LENGTH_SHORT).show();
						break;
					case SensorDataRetrievalService.PUBLISHING_FAILED:
						Toast.makeText(SmartMeetingsStartActivity.this,
								R.string.publishing_failed, Toast.LENGTH_SHORT)
								.show();
						break;
					}
				}
			};
			logicFacade.registerSensingStatusReceiver(sensingStatusReceiver);
		}
	}

	@Override
	public void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(final Intent intent) {
		Log.d(getClass().getSimpleName(), "new intent " + intent.getAction()
				+ " " + intent.getData());
		final String action = intent.getAction();

		if (action == null) {
			return;
		}

		if (action.equals(Intent.ACTION_VIEW)
				|| action.equals("android.nfc.action.NDEF_DISCOVERED")) {
			final Uri data = intent.getData();

			if (data == null) {
				return;
			}

			final String host = data.getHost();

			if (host.equals("rooms")) {
				showRoomForId(data);
			}

			if (host.equals("discussions")) {
				final AsyncTask<String, Void, Discussion> task = GetTopicInfoWorker
						.asTask(this);
				task.execute(data.toString());
			}

		}
	}

	private void showRoomForId(final Uri data) {
		try {
			final String roomId = URLDecoder.decode(
					data.getPath().substring(1), "utf-8");
			new AsyncTask<Void, Void, Room>() {

				@Override
				protected Room doInBackground(final Void... params) {
					return createLogicFacade().getRoomById(roomId);
				}

				@Override
				protected void onPostExecute(final Room room) {
					if (room != null) {
						showRoomDetails(room);
					}
				};
			}.execute();
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void emptyBackStack() {
		final FragmentManager fm = getFragmentManager();
		for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
			fm.popBackStack();
		}
	}

	@Override
	public Context getContext() {
		return this;
	}
}
