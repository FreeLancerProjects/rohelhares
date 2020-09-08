package com.rohelhares.services;


import com.rohelhares.model.PlaceDirectionModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Service {
    @GET("directions/json")
    Call<PlaceDirectionModel> getDirection(@Query("origin") String origin,
                                           @Query("destination") String destination,
                                           @Query("transit_mode") String transit_mode,
                                           @Query("key") String key
    );


}