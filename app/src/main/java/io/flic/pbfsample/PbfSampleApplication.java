package io.flic.pbfsample;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;


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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


import io.flic.poiclib.*;

public class PbfSampleApplication extends Application {
	public static final String TAG = "PbfSampleApplication";

	@Override
	public void onCreate() {
		super.onCreate();


		startService(new Intent(this.getApplicationContext(), PbfSampleService.class));

		FlicManager.init(this.getApplicationContext(), "2125f7c3-0d0e-42d5-88fe-fda8765867d6", "94d6448c-22d3-4d2e-951f-f625f60f471a");

		FlicManager manager = FlicManager.getManager();

		for (FlicButton button : manager.getKnownButtons()) {
			button.connect();
			listenToButtonWithToast(button);
		}
	}


	public void listenToButtonWithToast(FlicButton button) {
		button.addEventListener(new FlicButtonAdapter() {
			@Override
			public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
				if (isDown) {


					View v = LayoutInflater.inflate(R.layout.activity_main);
					View innerView = v.findViewById(R.id.number);

					Toast.makeText(getApplicationContext(), "Button " + button + " was pressed", Toast.LENGTH_SHORT).show();
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:017702329065"));
					startActivity(callIntent);


				}
			}
		});
	}
}

