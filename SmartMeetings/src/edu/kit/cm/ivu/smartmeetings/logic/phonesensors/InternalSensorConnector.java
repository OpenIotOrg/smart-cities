package edu.kit.cm.ivu.smartmeetings.logic.phonesensors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IInternalSensorConnector;

/**
 * This class represents a simple access point to the {@link SensorDataRetrievalService} functions.
 * @author David Kulicke
 *
 */
public class InternalSensorConnector implements IInternalSensorConnector {

	
	private final Activity activity;
	
	private final IEndpointConnector endpointConnector;
	
	/**
	 * Constructs a new {@link InternalSensorConnector}
	 * @param activity
	 */
	public InternalSensorConnector(final Activity activity, final IEndpointConnector endpointConnector) {
		this.activity = activity;
		this.endpointConnector = endpointConnector;
	}
	
	@Override
	public void registerSensingStatusReceiver(BroadcastReceiver receiver) {
		if (receiver == null) {
			throw new IllegalArgumentException("Receiver must not be null.");
		}
		
		IntentFilter filter = new IntentFilter(SensorDataRetrievalService.STATUS_CHANGED);
		LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter);
	}

	@Override
	public void startSensing(int refreshRate, String roomURI) {
		//only one sensing instance is allowed at the same time
		stopSensing();
		Intent startSensingIntent = new Intent(activity, SensorDataRetrievalService.class);
		startSensingIntent.putExtra("refreshRate", refreshRate);
		startSensingIntent.putExtra("publisher", new BackendPublisher(endpointConnector, roomURI));
		//startSensingIntent.putExtra("publisher", new DirectHTTPPublisher("http://192.168.1.100/GSNWrapper"));
		activity.startService(startSensingIntent);
	}

	@Override
	public void stopSensing() {
		Intent stopIntent = new Intent(activity,
				SensorDataRetrievalService.class);
		activity.stopService(stopIntent);
	}

}
