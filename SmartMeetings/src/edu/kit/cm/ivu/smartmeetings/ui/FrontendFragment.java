package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.util.Log;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

/**
 * This is the base class for all frontend fragments. It handles attaching and
 * detaching the fragment from our frontend and provides convenience methods for
 * accessing the frontend or the asynchronous logic (backend).
 * 
 * @author Michael Zangl
 * @author Andreas Eberle
 * 
 */
@TargetApi(11)
public abstract class FrontendFragment extends Fragment {

	/**
	 * The frontend that we attached to.
	 */
	private IFrontend frontend = null;

	@Override
	public void onAttach(final Activity activity) {

		Log.d("SmartMeetingsStartActivity", "onAttach "
				+ getClass().getSimpleName());

		if (activity instanceof IFrontend) {
			frontend = (IFrontend) activity;
		} else {
			throw new IllegalArgumentException(
					"Expected context activity to be a frontend.");
		}
		super.onAttach(activity);
	}

	/**
	 * Gets the frontend we attached to or a dummy frontend if we did not attach
	 * to a frontend.
	 * 
	 * @return The frontent, never <code>null</code>
	 */
	public IFrontend getFrontend() {
		return frontend;
	}

	/**
	 * Convenience method to access the backend.
	 * 
	 * @return The backend.
	 */
	public ISyncLogicFacade getBackend() {
		return frontend.createLogicFacade();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("SmartMeetingsStartActivity", "onResume "
				+ getClass().getSimpleName());
	}

	@Override
	public void onDetach() {
		Log.d("SmartMeetingsStartActivity", "onDetach "
				+ getClass().getSimpleName());

		super.onDetach();
		frontend = null;

		// If the fragment gets hidden while a background task is performing,
		// the UI should not be updated with the results.
		cancelTask(null);
	}

	private final Map<Class<?>, AsyncTask<?, ?, ?>> taskMap = new HashMap<Class<?>, AsyncTask<?, ?, ?>>();

	/**
	 * When this method gets called the doWork method of the worker object gets
	 * called in a background thread. After the method returns, the result is
	 * passed to the handleResult method of the worker object on the main
	 * thread.<br>
	 * <br>
	 * If a worker is submitted while a worker of the same type is active, the
	 * latter will be canceled.
	 * 
	 * @param worker
	 *            {@link Worker} object, that handles the work
	 * @param input
	 *            Input for the worker.
	 */
	protected <I, O> void doInBackground(final Worker<I, O> worker,
			final I... input) {

		cancelTask(worker);

		final Class<?> key = worker.getClass();

		final IFrontend frontend = this.frontend;
		frontend.showProgress();

		final AsyncTask<I, Void, O> task = new AsyncTask<I, Void, O>() {
			@Override
			protected O doInBackground(final I... params) {
				return worker.doWork(params);
			}

			@Override
			protected void onPostExecute(final O result) {

				taskMap.remove(key);
				worker.handleResult(result);
				frontend.hideProgress();
			};

			@Override
			protected void onCancelled() {
				frontend.hideProgress();
			}

		};
		taskMap.put(key, task);

		task.execute(input);
	}

	/**
	 * Cancels the task with the type of the passed worker.
	 * 
	 * @param worker
	 *            {@link Worker} whose type will be checked or <code>null</code>
	 *            to cancel all tasks.
	 */
	protected <I, O> void cancelTask(final Worker<I, O> worker) {
		if (worker == null) {
			// Cancel all
			for (final Class<?> key : taskMap.keySet()) {
				taskMap.get(key).cancel(false);
			}
			taskMap.clear();
		} else {
			// Cancel specific task
			final Class<?> key = worker.getClass();
			final AsyncTask<?, ?, ?> task = taskMap.get(key);

			if (task != null) {
				task.cancel(false);
				taskMap.remove(key);
			}
		}
	}

}