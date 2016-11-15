package com.example.islam.ubclone;

import com.example.islam.POJO.DriversResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by islam on 11/15/16.
 */
public interface RestService {
    @GET("passenger_api/get_drivers")
    Call<DriversResponse> getDrivers(@Query("location") String location);
}

