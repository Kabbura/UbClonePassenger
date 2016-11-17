package com.example.islam.concepts;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.islam.POJO.DriverResponse;
import com.example.islam.POJO.DriversResponse;
import com.example.islam.ubclone.MapsActivity;
import com.example.islam.ubclone.R;
import com.example.islam.ubclone.RestService;
import com.example.islam.ubclone.RestServiceConstants;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.Date;

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


    public Ride() {
        //Creating Rest Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(RestService.class);


        // Initialize details
        details = new RideDetails();
        details.notes = "";
        details.time = "Now";
        details.requestID="-1";

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

    public void requestDriver(final MapsActivity mapsActivity){

        //TODO: remove initializing price
        details.price = "price";
        if(details.isSet()){

            final ProgressDialog progressDialog = new ProgressDialog(mapsActivity);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Connecting");
            progressDialog.show();
            Call<DriverResponse> call = service.getDriver(details.pickup.toString(),
                    details.dest.toString(),
                    details.time,
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

        } else {
            Toast.makeText(mapsActivity, "Error sending request", Toast.LENGTH_SHORT).show();
        }
    }

    public class RideDetails{
        public RideLocation pickup;
        public RideLocation dest;
        public String time;
        public Boolean femaleOnly;
        public String notes;
        public String price;
        public String requestID;

        public RideDetails() {
        }
        public boolean isSet(){
            if (pickup == null) Log.d(TAG, "isSet: Pickup is null");
            if (dest == null) Log.d(TAG, "isSet: dest is null");
            if (time == null) Log.d(TAG, "isSet: time is null");
            if (femaleOnly == null) Log.d(TAG, "isSet: femaleOnly is null");
            if (notes == null) Log.d(TAG, "isSet: notes is null");
            if (price == null) Log.d(TAG, "isSet: price is null");
            if (requestID == null) Log.d(TAG, "isSet: requestID is null");

            return (pickup != null &&
                    dest != null &&
                    time != null &&
                    femaleOnly != null &&
                    notes != null &&
                    price != null &&
                    requestID != null);
        }
    }
}
