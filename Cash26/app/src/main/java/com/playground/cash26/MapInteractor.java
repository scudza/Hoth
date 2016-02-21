package com.playground.cash26;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Arrays;

import retrofit2.Response;

/**
 * Created by Jarred on 2016/02/17.
 */
public class MapInteractor implements Http.Callback {

	private static final String TAG = "MapInteractor";
	private static final long REQUEST_TIME = 500L;
	public static final int MAX_DISPLAYED_BANKS = 15;
	private long lastRequestTime;
	private Http client;
	private Callback mapInteractorCallback;
	private String apiKey;
	private boolean directionsLock = false;

	public MapInteractor(Callback callback, String key) {
		this.mapInteractorCallback = callback;
		apiKey = key;
		client = new Http(this);
	}

	public void onCameraChanged(LatLngBounds cameraBounds) {
		if (lastRequestTime + REQUEST_TIME <= System.currentTimeMillis()) {
			String lat2 = Double.toString(cameraBounds.northeast.latitude);
			String lon2 = Double.toString(cameraBounds.northeast.longitude);
			String lat1 = Double.toString(cameraBounds.southwest.latitude);
			String lon1 = Double.toString(cameraBounds.southwest.longitude);
			String query = "((" + lat1 + "," + lon1 + "),(" + lat2 + "," + lon2 + "))";
			lastRequestTime = System.currentTimeMillis();
			client.getBanksInVisibleRegion(query);
		}
	}

	public void onNavigateToBank(LatLng destination, Location currentLocation) {
		String currentLocationString = Double.toString(currentLocation.getLatitude()) + "," + Double.toString(currentLocation.getLongitude
				());
		String destinationLocation = Double.toString(destination.latitude) + "," + Double.toString(destination.longitude);
		Log.d(TAG, currentLocationString + "," + destinationLocation);
		client.getDirectionsTobank(currentLocationString, destinationLocation, apiKey);
	}

	@Override
	public void onResponseReceived(Response<Bank[]> response) {
		Log.d(TAG, response.body().length + " banks in your area");
		if (response.body().length != 0) {
			if (response.body().length > MAX_DISPLAYED_BANKS) {
				Bank[] newArray = Arrays.copyOfRange(response.body(), 0, MAX_DISPLAYED_BANKS);
				mapInteractorCallback.onBanksReceived(newArray);
			}
		} else {
			mapInteractorCallback.onBanksReceived(response.body());
		}
	}

	@Override
	public void onDirectionsReceived(OverViewPolyLine encodedPolyLine) {
		if (!encodedPolyLine.points.isEmpty()) {
			Log.d(TAG, "encoded string " + encodedPolyLine.points);
			mapInteractorCallback.onDirectionsReceived(encodedPolyLine.points);
		} else {
			Log.d(TAG, "empty string ");
		}
	}

	@Override
	public void onFailure(int responseCode) {
		mapInteractorCallback.onFailure();
	}

	public interface Callback {

		void onBanksReceived(Bank[] banks);

		void onDirectionsReceived(String encodedPolyLine);

		void onFailure();
	}
}
