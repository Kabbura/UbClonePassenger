package com.example.islam.concepts;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.POJO.TimeResponse;
import com.example.islam.ubclone.MapsActivity;
import com.example.islam.ubclone.R;
import com.example.islam.ubclone.RestService;
import com.example.islam.ubclone.RestServiceConstants;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

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

            // Validate time

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
                                Toast.makeText(mapsActivity, "Invalid time", Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                return;
                            }

                            // If request is after more than 18 hours
                            if (diff > (17*3600)) {
                                Toast.makeText(mapsActivity, "Time is too far. Please choose an earlier time.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mapsActivity, "Error setting time. Try again", Toast.LENGTH_SHORT).show();
        }


    }

    private void requestDriver(final MapsActivity mapsActivity) {
        Call<DriverResponse> call = service.getDriver(details.pickup.toString(),
                details.dest.toString(),
                (details.now)?"now":String.valueOf(details.time.getTime().getTime()),
                details.femaleOnly,
                details.notes,
                details.price,
                details.requestID
        );
        Log.d(TAG, "requestDriver: Request" + call.request().toString());
        call.enqueue(new Callback<DriverResponse>() {
            @Override
            public void onResponse(Call<DriverResponse> call, Response<DriverResponse> response) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                if (response.isSuccessful()){
                    mapsActivity.setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, mapsActivity.getString(R.string.finding_a_driver));
                    //TODO: try in 30 seconds

                } else {
                    Toast.makeText(mapsActivity, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverResponse> call, Throwable t) {
                Toast.makeText(mapsActivity, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
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

        public RideDetails() {
        }
        public boolean isSet(){
            if (pickup == null) Log.d(TAG, "isSet: Pickup is null");
            if (dest == null) Log.d(TAG, "isSet: dest is null");
            if (time == null && !now) Log.d(TAG, "isSet: time is null");
            if (femaleOnly == null) Log.d(TAG, "isSet: femaleOnly is null");
            if (notes == null) Log.d(TAG, "isSet: notes is null");
            if (price == null) Log.d(TAG, "isSet: price is null");
            if (requestID == null) Log.d(TAG, "isSet: requestID is null");

            return (pickup != null &&
                    dest != null &&
                    (time != null || now )&&
                    femaleOnly != null &&
                    notes != null &&
                    price != null &&
                    requestID != null);
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
        }
    }
}
