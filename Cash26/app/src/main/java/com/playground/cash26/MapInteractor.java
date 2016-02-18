package com.playground.cash26;

import android.util.Log;

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

	public MapInteractor(Callback callback) {
		this.mapInteractorCallback = callback;
		client = new Http(this);
	}

	public void onCameraChanged(LatLngBounds cameraBounds) {
		if (lastRequestTime + REQUEST_TIME <= System.currentTimeMillis()) {
			String lat2 = Double.toString(cameraBounds.northeast.latitude);
			String lon2 = Double.toString(cameraBounds.northeast.longitude);
			String lat1 = Double.toString(cameraBounds.southwest.latitude);
			String lon1 = Double.toString(cameraBounds.southwest.longitude);
			//(({lat1},{lon1}),({lat2},{lon2}))
			String query = "((" + lat1 + "," + lon1 + "),(" + lat2 + "," + lon2 + "))";
			client.getBanksInVisibleRegion(query);
		}
	}

	public void onNavigateToBank(double latitude, double longitude) {

	}

	@Override
	public void onResponseReceived(Response<Bank[]> response) {

		if (response.body().length != 0) {
			Log.d(TAG, response.body().length + " banks in your area");
			if (response.body().length > MAX_DISPLAYED_BANKS) {
				Bank[] newArray = Arrays.copyOfRange(response.body(), 0, MAX_DISPLAYED_BANKS);
				mapInteractorCallback.onBanksReceived(newArray);
			}
		} else {
			Log.d(TAG, response.body().length + " banks in your area");
		}
	}

	@Override
	public void onFailure(int responseCode) {

	}

	public interface Callback {

		void onBanksReceived(Bank[] banks);
	}
}
