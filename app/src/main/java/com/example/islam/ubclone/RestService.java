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
    @FormUrlEncoded
    @POST("passenger_api/login/")
    Call<LoginResponse> login(@Header("Authorization") String authorization,
                              @Field("registration_token") String registrationToken );

    @FormUrlEncoded
    @POST("passenger_api/register/")
    Call<SimpleResponse> register(
            @Field("email") String email,
            @Field("fullname") String fullname,
            @Field("password") String password,
            @Field("phone") String phone,
            @Field("gender") String gender
    );

    @GET("passenger_api/get_drivers/")
    Call<DriversResponse> getDrivers(@Query("location") String location);


    @GET("passenger_api/driver/")
    Call<DriverResponse> getDriver(@Header("Authorization") String authorization,
            @Query("pickup") String pickup,
            @Query("dest") String dest,
            @Query("time") String time,
            @Query("female_driver") Boolean female_driver,
            @Query("notes") String notes,
            @Query("price") String price,
            @Query("request_id") String request_id,
            @Query("pickup_text") String pickupText,
            @Query("dest_text") String destText

            );

    @GET("passenger_api/requests/")
    Call<RequestsResponse> getRequests(@Header("Authorization") String authorization );

    @GET("time/")
    Call<TimeResponse> getTime( );

    @FormUrlEncoded
    @POST("passenger_api/token/")
    Call<SimpleResponse> updateToken(@Header("Authorization") String authorization,
                                     @Field("registration_token") String registrationToken);

    @GET("passenger_api/cancel/")
    Call<SimpleResponse> cancelRequest(@Header("Authorization") String authorization,
                                     @Query("request_id") String requestId );

    @FormUrlEncoded
    @POST("passenger_api/arrived/")
    Call<SimpleResponse> postArrived(@Header("Authorization") String authorization,
                                     @Field("request_id") String requestId );
}

