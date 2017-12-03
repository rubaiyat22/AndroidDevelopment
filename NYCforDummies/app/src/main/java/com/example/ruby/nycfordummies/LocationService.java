package com.example.ruby.nycfordummies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;


/**
 * Created by ruby on 8/10/17.
 */

//LocationService is a service that listens for GPS in the background and sends Broadcast recievers with the GPS data
public class LocationService extends Service implements LocationListener {
	private LocationManager locationManager;
	private Location location;
	// The minimum distance to change Updates in meters
	public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

	// The minimum time between updates in milliseconds
	public static final long MIN_TIME_BW_UPDATES = 5000; // 5 seconds

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onCreate() {
		generateLocation();
	}

	private void generateLocation() {
		locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		//If GPS is turned on
		if (gpsEnabled) {
			//noinspection MissingPermission
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
			if (locationManager != null) {
				//noinspection MissingPermission
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					Intent intent = new Intent("LOCATION_UPDATE");
					intent.putExtra("LATITUDE", location.getLatitude());
					intent.putExtra("LONGITUDE", location.getLongitude());
					sendBroadcast(intent);
				}
			}
			//If only Network connection is turned
		} else if (networkEnabled) {
			//noinspection MissingPermission
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
			if (locationManager != null) {
				//noinspection MissingPermission
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					Intent intent = new Intent("LOCATION_UPDATE");
					intent.putExtra("LATITUDE", location.getLatitude());
					intent.putExtra("LONGITUDE", location.getLongitude());
					sendBroadcast(intent);
				}
			}
		} else {
			Toast.makeText(this, "No provider available; Please turn on a location provider. ", Toast.LENGTH_LONG).show();
			handleNoProvider();
		}
	}

	//This method handles the event where both GPS and Networks in turned off
	public void handleNoProvider() {
		//Creates a new intent to start Settings of the app
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (locationManager != null) {//Stop using GPS Listener
			locationManager.removeUpdates(LocationService.this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//noinspection MissingPermission
		locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		//noinspection MissingPermission
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
		if (locationManager != null) {
			//noinspection MissingPermission
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (location != null) {
				Intent intent = new Intent("LOCATION_UPDATE");
				intent.putExtra("LATITUDE", location.getLatitude());
				intent.putExtra("LONGITUDE", location.getLongitude());
				sendBroadcast(intent);
			}
		}

	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {
	}

	@Override
	public void onProviderDisabled(String s) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);

	}

}
