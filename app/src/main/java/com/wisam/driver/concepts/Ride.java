package com.wisam.driver.concepts;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.wisam.driver.POJO.Driver;
import com.wisam.driver.POJO.DriverResponse;
import com.wisam.driver.POJO.DriversResponse;
import com.wisam.driver.POJO.SimpleResponse;
import com.wisam.driver.POJO.TimeResponse;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.DriverLocation;
import com.wisam.driver.events.LogoutRequest;
import com.wisam.driver.events.RequestFinished;
import com.wisam.driver.events.RideStarted;
import com.wisam.driver.ubclone.MapsActivity;
import com.wisam.driver.ubclone.PrefManager;
import com.wisam.driver.ubclone.R;
import com.wisam.driver.ubclone.RestService;
import com.wisam.driver.ubclone.RestServiceConstants;
import com.wisam.driver.ubclone.RideRequestService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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

    public RestService getRestService() {
        return service;
    }

    public Ride(Context context) {
        //Creating Rest Services

        RestServiceConstants constants = new RestServiceConstants();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(constants.getBaseUrl(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(RestService.class);

        // Initialize details
        details = new RideDetails();
        details.reset();

    }

    public void getDrivers(final MapsActivity mapsActivity, LatLng latLng) {
        String location = latLng.latitude +","+latLng.longitude;
        // Count is set to 50
        Call<DriversResponse> call = service.getDrivers(location, 50);
        Log.d(TAG, "getDriver: raw: " + call.request().toString());
        call.enqueue(new Callback<DriversResponse>() {
            @Override
            public void onResponse(Call<DriversResponse> call, Response<DriversResponse> response) {
                Log.d(TAG, "getDriver: onResponse: raw: " + response.raw());
                if (response.isSuccessful()){
                    //TODO: Check if drivers is null
                    Log.d(TAG, "getDriver: onResponse: raw: " + response.raw());
                    Log.d(TAG, "getDriver: onResponse: Retrofit response success: Got "+ response.body().drivers.size());
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
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mapsActivity.getString(R.string.connecting));
            progressDialog.show();

            // Validate date
            validateTime(mapsActivity);


        } else {
            Toast.makeText(mapsActivity, R.string.error_sending_request, Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG, "onResponse: " + response.raw());
                    if (response.isSuccessful() && response.body() != null){
                        if (response.body().getTime() != null){
                            Log.d(TAG, "onResponse: server time: "+ response.body().getTime());
                            Log.d(TAG, "onResponse: c-norm time: "+ (details.time.getTime().getTime()/1000));
                            Long currentTime = response.body().getTime();
                            Long requestTime = (details.time.getTime().getTime()/1000);
                            Long diff = requestTime - currentTime;
                            Log.d(TAG, "onResponse: time difference: "+ diff);
                            if (diff < -300) {
                                Toast.makeText(mapsActivity, R.string.invalid_date, Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                return;
                            }

                            // If request is after more than 72 hours
                            if (diff > (72*3600)) {
                                Toast.makeText(mapsActivity, R.string.time_too_far, Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                return;
                            }

                            if (diff > (-5*60) && diff < (10 * 60)) {
                                details.now = true;
                            }

                            requestDriver(mapsActivity);
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                        }
                    } else {
                        Toast.makeText(mapsActivity, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TimeResponse> call, Throwable t) {
                    Toast.makeText(mapsActivity, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(mapsActivity, R.string.error_setting_date, Toast.LENGTH_SHORT).show();
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
                    // Toast.makeText(mapsActivity, "Successful. Status: "+response.body().getStatus(), Toast.LENGTH_SHORT).show();
                    // There are 4 situations here:
                    // Status 0: When request is pending. request_id is returned.
                    // Status 1: No driver found
                    // Status 5: When this request has a non pending status. Return status in the error_msg
                    // Status 6: When a request is already accepted. Return request id in the error_msg
                    switch (response.body().getStatus()){
                        case 0:
                            Intent intent = new Intent(mapsActivity, RideRequestService.class);
                            mapsActivity.startService(intent);
                            Log.d(TAG, "onResponse: status 0");
                            details.requestID = response.body().getRequestID();
                            details.setStatus(PrefManager.FINDING_DRIVER);
                            prefManager.setCurrentRide(details);
                            Log.d(TAG, "onResponse: Request ID: " + response.body().getRequestID());

                            EventBus.getDefault().post(new RideStarted(details, mapsActivity));
                            mapsActivity.setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, mapsActivity.getString(R.string.finding_a_driver));

                            break;
                        case 3:
                            mapsActivity.toast.setText(R.string.all_drivers_are_busy);
                            mapsActivity.toast.show();
                            break;
                        case 5: // When this request has "completed" or "canceled" status.Return status in the error_msg
                            if (prefManager.getCurrentRide().requestID.equals(details.requestID))
//                                EventBus.getDefault().post(new RequestFinishedUI(details.requestID));
                            EventBus.getDefault().post(new RequestFinished(details.requestID));
                            details.setStatus(PrefManager.NO_RIDE);
                            prefManager.setCurrentRide(details);
//                            prefManager.setRideStatus(PrefManager.NO_RIDE);
                            break;
                        case 6: // When a request is already accepted.Return request id in the error_msg
                            details.requestID = response.body().getErrorMessage();
                            prefManager.setCurrentRide(details);
//                            prefManager.setRideId(response.body().getErrorMessage());
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
                    Toast.makeText(mapsActivity, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverResponse> call, Throwable t) {
                Toast.makeText(mapsActivity, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
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
        if (prefManager.getCurrentRide().requestID.equals("-1")) {
            Log.w(TAG, "cancelRequest: -1 id");
            return;
        }
        Log.d(TAG, "cancelRequest: id: " + prefManager.getCurrentRide().requestID);
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        Call<SimpleResponse> call = service.cancelRequest("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                prefManager.getCurrentRide().requestID);
        Log.d(TAG, "cancelRequest: "+call.request().toString());
        progressDialog   = new ProgressDialog(mapsActivity);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mapsActivity.getString(R.string.connecting));
        progressDialog.show();
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()){
                    Log.i(TAG, "onResponse: Request has been canceled");

//                    EventBus.getDefault().post(new RequestFinishedUI(prefManager.getCurrentRide().requestID));
                    EventBus.getDefault().post(new RequestFinished(prefManager.getCurrentRide().requestID));
//                    prefManager.clearCurrentRide();
                }else {
                    Toast.makeText(mapsActivity, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.toString());
                Toast.makeText(mapsActivity, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
        });
    }

    public void arrived(final MapsActivity mapsActivity) {
        final PrefManager prefManager = new PrefManager(mapsActivity);
        if (prefManager.getCurrentRide().requestID.equals("-1")) {
            Log.w(TAG, "cancelRequest: -1 id");
            return;
        }
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        Call<SimpleResponse> call = service.postArrived("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                prefManager.getCurrentRide().requestID);
        progressDialog   = new ProgressDialog(mapsActivity);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connecting");
        progressDialog.show();
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()){
                    Log.i(TAG, "onResponse: Passenger has arrived");

//                    EventBus.getDefault().post(new RequestFinishedUI(prefManager.getCurrentRide().requestID));
                    EventBus.getDefault().post(new RequestFinished(prefManager.getCurrentRide().requestID));
                    Toast.makeText(mapsActivity, R.string.thank_you_for_booking, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mapsActivity, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Toast.makeText(mapsActivity, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
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
        public Integer distance;
        public Integer duration;
        private Integer status;
        private Driver driver;
        private PolylineOptions routePolylineOptions;

        public String getTime(){
//            return(time == null)? "now" : time.getTime().getTime();
            return (time == null)?"now":String.valueOf(time.getTime().getTime());
        }

        public RideDetails() {
            status = PrefManager.NO_RIDE;
            requestID = "-1";
            now = true;
            driver = new Driver("-","","-","-","");
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
            distance = null;
            duration = null;
            status = PrefManager.NO_RIDE;
            driver = new Driver("-","","-","-","");
        }

        public boolean isVoid(){
            return status.equals(PrefManager.NO_RIDE);
        }


        public void setStatus(Integer status){
            if (status.equals(PrefManager.COMPLETED) ||
                    status.equals(PrefManager.NO_RIDE) ||
                    status.equals(PrefManager.PASSENGER_ONBOARD) ||
                    status.equals(PrefManager.ARRIVED_PICKUP) ||
                    status.equals(PrefManager.ARRIVED_DEST) ||
                    status.equals(PrefManager.DRIVER_ACCEPTED) ||
                    status.equals(PrefManager.FINDING_DRIVER) ||
                    status.equals(PrefManager.ON_GOING_RIDE) ||
                    status.equals(PrefManager.ON_THE_WAY)){
                this.status = status;
            }
            else {
                this.status = PrefManager.ON_GOING_RIDE;
            }
        }

        public void setStatus(Integer status, Driver driver){
            this.driver = driver;
            setStatus(status);
        }

        public Integer getStatus() {
            return (status == null)? PrefManager.NO_RIDE: status;
        }

        public Driver getDriver() {
            return (driver == null)?new Driver("-","","-","-",""): driver ;
        }

        public void setDriver(Driver driver){
            this.driver = (driver == null)? new Driver("-","","-","-",""): driver ;
        }

        public PolylineOptions getRoutePolylineOptions() {
            return routePolylineOptions;
        }

        public void setRoutePolylineOptions(PolylineOptions routePolyline) {
            this.routePolylineOptions = routePolyline;
        }
    }
}
