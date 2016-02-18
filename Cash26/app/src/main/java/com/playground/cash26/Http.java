package com.playground.cash26;

import android.util.Log;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jarred on 2016/02/17.
 */
public class Http implements Callback<Bank[]> {

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

	public void getBanksInVisibleRegion(String query) {
		Call<Bank[]> call = communicationInterface.getBanks(query);
		Log.d(TAG, query);
		call.enqueue(this);
	}

	@Override
	public void onResponse(Call<Bank[]> call, Response<Bank[]> response) {
		if (response.code() == HttpURLConnection.HTTP_OK) {
			Log.d(TAG, response.message());
			Log.d(TAG, Integer.toString(response.body().length));
			//	Log.d(TAG, response.body()[0].title);
			Log.d(TAG, call.request().url().toString());
			httpCallback.onResponseReceived(response);
		}
	}

	@Override
	public void onFailure(Call<Bank[]> call, Throwable t) {
		Log.d(TAG, t.getMessage());
		Log.d(TAG, call.request().url().toString());
		//httpCallback.onFail(call.request().headers().);
	}

	public interface Callback {

		void onResponseReceived(Response<Bank[]> response);

		void onFailure(int responseCode);
	}

}
