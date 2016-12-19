package com.example.islam.concepts;

import android.util.Log;

import com.example.islam.POJO.PriceResponse;
import com.example.islam.events.PriceUpdated;
import com.example.islam.ubclone.RestService;
import com.example.islam.ubclone.RestServiceConstants;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by islam on 11/30/16.
 */
public class PriceSettings {
    private static final String TAG = "PriceSettings";
    private RestService service;
    private PriceResponse priceConstants;
    private Boolean updatedFromServer;

    public PriceSettings(){
        updatedFromServer = false;
        //Creating Rest Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);
    }

    public Boolean isUpdatedFromServer(){
        return  updatedFromServer;
    }

    public void reset(){
        updatedFromServer = false;
    }

    public void setUpdatedFromServer(){
        updatedFromServer = true;
    }

    public Double getPrice(Integer duration, Integer distance){
        if (isUpdatedFromServer()){
            Double durationInMinutes = duration / 60.0;
            Double distanceInKm = distance / 1000.0;
            Double ridePrice = (durationInMinutes * priceConstants.getPerMinute()) + (distanceInKm * priceConstants.getPerKm());
            return (ridePrice > priceConstants.getMinimumPrice())? ridePrice:priceConstants.getMinimumPrice();
        } else {
            Log.e(TAG, "calculatePrice: called and price is outdated.");
            return null;
        }
    }

    public void updateFromServer(final Boolean emit, final String time){
        reset();
        Call<PriceResponse> call = service.getPrice(time);
        call.enqueue(new Callback<PriceResponse>() {
            @Override
            public void onResponse(Call<PriceResponse> call, Response<PriceResponse> response) {
                Log.i(TAG, "onResponse: response: " + response.raw());
                if (response.isSuccessful()){
                    priceConstants = response.body();
                    setUpdatedFromServer();
                    if (emit) EventBus.getDefault().post(new PriceUpdated(0,0));
                } else {
                    Log.w(TAG, "onResponse: failed to get price");
                }
            }

            @Override
            public void onFailure(Call<PriceResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: response: " + t.toString());
                updateFromServer(emit, time);

            }
        });

    }

    public void updateFromServer() {
            updateFromServer(false, "now");

    }
}
