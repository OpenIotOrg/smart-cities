package edu.kit.cm.ivu.smartmeetings.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import edu.kit.cm.ivu.smartmeetings.R;

public class ExerciseScanFragment extends ScanFragment {

	private GetTopicInfoWorker getTopicInfoWorker;

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		getTopicInfoWorker = new GetTopicInfoWorker(getFrontend());
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.scan, null);
		return root;
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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
		doInBackground(getTopicInfoWorker, data);
	}
}
