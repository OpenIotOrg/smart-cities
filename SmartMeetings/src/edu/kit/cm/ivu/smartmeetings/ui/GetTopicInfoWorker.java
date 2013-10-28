package edu.kit.cm.ivu.smartmeetings.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.services.smartmeetings.model.Discussion;

import edu.kit.cm.ivu.smartmeetings.logic.interfaces.ISyncLogicFacade;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

/**
 * Worker that parses the result of a scan and if the content has the correct
 * format of a discussion descriptor, the discussion is displayed.
 * 
 * @author Kirill Rakhman
 */
public class GetTopicInfoWorker implements Worker<String, Discussion> {

	private final IFrontend frontend;

	public GetTopicInfoWorker(final IFrontend frontend) {
		this.frontend = frontend;

	}

	@Override
	public Discussion doWork(final String... input) {

		final String scanIdentifier = input[0];
		final Uri uri = Uri.parse(scanIdentifier);

		if (!"smartmeetings".equals(uri.getScheme())) {
			return null;
		}

		if (!"discussions".equals(uri.getHost())) {
			return null;
		}

		try {
			final String topic = URLDecoder.decode(uri.getPath().substring(1),
					"utf-8");

			String name = null;

			if (uri.getQueryParameterNames().contains("name")) {
				name = URLDecoder
						.decode(uri.getQueryParameter("name"), "utf-8");
			}

			final ISyncLogicFacade backend = frontend.createLogicFacade();
			final Discussion discussion = backend.getTopicInformation(topic,
					name);

			return discussion;
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void handleResult(final Discussion result) {

		final Discussion discussion = result;
		if (result != null) {
			frontend.showDiscussion(discussion);
		} else {
			if (frontend instanceof Context) {
				Toast.makeText((Context) frontend, "Wrong format",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static AsyncTask<String, Void, Discussion> asTask(
			final IFrontend frontend) {
		final GetTopicInfoWorker worker = new GetTopicInfoWorker(frontend);

		return new AsyncTask<String, Void, Discussion>() {

			@Override
			protected Discussion doInBackground(final String... params) {
				return worker.doWork(params);
			}

			@Override
			protected void onPostExecute(final Discussion result) {
				worker.handleResult(result);
			}

		};
	}

	public static String getDiscussionUri(final String id, final String name) {
		final Builder builder = Uri.parse("smartmeetings://discussions/")
				.buildUpon().encodedPath(id);

		if (name != null) {
			builder.appendQueryParameter("name", name);
		}

		return builder.toString();
	}
}