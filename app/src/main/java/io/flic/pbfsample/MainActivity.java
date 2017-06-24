package io.flic.pbfsample;

import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import io.flic.poiclib.FlicButton;
import io.flic.poiclib.FlicButtonAdapter;
import io.flic.poiclib.FlicButtonListener;
import io.flic.poiclib.FlicButtonMode;
import io.flic.poiclib.FlicManager;
import io.flic.poiclib.FlicScanWizard;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener{

	HashMap<FlicButton, FlicButtonListener> listeners = new HashMap<>();

	private GoogleApiClient mGoogleApiClient;
	public static final String TAG = MainActivity.class.getSimpleName();
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationRequest mLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		for (FlicButton button : FlicManager.getManager().getKnownButtons()) {
			setupEventListenerForButtonInActivity(button);
		}

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(10 * 1000)        // 10 seconds, in milliseconds
				.setFastestInterval(1 * 1000); // 1 second, in milliseconds
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();

		for (Map.Entry<FlicButton, FlicButtonListener> entry : listeners.entrySet()) {
			entry.getKey().removeEventListener(entry.getValue());
			entry.getKey().returnTemporaryMode(FlicButtonMode.SuperActive);
		}

		// Cancels the scan wizard, if it's running
		FlicManager.getManager().getScanWizard().cancel();
	}

	private void setupEventListenerForButtonInActivity(FlicButton button) {
		FlicButtonListener listener = new FlicButtonAdapter() {
			@Override
			public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
				((TextView)findViewById(R.id.textView)).setText(isUp ? "Up" : "Down");
			}
		};
		button.addEventListener(listener);
		button.setTemporaryMode(FlicButtonMode.SuperActive);

		// Save the event listener so we can remove it later
		listeners.put(button, listener);
	}

	public void scanNewButton(View v) {
		// Disable the button until the scan wizard has finished
		v.setEnabled(false);

		((TextView)findViewById(R.id.scanWizardStatus)).setText("Press your Flic button");

		FlicManager.getManager().getScanWizard().start(new FlicScanWizard.Callback() {
			@Override
			public void onDiscovered(FlicScanWizard wizard, String bdAddr, int rssi, boolean isPrivateMode, int revision) {
				String text = isPrivateMode ? "Found private button. Hold down for 7 seconds." : "Found Flic, now connecting...";
				((TextView)findViewById(R.id.scanWizardStatus)).setText(text);
			}

			@Override
			public void onBLEConnected(FlicScanWizard wizard, String bdAddr) {
				((TextView)findViewById(R.id.scanWizardStatus)).setText("Connected. Now pairing...");
			}

			@Override
			public void onCompleted(FlicScanWizard wizard, FlicButton button) {
				findViewById(R.id.scanNewButton).setEnabled(true);
				((PbfSampleApplication)getApplication()).listenToButtonWithToast(button);

				((TextView)findViewById(R.id.scanWizardStatus)).setText("Scan wizard success!");
				setupEventListenerForButtonInActivity(button);
			}

			@Override
			public void onFailed(FlicScanWizard wizard, int flicScanWizardErrorCode) {
				findViewById(R.id.scanNewButton).setEnabled(true);
				((TextView)findViewById(R.id.scanWizardStatus)).setText("Scan wizard failed with code " + flicScanWizardErrorCode);
			}
		});
	}

	protected void onResume (){
		super.onResume();
		mGoogleApiClient.connect();
	}

	protected void onPause() {
		super.onPause();
		if (mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.i(TAG, "Location services connected.");
		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (location == null) {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);		}
		else {
			handleNewLocation(location);
		};
	}

	private void handleNewLocation(Location location) {
		Log.d(TAG, location.toString());

		double currentLatitude = location.getLatitude();
		double currentLongitude = location.getLongitude();
		LatLng latLng = new LatLng(currentLatitude, currentLongitude);
		TextView username = (TextView) findViewById(R.id.textView2);
		username.setText("Current loc is " + latLng);


	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i(TAG, "Location services suspended. Please reconnect.");
	}


	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		handleNewLocation(location);
	}
}