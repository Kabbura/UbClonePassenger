package com.wisam.driver.ubclone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.wisam.driver.POJO.Driver;
import com.wisam.driver.POJO.DriverResponse;
import com.wisam.driver.concepts.PendingRequest;
import com.wisam.driver.concepts.Ride;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.DriverCanceled;
import com.wisam.driver.events.DriverCanceledUI;
import com.wisam.driver.events.DriverLocation;
import com.wisam.driver.events.DriverRejected;
import com.wisam.driver.events.DriverUpdatedStatus;
import com.wisam.driver.events.LogoutRequest;
import com.wisam.driver.events.RequestFinished;
import com.wisam.driver.events.RequestFinishedUI;
import com.wisam.driver.events.RideStarted;
import com.wisam.driver.exceptions.InvalidRideDetailsException;
import com.wisam.driver.exceptions.RideNotFoundException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRequestService extends Service {
    private PrefManager prefManager;
    private final static String TAG = "RideRequestService";
    private Handler handler;
    int callCounter = 0;
    int validCode = 0;
    private Ride pendingRide;
    private ArrayList<PendingRequest> pendingRequestList;

    public RideRequestService() {
        //super("RideRequestService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        prefManager = new PrefManager(this);
        pendingRide = new Ride(this);
        pendingRequestList = new ArrayList<PendingRequest>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Subscribe
    public void onRideStarted(RideStarted rideStarted) {
        if (!prefManager.isLoggedIn()) return;

        pendingRide.details = rideStarted.getDetails();
        showNotification();
        startPendingRequest(pendingRide.details);
    }

    private void startPendingRequest(Ride.RideDetails details) {
        PendingRequest pendingRequest = new PendingRequest(this, details);
        try {
            pendingRequest.start();
            pendingRequestList.add(pendingRequest);
        } catch (InvalidRideDetailsException e) {
            Log.e(TAG, "startPendingRequest: Failed to start pending request. " + e.getMessage());
        }

    }

    public void showNotification(){
        Intent activityIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getText(R.string.app_name));
        builder.setContentText(getString(R.string.ongoing_ride));
        Notification not = builder.build();
        startForeground(1, not);
    }


    private void recursiveServerCall(final Call<DriverResponse> call, final int mValidCode, final String rideRequest){
        call.enqueue(new Callback<DriverResponse>() {
            @Override
            public void onResponse(Call<DriverResponse> call, Response<DriverResponse> response) {
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
                            Log.i(TAG, "onResponse: status 0. Trying again in 30 seconds");
//                            callable(call.clone(), validCode);
                            Log.d(TAG, "onDriverReject: Restarted successfully");
                            break;
                        case 3:
//                            Toast.makeText(RideRequestService.this, "Sorry, all drivers are busy. Try again later.", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "onResponse: status 1. No drivers available.");
//                            EventBus.getDefault().post(new RequestFinished(pendingRide.details.requestID));
                            break;
                        case 5: // When this request has "completed" or "canceled" status.Return status in the error_msg
                            EventBus.getDefault().post(new RequestFinished(pendingRide.details.requestID));
                            prefManager.clearCurrentRide();
                            break;
                        case 6: // When a request is already accepted.Return request id in the error_msg
                            pendingRide.details.requestID = response.body().getErrorMessage();
                            prefManager.setCurrentRide(pendingRide.details);
//                                   prefManager.setRideId(response.body().getErrorMessage());

                            if (mValidCode != validCode) {
                                // Special case when the handler callback is removed but the request was ongoing. In its
                                // response it will call this function with an outdated valid code
                                Log.d(TAG, "callable: valid code: "+mValidCode+" ");
                                return;
                            }
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
                    Toast.makeText(RideRequestService.this, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverResponse> call, Throwable t) {
                Toast.makeText(RideRequestService.this, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure: Failed to connect. Trying again in 30 seconds");
                onDriverReject(new DriverRejected(rideRequest));
            }
        });
    }

    @Subscribe
    public void onDriverReject(final DriverRejected driverRejected){

        try {
            PendingRequest pendingRequest = getPendingRequest(driverRejected.getRequestID());
            pendingRequest.restart();
        } catch (RideNotFoundException e) {
            Log.w(TAG, "onDriverReject: Request with ID " + driverRejected.getRequestID() + " not found. Aborting.");
        } catch (InvalidRideDetailsException e) {
            Log.e(TAG, "onDriverReject: Invalid ride details. " + e.getMessage());
        }
    }

    private PendingRequest getPendingRequest(String requestId) throws RideNotFoundException {
        try {
            return getRequestFromPendingRequestList(requestId);
        } catch (RideNotFoundException e) {
            Log.w(TAG, "getPendingRequest: " + e.getMessage());
            return getPendingRequestFromSharedPreferences(requestId);
        }
    }

    private PendingRequest getPendingRequestFromSharedPreferences (String requestId) throws RideNotFoundException  {
        Ride.RideDetails rideDetails;
        rideDetails = prefManager.getRide(requestId);
        if (rideDetails != null) {
            rideDetails.setStatus(PrefManager.FINDING_DRIVER);
            prefManager.updateOngoingRide(rideDetails);
            return new PendingRequest(this, rideDetails);
        }
        throw new RideNotFoundException("Request with ID " + requestId + " is not in the SharedPreferences.");
    }

    private PendingRequest getRequestFromPendingRequestList(String requestId) throws RideNotFoundException {
        for (PendingRequest pendingRequest : pendingRequestList) {
            if (pendingRequest.hasId(requestId)) {
                return pendingRequest;
            }
        }
        throw new RideNotFoundException("Request with ID " + requestId + " is not in the pendingRequestList.");
    }

    @Subscribe
    public void onDriverAccepted(DriverAccepted driverAccepted){
        Log.d(TAG, "onDriverAccepted: service id: " + prefManager.getCurrentRide().requestID + " driverAccepted request: " + driverAccepted.getRequestID());
        if (!prefManager.getCurrentRide().requestID.equals(driverAccepted.getRequestID())) {
            return;
        }
        validCode++;
        Log.d(TAG, "onDriverAccepted: A driver has accepted");
        Ride ride = new Ride(this);
        ride.details = prefManager.getRide(driverAccepted.getRequestID());
        ride.details.setStatus(PrefManager.DRIVER_ACCEPTED);
        ride.details.setDriver(driverAccepted.getDriver());

        prefManager.setCurrentRide(ride.details);
//        prefManager.setRideStatus(PrefManager.DRIVER_ACCEPTED);
//        prefManager.setRideDriver(driverAccepted.getDriver());
        try {
            handler.removeCallbacksAndMessages(null);
        }catch (NullPointerException e){
            Log.i(TAG, "onRequestCanceled: handler messages removed");
        }
    }

    @Subscribe
    public void onDriverUpdatedStatus(DriverUpdatedStatus driverUpdatedStatus){

        Ride ride = new Ride(this);
        ride.details = prefManager.getRide(driverUpdatedStatus.getRequestID());
        if (ride.details == null){
            Log.w(TAG, "onDriverUpdatedStatus: No ride found matching the request id: " + driverUpdatedStatus.getRequestID());
            return;
        }

        switch (driverUpdatedStatus.getMessage()){
            case RestServiceConstants.ON_THE_WAY:
                ride.details.setStatus(PrefManager.ON_THE_WAY);
                break;

            case RestServiceConstants.ARRIVED_PICKUP:
                ride.details.setStatus(PrefManager.ARRIVED_PICKUP);
                break;

            case RestServiceConstants.PASSENGER_ONBOARD:
                ride.details.setStatus(PrefManager.PASSENGER_ONBOARD);
                break;

            case RestServiceConstants.ARRIVED_DEST:
                ride.details.setStatus(PrefManager.ARRIVED_DEST);
                break;

            case RestServiceConstants.COMPLETED:
                ride.details.setStatus(PrefManager.COMPLETED);
                break;

        }
        if (prefManager.getCurrentRide().requestID.equals(driverUpdatedStatus.getRequestID())) {
            prefManager.setCurrentRide(ride.details);
        } else {
            prefManager.updateOngoingRide(ride.details);
        }

    }

    @Subscribe
    public void onRequestCanceled(RequestFinished requestCanceled){
        if (prefManager.getCurrentRide().requestID.equals(requestCanceled.getRequestID()))
            EventBus.getDefault().post(new RequestFinishedUI(requestCanceled.getRequestID()));

        for (PendingRequest request : pendingRequestList) {
            Log.i(TAG, "onRequestCanceled: pending request id: " + request.getId());
        }
        Log.i(TAG, "onRequestCanceled: list size: " + pendingRequestList.size());
        cancelPendingRequest(requestCanceled.getRequestID());
        removeRequestFromPendingRequestsList(requestCanceled.getRequestID());
        removeRideFromOngoingRidesList(requestCanceled.getRequestID());

    }

    private void removeRequestFromPendingRequestsList(String requestId) {
        try {
            int index = getPendingRequestIndex(requestId);
            pendingRequestList.remove(index);
        } catch (RideNotFoundException e) {
            Log.w(TAG, "removeRequestFromPendingRequestsList: "+ e.getMessage() );
        }
    }

    private int getPendingRequestIndex(String requestId) throws RideNotFoundException {
        for (int i = 0; i < pendingRequestList.size(); i++) {
            if (pendingRequestList.get(i).hasId(requestId))
                return i;
        }
        throw new RideNotFoundException("Request with ID " + requestId + " is not in the pendingRequestList.");
    }

    private void cancelPendingRequest(String requestId) {
        try {
            PendingRequest pendingRequest = getRequestFromPendingRequestList(requestId);
            pendingRequest.cancel();
        } catch (RideNotFoundException e) {
            Log.i(TAG, "onRequestCanceled: Request is not in the pending request list.");
        }
    }

    private void removeRideFromOngoingRidesList(String requestId) {
        prefManager.removeOngoingRide(requestId);

        if (prefManager.getOngoingRides().size() == 0) stopForeground(true);
    }

    @Subscribe
    public void onDriverCanceled(DriverCanceled driverCanceled){
        if (prefManager.getCurrentRide().requestID.equals(driverCanceled.getRequestID()))
            EventBus.getDefault().post(new DriverCanceledUI(driverCanceled.getRequestID()));

        onRequestCanceled(new RequestFinished(driverCanceled.getRequestID()));
    }

    @Subscribe
    public void onLogoutRequest(LogoutRequest logoutRequest){
        prefManager.setIsLoggedIn(false);
        onRequestCanceled(new RequestFinished(pendingRide.details.requestID));
        stopSelf();
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
