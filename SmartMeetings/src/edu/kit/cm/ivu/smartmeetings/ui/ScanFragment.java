package edu.kit.cm.ivu.smartmeetings.ui;

import edu.kit.cm.ivu.smartmeetings.ui.util.IScanCallback;

/**
 * Abstract implementation of the {@link FrontendFragment} with capabilities to
 * scan QR-codes and NFC-tags. Once started NFC-tags will be scanned
 * automatically. To scan QR-codes call {@link #readQrCode()}. The result of the
 * scan will be passed to {@link #received(String)}.
 * 
 * @author Kirill Rakhman
 */
public abstract class ScanFragment extends FrontendFragment implements
		IScanCallback {

	@Override
	public void onStart() {
		super.onStart();
		getFrontend().setNfcListener(this);
		getFrontend().setQRListener(this);
	}

	@Override
	public void scanSuccessful(final String data) {
		received(data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// When finished the scanning should be turned off
		getFrontend().setNfcListener(null);
		getFrontend().setQRListener(null);
	}

	/**
	 * @see IFrontend#readQRCode()
	 */
	protected void readQrCode() {
		getFrontend().readQRCode();
	}

	/**
	 * Called whe we received the data we wanted.
	 * 
	 * @param data
	 *            The data we received from the scanner.
	 */
	protected abstract void received(String data);

}