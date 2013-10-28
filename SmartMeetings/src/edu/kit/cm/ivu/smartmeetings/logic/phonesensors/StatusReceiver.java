package edu.kit.cm.ivu.smartmeetings.logic.phonesensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class StatusReceiver extends BroadcastReceiver {

	public abstract void onStatusChanged(int status);

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final int status = intent.getIntExtra("status", -1);
		onStatusChanged(status);
	}
}