package com.example.islam.ubclone;

import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.POJO.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by islam on 11/15/16.
 */
public interface RestService {
    @GET("passenger_api/login")
    Call<LoginResponse> login(@Header("Authorization") String authorization );

    @GET("passenger_api/register")
    Call<RegisterResponse> register(
            @Query("email") String email
    );

    @GET("passenger_api/get_drivers")
    Call<DriversResponse> getDrivers(@Query("location") String location);


    @GET("passenger_api/get_drivers")
    Call<DriverResponse> getDriver(
            @Query("pickup") String pickup,
            @Query("dest") String dest,
            @Query("time") String time,
            @Query("female_driver") Boolean female_driver,
            @Query("notes") String notes,
            @Query("price") String price,
            @Query("request_id") String request_id

            );
}

