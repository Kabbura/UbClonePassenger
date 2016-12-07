package com.example.islam.concepts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.islam.POJO.Driver;
import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.POJO.SimpleResponse;
import com.example.islam.POJO.TimeResponse;
import com.example.islam.events.DriverAccepted;
import com.example.islam.events.LogoutRequest;
import com.example.islam.events.RequestCanceled;
import com.example.islam.events.RideStarted;
import com.example.islam.ubclone.MapsActivity;
import com.example.islam.ubclone.PrefManager;
import com.example.islam.ubclone.R;
import com.example.islam.ubclone.RestService;
import com.example.islam.ubclone.RestServiceConstants;
import com.example.islam.ubclone.RideRequestService;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by islam on 11/17/16.
 */
public class Ride {
    private final static String TAG = "InRideClass";
    private RestService service;
    public RideDetails details;
    private Driver driver;
    ProgressDialog progressDialog;


    public Ride() {
        //Creating Rest Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);

        // Initialize details
        details = new RideDetails();
        details.reset();

    }

    public void getDrivers(final MapsActivity mapsActivity, LatLng latLng) {
        String location = latLng.latitude +","+latLng.longitude;
        Call<DriversResponse> call = service.getDrivers(location);
        call.enqueue(new Callback<DriversResponse>() {
            @Override
            public void onResponse(Call<DriversResponse> call, Response<DriversResponse> response) {
                if (response.isSuccessful()){
                    //TODO: Check if drivers is null
                    Log.d(TAG, "onResponse: raw: " + response.raw());
                    Log.d(TAG, "onResponse: Retrofit response success: Got "+ response.body().drivers.size());
                    mapsActivity.clearDriversMarkers();
                    mapsActivity.setDriversMarkers(response.body().drivers);
                }
            }

            @Override
            public void onFailure(Call<DriversResponse> call, Throwable t) {
                Log.d(TAG, "onResponse: Retrofit response failed: "+ t.getLocalizedMessage());
            }
        });

    }

