package com.playground.cash26;

import android.util.Log;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jarred on 2016/02/17.
 */
public class Http {

	private static final String TAG = "HTTP";
	private static final String BASE_URL = "https://www.barzahlen.de";
	private CommunicationInterface communicationInterface;
	Callback httpCallback;

	public Http(Callback callback) {
		httpCallback = callback;
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		communicationInterface = retrofit.create(CommunicationInterface.class);

	}

	public void getDirectionsTobank(String originLatLng, String destinationLatLng, String apiKey) {
		Call<DirectionsResponse> call = communicationInterface.getDirections(originLatLng, destinationLatLng, apiKey);
		call.enqueue(new retrofit2.Callback<DirectionsResponse>() {
			@Override
			public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
				if (response.code() == HttpURLConnection.HTTP_OK) {
					if (response.body() != null) {
						Log.d(TAG, "Response received " + response.body().status);
						httpCallback.onDirectionsReceived(response.body().routes[0].overview_polyline);
					} else {
						Log.d(TAG, "Response received null body");

					}
				}
			}

			@Override
			public void onFailure(Call<DirectionsResponse> call, Throwable t) {
				Log.d(TAG, " failed to get directions " + t.getMessage());
				Log.d(TAG, " failed to get directions " + call.request().url().toString());
				httpCallback.onFailure(0);
			}
		});
	}

	public void getBanksInVisibleRegion(String query) {
		Call<Bank[]> call = communicationInterface.getBanks(query);
		Log.d(TAG, query);
		call.enqueue(new retrofit2.Callback<Bank[]>() {
			@Override
			public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
				if (response.code() == HttpURLConnection.HTTP_OK) {
					Log.d(TAG, response.message());
					Log.d(TAG, call.request().url().toString());
					httpCallback.onResponseReceived(response);
				}
			}

			@Override
			public void onFailure(Call<Bank[]> call, Throwable t) {
				Log.d(TAG, t.getMessage());
				Log.d(TAG, call.request().url().toString());
				httpCallback.onFailure(0);
			}
		});
	}

	public interface Callback {

		void onResponseReceived(Response<Bank[]> response);

		void onFailure(int responseCode);

		void onDirectionsReceived(OverViewPolyLine overviewPolyLine);
	}

}
