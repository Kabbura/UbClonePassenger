package com.wisam.driver.ubclone;

import com.wisam.driver.POJO.DriverResponse;
import com.wisam.driver.POJO.DriversResponse;
import com.wisam.driver.POJO.LoginResponse;
import com.wisam.driver.POJO.RequestsResponse;
import com.wisam.driver.POJO.SimpleResponse;
import com.wisam.driver.POJO.TimeResponse;
import com.wisam.driver.POJO.PriceResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by islam on 11/15/16.
 */
public interface RestService {
    @FormUrlEncoded
    @POST("passenger_api/login/")
    Call<LoginResponse> login(@Header("Authorization") String authorization,
                              @Field("registration_token") String registrationToken,
                              @Field("version_code") Integer versionCode);

    @FormUrlEncoded
    @POST("passenger_api/register/")
    Call<SimpleResponse> register(
            @Field("email") String email,
            @Field("fullname") String fullname,
            @Field("password") String password,
            @Field("phone") String phone,
            @Field("gender") String gender,
            @Field("registration_token") String registrationToken
    );

    @GET("passenger_api/get_drivers/")
    Call<DriversResponse> getDrivers(@Query("location") String location,
                                     @Query("count") Integer count);


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

    @GET("price/")
    Call<PriceResponse> getPrice(@Query("time") String time);


    @GET("passenger_api/update/")
    Call<SimpleResponse> updateProfile(@Header("Authorization") String authorization,
                                      @Query("fullname") String fullname,
                                      @Query("phone") String phone,
                                      @Query("password") String pasword
                                       );
}

