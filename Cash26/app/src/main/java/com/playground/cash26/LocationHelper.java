package com.playground.cash26;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jarred on 2016/02/17.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

	public static final int LOCATION_PERMISSION = 0;

	private GoogleApiClient googleApiClient;
	private Location lastKnownLocation;
	private Activity activity;
	private Callback callback;

	public LocationHelper(Activity activity, Callback callback) {
		this.activity = activity;
		this.callback = callback;
		googleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		googleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
			if (lastKnownLocation != null) {
				callback.onLastKnowLocationReceived(lastKnownLocation);
			}
		} else {
			ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	public void disconnectLocationService() {
		googleApiClient.disconnect();
	}

	public interface Callback {

		void onLastKnowLocationReceived(Location location);
	}

	public Location getLastKnownLocation() {
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
		}
		return lastKnownLocation;
	}

}
