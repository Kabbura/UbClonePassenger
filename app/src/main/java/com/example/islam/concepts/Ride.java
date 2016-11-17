package com.example.islam.concepts;

import android.util.Log;

import com.example.islam.POJO.DriversResponse;
import com.example.islam.ubclone.MapsActivity;
import com.example.islam.ubclone.RestService;
import com.example.islam.ubclone.RestServiceConstants;
import com.google.android.gms.maps.model.LatLng;

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

    public void getDrivers(final MapsActivity mapsActivity, LatLng latLng) {
        //Creating Rest Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestService service = retrofit.create(RestService.class);
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
//                Log.d(TAG, "onResponse: Retrofit response failed: "+ call.request().toString());
//                call.clone().enqueue(this);

            }
        });

    }

    public void requestDriver(){

    }
}
