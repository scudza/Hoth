package com.playground.cash26;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Jarred on 2016/02/17.
 */
public interface CommunicationInterface {

	@GET("/filialfinder/get_stores?")
	Call<Bank[]> getBanks(@Query("map_bounds") String bounds);

	@GET("https://maps.googleapis.com/maps/api/directions/json?")
	Call<Routes[]> getDirections(@Query("origin") String OrigLatLng,@Query("destination") String destLatLng,@Query("key") String key);

}
