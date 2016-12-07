package com.example.islam.ubclone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.islam.POJO.Driver;
import com.example.islam.POJO.DriverResponse;
import com.example.islam.concepts.Ride;
import com.example.islam.events.DriverAccepted;
import com.example.islam.events.DriverCanceled;
import com.example.islam.events.DriverLocation;
import com.example.islam.events.DriverRejected;
import com.example.islam.events.DriverUpdatedStatus;
import com.example.islam.events.LogoutRequest;
import com.example.islam.events.RequestCanceled;
import com.example.islam.events.RequestCanceledFromService;
import com.example.islam.events.RideStarted;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RideRequestService extends Service {
    private PrefManager prefManager;
    private final static String TAG = "RideRequestService";
    private Handler handler;
    int callCounter = 0;
    int validCode = 0;
    String requestID;

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
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        prefManager = new PrefManager(this);
        return START_STICKY;
    }

    @Subscribe
    protected void onRideStarted(RideStarted rideStarted) {
        if (!prefManager.isLoggedIn() || prefManager.getRideStatus().equals(PrefManager.NO_RIDE)){
            Log.i(TAG, "onHandleIntent: No ride is ongoing");
            return;
        }

        // Adding notification
        // Tapping the notification will open the specified Activity.
        Intent activityIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // This always shows up in the notifications area when this Service is running.
        // TODO: String localization
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getText(R.string.app_name));
//        builder.setContentText("content text");

        Notification not = builder.build();
        startForeground(1, not);


        requestID = prefManager.getRideId();
        Log.d(TAG, "onHandleIntent: Got request_id: "+ requestID);
        Ride ride = new Ride();
        ride.details = prefManager.getRideDetails();
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

        if (ride.details.pickup == null) Log.d(TAG, "isSet: Pickup is null");
        if (ride.details.dest == null) Log.d(TAG, "isSet: dest is null");
        if (ride.details.time == null) Log.d(TAG, "isSet: date is null");
        if (ride.details.femaleOnly == null) Log.d(TAG, "isSet: femaleOnly is null");
        if (ride.details.notes == null) Log.d(TAG, "isSet: notes is null");
        if (ride.details.price == null) Log.d(TAG, "isSet: price is null");
        if (ride.details.requestID == null) Log.d(TAG, "isSet: requestID is null");
        if (ride.details.pickupText == null) Log.d(TAG, "isSet: pickupText is null");
        if (ride.details.destText == null) Log.d(TAG, "isSet: destText is null");


        Call<DriverResponse> call = service.getDriver("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                ride.details.pickup.toString(),
                ride.details.dest.toString(),
                (ride.details.now)?"now":String.valueOf(ride.details.time.getTime().getTime()),
                ride.details.femaleOnly,
                ride.details.notes,
                ride.details.price,
                ride.details.requestID,
                ride.details.pickupText,
                ride.details.destText);



        handler = new Handler();
        Log.d(TAG, "run: Calling handler ");
        callable(call, validCode);
    }
    public void callable(final Call<DriverResponse> call, final int mValidCode){
//        Log.d(TAG, "callable: call: "+call.request().toString());
        // Check if user logged in: and if request hasn't been updated:
        if (!prefManager.isLoggedIn()){
            Log.i(TAG, "callable: user is not logged in");
            return;
        }
        if (mValidCode != validCode) {
            // Special case when the handler callback is removed but the request was ongoing. In its
            // response it will call this function with an outdated valid code
            Log.d(TAG, "callable: valid code: "+mValidCode+" ");
            return;
        }

        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "run: I am called for "+ callCounter++ +" service: "+call.request().toString());
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
                                   callable(call.clone(), mValidCode);
                                   break;
                               case 3:
                                   Toast.makeText(RideRequestService.this, "Sorry, all drivers are busy. Try again later.", Toast.LENGTH_LONG).show();
                                   Log.i(TAG, "onResponse: status 1. No drivers available.");
                                   EventBus.getDefault().post(new RequestCanceled());
                                   return;
                               case 5: // When this request has "completed" or "canceled" status.Return status in the error_msg
                                   EventBus.getDefault().post(new RequestCanceled());
                                   prefManager.setRideStatus(PrefManager.NO_RIDE);
                                   return;
                               case 6: // When a request is already accepted.Return request id in the error_msg
                                   prefManager.setRideId(response.body().getErrorMessage());
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
                           Toast.makeText(RideRequestService.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                       }

                   }

                   @Override
                   public void onFailure(Call<DriverResponse> call, Throwable t) {
                       Log.i(TAG, "onFailure: Failed to connect. Trying again in 30 seconds");
                       callable(call.clone(), validCode);
                   }
               });
            }
        }, 30000);
    }


    @Subscribe
    public void onDriverReject(DriverRejected driverRejected){
        Log.d(TAG, "restartCallable: Restarting");
        try {
            handler.removeCallbacksAndMessages(null);
        }catch (NullPointerException e){
            Log.i(TAG, "onRequestCanceled: handler messages removed");
        }
        validCode++;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        String email, password;
        if (!prefManager.isLoggedIn() || !prefManager.getRideStatus().equals(PrefManager.FINDING_DRIVER) ){
            return;
        }
        email = prefManager.getUser().getEmail();
        password = prefManager.getUser().getPassword();
        RestService service = retrofit.create(RestService.class);
        Ride ride = new Ride();
        ride.details = prefManager.getRideDetails();
        Call<DriverResponse> call = service.getDriver("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                ride.details.pickup.toString(),
                ride.details.dest.toString(),
                (ride.details.now)?"now":String.valueOf(ride.details.time.getTime().getTime()),
                ride.details.femaleOnly,
                ride.details.notes,
                ride.details.price,
                ride.details.requestID,
                ride.details.pickupText,
                ride.details.destText);

        callable(call, validCode);
    }

    @Subscribe
    public void onDriverAccepted(DriverAccepted driverAccepted){
        Log.d(TAG, "onDriverAccepted: A driver has accepted");
        validCode++;
        prefManager.setRideStatus(PrefManager.DRIVER_ACCEPTED);
        prefManager.setRideDriver(driverAccepted.getDriver());
        try {
            handler.removeCallbacksAndMessages(null);
        }catch (NullPointerException e){
            Log.i(TAG, "onRequestCanceled: handler messages removed");
        }
    }

    @Subscribe
    public void onDriverUpdatedStatus(DriverUpdatedStatus driverUpdatedStatus){
        switch (driverUpdatedStatus.getMessage()){
            case RestServiceConstants.ON_THE_WAY:
                prefManager.setRideStatus(PrefManager.ON_THE_WAY);
                break;

            case RestServiceConstants.ARRIVED_PICKUP:
                prefManager.setRideStatus(PrefManager.ARRIVED_PICKUP);
                break;

            case RestServiceConstants.PASSENGER_ONBOARD:
                prefManager.setRideStatus(PrefManager.PASSENGER_ONBOARD);
                break;

            case RestServiceConstants.ARRIVED_DEST:
                prefManager.setRideStatus(PrefManager.ARRIVED_DEST);
                break;

            case RestServiceConstants.COMPLETED:
                prefManager.setRideStatus(PrefManager.COMPLETED);
                break;

        }

    }

    @Subscribe
    public void onRequestCanceled(RequestCanceled requestCanceled){
        Log.d(TAG, "onRequestCanceled: called");
        //PrefManager
        prefManager.setRideDriver(new Driver(getString(R.string.dash),"",getString(R.string.dash),getString(R.string.dash),""));
        prefManager.setRideStatus(PrefManager.NO_RIDE);
        prefManager.setRideId("");
        validCode++;

        stopForeground(true);
        try {
            handler.removeCallbacksAndMessages(null);
        }catch (NullPointerException e){
            Log.i(TAG, "onRequestCanceled: handler messages removed");
        }
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onDriverCanceled(DriverCanceled driverCanceled){
        onRequestCanceled(new RequestCanceled());
    }

    @Subscribe
    public void onLogoutRequest(LogoutRequest logoutRequest){
        prefManager.setIsLoggedIn(false);
        onRequestCanceled(new RequestCanceled());
        stopSelf();
    }

}
