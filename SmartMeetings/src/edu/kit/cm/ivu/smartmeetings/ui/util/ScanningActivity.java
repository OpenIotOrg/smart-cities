package edu.kit.cm.ivu.smartmeetings.ui.util;

import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * This is a special Activity subclass that proides methods to intercept Scan
 * events.
 * 
 * @author Andreas Eberle
 * @author Michael Zangl
 * 
 */
public abstract class ScanningActivity extends Activity {

	/**
	 * The Tag we use for logging.
	 */
	private static final String TAG = "ScanningActivity";

	// ===========QR-Code-Scanning==========================================================================

	private IScanCallback qrListener;

	/**
	 * Starts the reading of a QR code. This starts a fill screen QR code
	 * reader.
	 * 
	 * @return A list that will contain the QR-Code results.
	 */
	public void readQRCode() {
		Log.d(TAG, "readQRCode() called");

		final IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();

	}

	public void setQRListener(final IScanCallback qrListener) {
		this.qrListener = qrListener;

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent intent) {
		if (requestCode != IntentIntegrator.REQUEST_CODE) {
			super.onActivityResult(requestCode, resultCode, intent);
			return;
		}

		final IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanResult != null) {
			// handle scan result
			final String contents = scanResult.getContents();
			if (contents != null) {
				Log.d(TAG, "QR Code read: " + contents);

				if (this.qrListener != null) {
					this.qrListener.scanSuccessful(contents);
				}
			}

		}

	}

	// ===========END OF
	// QR-Code-Scanning====================================================================

	// ===========NFC-Scanning==========================================================================

	private NfcAdapter mAdapter;
	private PendingIntent nfcPendingIntent;
	private IScanCallback nfcListener;

	public void setNfcListener(final IScanCallback nfcListener) {
		this.nfcListener = nfcListener;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* enable NFC */
		this.mAdapter = NfcAdapter.getDefaultAdapter(this);
		this.nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
				this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();

		/* activate NFC */
		if (this.mAdapter != null) {
			if (!this.mAdapter.isEnabled()) {
				Log.wtf(TAG, "NFC DISABLED!!!!!!");
			}
			this.mAdapter.enableForegroundDispatch(this, this.nfcPendingIntent,
					null, null);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		/* deactivate NFC */
		if (this.mAdapter != null) {
			this.mAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	public void onNewIntent(final Intent intent) {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			/* a NFC scan happened */
			setIntent(intent);
			resolveIntent(intent);
		} else {
			super.onNewIntent(intent);
		}
	}

	private void resolveIntent(final Intent intent) {
		final String action = intent.getAction();

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) { // check if the
																// intent is an
																// nfc intent
			handleNfcIntent(intent);
		}
	}

	private void handleNfcIntent(final Intent intent) {
		final Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null && rawMsgs.length == 1) {
			Log.i(TAG, "Found NFC!");

			final String nfcTagText = parseRawMessage(rawMsgs[0]);

			Log.i(TAG, "Message : " + nfcTagText);
			if (this.nfcListener != null) {
				this.nfcListener.scanSuccessful(nfcTagText);
			}
		} else {
			Log.i(TAG, "Found invalid NFC: " + Arrays.toString(rawMsgs));
		}
	}

	private static String parseRawMessage(final Parcelable rawMessage) {
		final int offset = 3;

		final NdefMessage ndefMessage = (NdefMessage) rawMessage;
		final byte[] bytes = ndefMessage.toByteArray();
		final String nfcTagText = new String(bytes, offset, bytes.length
				- offset);

		return nfcTagText;
	}

	// ===========END OF
	// NFC-Scanning====================================================================

}
