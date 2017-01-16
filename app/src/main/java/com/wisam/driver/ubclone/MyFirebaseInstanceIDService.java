package com.wisam.driver.ubclone;

import android.util.Base64;
import android.util.Log;

import com.wisam.driver.POJO.SimpleResponse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private PrefManager prefManager;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        prefManager = new PrefManager(this);
        if (prefManager.isLoggedIn()){

            RestServiceConstants constants = new RestServiceConstants();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(constants.getBaseUrl(MyFirebaseInstanceIDService.this))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            prefManager.setRegistrationToken(refreshedToken);

            RestService service = retrofit.create(RestService.class);
            Call<SimpleResponse> call = service.updateToken("Basic "+ Base64.encodeToString((prefManager.getUser().getEmail() + ":" + prefManager.getUser().getPassword()).getBytes(),Base64.NO_WRAP)
            , refreshedToken);
            call.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if(!response.isSuccessful()){
                        prefManager.setIsLoggedIn(false);
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                        prefManager.setIsLoggedIn(false);
                }
            });
        }
    }
}