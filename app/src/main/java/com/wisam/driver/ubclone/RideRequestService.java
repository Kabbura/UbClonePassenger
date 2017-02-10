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
import com.wisam.driver.events.ControlCanceledRequest;
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
import org.greenrobot.eventbus.ThreadMode;

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
            PendingRequest pendingRequest = new PendingRequest(this, rideDetails);
            pendingRequestList.add(pendingRequest);
            return pendingRequest;
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
        cancelPendingRequest(driverAccepted.getRequestID());
        removeRequestFromPendingRequestsList(driverAccepted.getRequestID());
        updateAcceptedRideInOngoingRidesList(driverAccepted);
    }

    private void updateAcceptedRideInOngoingRidesList(DriverAccepted driverAccepted) {
        Ride ride = new Ride(this);
        ride.details = prefManager.getRide(driverAccepted.getRequestID());
        ride.details.setStatus(PrefManager.DRIVER_ACCEPTED);
        ride.details.setDriver(driverAccepted.getDriver());
        prefManager.setCurrentRide(ride.details);
    }

    private void updateCanceledRideInOngoingRidesList(DriverCanceled driverCanceled) {
        Ride ride = new Ride(this);
        ride.details = prefManager.getRide(driverCanceled.getRequestID());
        ride.details.setStatus(PrefManager.FINDING_DRIVER);
        ride.details.setDriver(new Driver("-","","-","-",""));
        prefManager.setCurrentRide(ride.details);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverCanceled(DriverCanceled driverCanceled){
        Log.d(TAG, "onDriverCanceled: Called. " + driverCanceled.getRequestID());
        onDriverReject(new DriverRejected(driverCanceled.getRequestID()));
        updateCanceledRideInOngoingRidesList(driverCanceled);
        EventBus.getDefault().post(new DriverUpdatedStatus(RestServiceConstants.FINDING_DRIVER,driverCanceled.getRequestID() ));
    }

    @Subscribe
    public void onControlCanceledRequest(ControlCanceledRequest controlCanceledRequest){
        if (prefManager.getCurrentRide().requestID.equals(controlCanceledRequest.getRequestID()))
            EventBus.getDefault().post(new DriverCanceledUI(controlCanceledRequest.getRequestID()));

        onRequestCanceled(new RequestFinished(controlCanceledRequest.getRequestID()));
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
