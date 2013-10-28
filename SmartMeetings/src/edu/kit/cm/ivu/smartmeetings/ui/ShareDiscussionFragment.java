package edu.kit.cm.ivu.smartmeetings.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import edu.kit.cm.ivu.smartmeetings.R;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;

/**
 * 
 * @author Kirill Rakhman
 */
public class ShareDiscussionFragment extends FrontendFragment {

	private ImageView imageView;
	private TextView title;
	private String id;
	private NfcAdapter nfc;
	private String name;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.share_discussion, menu);

		final MenuItem item = menu.findItem(R.id.share);
		final ShareActionProvider mShareActionProvider = (ShareActionProvider) item
				.getActionProvider();

		final Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, name);
		shareIntent.putExtra(Intent.EXTRA_TEXT, discussionUri);

		mShareActionProvider.setShareIntent(shareIntent);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.share_discussion_scanning, container,
				false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		imageView = (ImageView) view.findViewById(R.id.imageViewQR);
		title = (TextView) view.findViewById(R.id.textViewDiscussionName);

		id = getArguments().getString("id");
		name = getArguments().getString("name");

		discussionUri = GetTopicInfoWorker.getDiscussionUri(id, name);

		title.setText(name);

		doInBackground(downloadQRWorker, discussionUri);

		final Activity activity = getActivity();
		nfc = NfcAdapter.getDefaultAdapter(activity);
		nfc.setNdefPushMessageCallback(
				new NfcAdapter.CreateNdefMessageCallback() {
					@Override
					public NdefMessage createNdefMessage(final NfcEvent event) {

						final NdefRecord uriRecord = NdefRecord.createUri(Uri
								.parse(discussionUri));
						return new NdefMessage(new NdefRecord[] { uriRecord });
					}
				}, activity);

	}

	@Override
	public void onDestroy() {
		nfc.setNdefPushMessageCallback(null, getActivity());
		super.onDestroy();
	}

	private final Worker<String, Bitmap> downloadQRWorker = new Worker<String, Bitmap>() {

		@Override
		public Bitmap doWork(final String... input) {

			final String url = "http://qrcode.kaywa.com/img.php?s=12&d="
					+ input[0];

			try {
				final InputStream in = new URL(url).openStream();
				return BitmapFactory.decodeStream(in);
			} catch (final MalformedURLException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public void handleResult(final Bitmap result) {
			if (result != null) {
				imageView.setImageBitmap(result);
			}
		}
	};
	private String discussionUri;

}
