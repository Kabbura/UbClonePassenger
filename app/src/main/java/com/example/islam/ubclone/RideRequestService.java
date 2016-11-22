package com.example.islam.ubclone;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.POJO.LoginResponse;
import com.example.islam.concepts.Ride;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RideRequestService extends Service {
    private PrefManager prefManager;
    private final static String TAG = "RideRequestService";
    private Handler handler;
    int i = 0;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public RideRequestService() {
        //super("RideRequestService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onHandleIntent(intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;
    }

    //    @Override
    protected void onHandleIntent(Intent intent) {
        prefManager = new PrefManager(this);

//        Toast.makeText(this, "I am called.", Toast.LENGTH_LONG).show();
        String requestID = intent.getStringExtra("request_id");
        Log.d(TAG, "onHandleIntent: Got request_id: "+ requestID);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestService service = retrofit.create(RestService.class);
        String email, password;
        if (!prefManager.isLoggedIn()){
            return;
        }
        email = prefManager.getUser().getEmail();
        password = prefManager.getUser().getPassword();
        Call<DriverResponse> call = service.getDriver("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),"","","",true,"","", requestID);
        handler = new Handler();
        Log.d(TAG, "run: Calling handler ");
        callable(call);

//        call.enqueue(new );
    }
    public void callable(final Call<DriverResponse> call){
        Log.d(TAG, "callable: call: "+call.request().toString());
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "run: I am called for "+ i++ +" service: "+call.toString());
               call.enqueue(new Callback<DriverResponse>() {
                   @Override
                   public void onResponse(Call<DriverResponse> call, Response<DriverResponse> response) {
                       if (response.isSuccessful()){
                           Log.d(TAG, "onResponse: is successful");
                           // There are 4 situations here:
                           // Status 0: When request is pending. request_id is returned.
                           // Status 1: No driver found
                           // Status 5: When this request has a non pending status. Return status in the error_msg
                           // Status 6: When a request is already accepted. Return request id in the error_msg
                           switch (response.body().getStatus()){
                               case 0:
                                   Log.i(TAG, "onResponse: status 0. Trying again in 30 seconds");
                                   callable(call.clone());
                                   break;
                               case 1:
                                   Toast.makeText(RideRequestService.this, "Sorry, all drivers are busy. Try again later.", Toast.LENGTH_LONG).show();
                                   Log.i(TAG, "onResponse: status 1. No drivers available.");
                                   return;
                               case 5:
                                   prefManager.setRideStatus(PrefManager.NO_RIDE);
                                   return;
                               case 6:
                                   prefManager.setRideStatus(PrefManager.ON_GOING_RIDE);
                           }

                       } else {
                           Toast.makeText(RideRequestService.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                       }

                   }

                   @Override
                   public void onFailure(Call<DriverResponse> call, Throwable t) {
                       Log.i(TAG, "onFailure: Failed to connect. Trying again in 30 seconds");
                       callable(call.clone());
                   }
               });


            }
        }, 1000);
    }
}