    public void makeRequest(final MapsActivity mapsActivity){

        if(details.isSet()){
            progressDialog   = new ProgressDialog(mapsActivity);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Connecting");
            progressDialog.show();

            // Validate date
            validateTime(mapsActivity);


        } else {
            Toast.makeText(mapsActivity, "Error sending request", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateTime(final MapsActivity mapsActivity) {
        if (details.now) requestDriver(mapsActivity);
        else if (details.time != null){
            Call<TimeResponse> call = service.getTime();
            Log.d(TAG, "validateTime: Request" + call.request().toString());
            call.enqueue(new Callback<TimeResponse>() {
                @Override
                public void onResponse(Call<TimeResponse> call, Response<TimeResponse> response) {
                    if (response.isSuccessful() && response.body() != null){
                        if (response.body().getTime() != null){
                            Long currentTime = response.body().getTime();
                            Long requestTime = details.time.getTime().getTime()/1000;
                            Long diff = requestTime - currentTime;
                            if (diff < -300) {
                                Toast.makeText(mapsActivity, "Invalid date", Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                return;
                            }

                            // If request is after more than 18 hours
                            if (diff > (17*3600)) {
                                Toast.makeText(mapsActivity, "Time is too far. Please choose an earlier date.", Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                return;
                            }

                            if (diff > (-5*60) && diff < (10 * 60)) {
                                details.now = true;
                            }

                            requestDriver(mapsActivity);
                        }
                    } else {
                        Toast.makeText(mapsActivity, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TimeResponse> call, Throwable t) {
                    Toast.makeText(mapsActivity, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(mapsActivity, "Error setting date. Try again", Toast.LENGTH_SHORT).show();
        }


    }

    private void requestDriver(final MapsActivity mapsActivity) {
        final PrefManager prefManager = new PrefManager(mapsActivity);
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        Call<DriverResponse> call = service.getDriver("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP) ,
                details.pickup.toString(),
                details.dest.toString(),
                (details.now)?"now":String.valueOf(details.time.getTime().getTime()),
                details.femaleOnly,
                details.notes,
                details.price,
                details.requestID,
                details.pickupText,
                details.destText
        );
        Log.d(TAG, "requestDriver: Request" + call.request().toString());
        call.enqueue(new Callback<DriverResponse>() {
            @Override
            public void onResponse(Call<DriverResponse> call, Response<DriverResponse> response) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response.raw());
                Log.d(TAG, "onResponse: " + response.toString());
                if (response.isSuccessful()){
                    Toast.makeText(mapsActivity, "Successful. Status: "+response.body().getStatus(), Toast.LENGTH_SHORT).show();
                    // There are 4 situations here:
                    // Status 0: When request is pending. request_id is returned.
                    // Status 1: No driver found
                    // Status 5: When this request has a non pending status. Return status in the error_msg
                    // Status 6: When a request is already accepted. Return request id in the error_msg
                    switch (response.body().getStatus()){
                        case 0:
                            Log.d(TAG, "onResponse: status 0");

                            Intent intent = new Intent(mapsActivity, RideRequestService.class);
                            if (mapsActivity.startService(intent) == null) {
                                Toast.makeText(mapsActivity, "Failed to start the service", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mapsActivity, "Service started", Toast.LENGTH_SHORT).show();
                            }
                            prefManager.setRideStatus(PrefManager.FINDING_DRIVER);
                            prefManager.setRideId(response.body().getRequestID());
                            details.requestID = response.body().getRequestID();
                            prefManager.setRideDetails(details);

                            EventBus.getDefault().post(new RideStarted());
                            mapsActivity.setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, mapsActivity.getString(R.string.finding_a_driver));

                            break;
                        case 3:
                            mapsActivity.toast.setText("Sorry, all drivers are busy. Try again later.");
                            mapsActivity.toast.show();
                            break;
                        case 5: // When this request has "completed" or "canceled" status.Return status in the error_msg
                            EventBus.getDefault().post(new RequestCanceled());
                            prefManager.setRideStatus(PrefManager.NO_RIDE);
                            break;
                        case 6: // When a request is already accepted.Return request id in the error_msg
                            prefManager.setRideId(response.body().getErrorMessage());
                            Log.d(TAG, "onResponse: id: " + response.body().getErrorMessage());
                            EventBus.getDefault().post(new DriverAccepted(new Driver(
                                    "unknown",
                                    "unknown",
                                    "unknown",
                                    "unknown",
                                    "unknown"
                            )));
                            break;
                        case 1:
                            EventBus.getDefault().post(new LogoutRequest());
                            break;

                    }

                } else {
                    Toast.makeText(mapsActivity, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverResponse> call, Throwable t) {
                Toast.makeText(mapsActivity, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
        });
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void cancelRequest(final MapsActivity mapsActivity) {

        final PrefManager prefManager = new PrefManager(mapsActivity);
        Log.d(TAG, "cancelRequest: id: " + prefManager.getRideId());
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        Call<SimpleResponse> call = service.cancelRequest("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                prefManager.getRideId());
        Log.d(TAG, "cancelRequest: "+call.request().toString());
        progressDialog   = new ProgressDialog(mapsActivity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connecting");
        progressDialog.show();
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()){
                    Log.i(TAG, "onResponse: Request has been canceled");
                    EventBus.getDefault().post(new RequestCanceled());
                }else {
                    Toast.makeText(mapsActivity, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.toString());
                Toast.makeText(mapsActivity, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
        });
    }

    public void arrived(final MapsActivity mapsActivity) {
        final PrefManager prefManager = new PrefManager(mapsActivity);
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        Call<SimpleResponse> call = service.postArrived("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                prefManager.getRideId());
        progressDialog   = new ProgressDialog(mapsActivity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connecting");
        progressDialog.show();
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()){
                    Log.i(TAG, "onResponse: Passenger has arrived");
                    EventBus.getDefault().post(new RequestCanceled());
                    Toast.makeText(mapsActivity, "Thank you for booking with us.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mapsActivity, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Toast.makeText(mapsActivity, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();

            }
        });

    }

    public class RideDetails{
        public RideLocation pickup;
        public RideLocation dest;
        public Calendar time;
        public Boolean now;
        public Boolean femaleOnly;
        public String notes;
        public String price;
        public String requestID;
        public String pickupText;
        public String destText;

        public RideDetails() {
        }
        public boolean isSet(){
            if (pickup == null) Log.d(TAG, "isSet: Pickup is null");
            if (dest == null) Log.d(TAG, "isSet: dest is null");
            if (time == null && !now) Log.d(TAG, "isSet: date is null");
            if (femaleOnly == null) Log.d(TAG, "isSet: femaleOnly is null");
            if (notes == null) Log.d(TAG, "isSet: notes is null");
            if (price == null) Log.d(TAG, "isSet: price is null");
            if (requestID == null) Log.d(TAG, "isSet: requestID is null");
            if (pickupText == null) Log.d(TAG, "isSet: pickupText is null");
            if (destText == null) Log.d(TAG, "isSet: destText is null");

            return (pickup != null &&
                    dest != null &&
                    (time != null || now )&&
                    femaleOnly != null &&
                    notes != null &&
                    price != null &&
                    requestID != null &&
                    pickupText != null &&
                    destText != null);
        }

        public void reset() {
            pickup = null;
            dest = null;
            femaleOnly = null;
            price = null;
            notes = "";
            time = null;
            requestID="-1";
            now = true;
            pickupText = null;
            destText = null;
        }
    }
}
