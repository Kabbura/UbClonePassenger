package com.example.islam.ubclone;

import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.POJO.LoginResponse;
import com.example.islam.POJO.RequestsResponse;
import com.example.islam.POJO.SimpleResponse;
import com.example.islam.POJO.TimeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by islam on 11/15/16.
 */
public interface RestService {
    @GET("passenger_api/login")
    Call<LoginResponse> login(@Header("Authorization") String authorization,
                              @Query("registration_token") String registrationToken );

    @GET("passenger_api/register")
    Call<SimpleResponse> register(
            @Query("email") String email,
            @Query("fullname") String fullname,
            @Query("password") String password,
            @Query("phone") String phone,
            @Query("gender") String gender
    );

    @GET("passenger_api/get_drivers")
    Call<DriversResponse> getDrivers(@Query("location") String location);


    @GET("passenger_api/driver")
    Call<DriverResponse> getDriver(@Header("Authorization") String authorization,
            @Query("pickup") String pickup,
            @Query("dest") String dest,
            @Query("time") String time,
            @Query("female_driver") Boolean female_driver,
            @Query("notes") String notes,
            @Query("price") String price,
            @Query("request_id") String request_id

            );

    @GET("passenger_api/requests")
    Call<RequestsResponse> getRequests(@Header("Authorization") String authorization );

    @GET("time")
    Call<TimeResponse> getTime( );

    @GET("token")
    Call<SimpleResponse> updateToken(@Header("Authorization") String authorization,
                                     @Query("registration_token") String registrationToken);

    @FormUrlEncoded
    @POST("cancel")
    Call<SimpleResponse> cancelRequest(@Header("Authorization") String authorization,
                                     @Field("request_id") String requestId );
}

