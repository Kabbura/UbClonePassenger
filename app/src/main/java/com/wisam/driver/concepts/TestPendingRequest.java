package com.wisam.driver.concepts;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.wisam.driver.POJO.Driver;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.DriverCanceled;
import com.wisam.driver.events.RequestFinished;
import com.wisam.driver.events.RideStarted;
import com.wisam.driver.ubclone.MapsActivity;
import com.wisam.driver.ubclone.PrefManager;
import com.wisam.driver.ubclone.R;
import com.wisam.driver.ubclone.RideRequestService;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by islam on 2/6/17.
 */
public class TestPendingRequest {


    private static final String TAG = "TestPendingRequest";
    private MapsActivity mapsActivity;
    private PrefManager prefManager;

    public TestPendingRequest(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
        prefManager = new PrefManager(mapsActivity);

        prefManager.setBaseUrl("http://192.168.43.155:8080/");
        startNewRide(1000);

        driverAcceptsRide("First driver", "9999", 10000);
        driverCancelsRequest("9999", 15000);
//        driverAcceptsRide("Second driver", "9999", 12000);
//        driverCancelsRequest("9999", 13000);

        cancelRide(25000);



//        prefManager.setBaseUrl("http://wissamapps.esy.es/public/");
    }

    private void driverCancelsRequest(final String requestId, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "driverCancelsRequest: Called");
                                    EventBus.getDefault().post(new DriverCanceled(requestId));
                                } },
                delay);

    }

    private void driverAcceptsRide(final String driverName, final String requestId, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "driverAcceptsRide: Called");
                                    EventBus.getDefault().post(new DriverAccepted(new Driver(
                                            driverName,
                                            "09090",
                                            "KH11",
                                            requestId,
                                            "Lorry"
                                    )));

                                } },
                delay);



    }

    private void cancelRide(int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "cancelRide: Called");
                                    EventBus.getDefault().post(new RequestFinished("9999"));
                                } },
                delay);

    }

    private void startNewRide(int delay) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {Ride ride = new Ride(mapsActivity);
                                    Ride.RideDetails details = ride.details;

                                    details.pickupText = "Pick up text";
                                    details.destText = "Dest text";
                                    details.pickup = new RideLocation(15.592791, 32.534134);
                                    details.dest = new RideLocation(15.593791, 32.534234);
                                    details.now = true;
                                    details.femaleOnly = false;
                                    details.price = "40";
                                    details.distance = 20;
                                    details.duration = 30;

                                    Intent intent = new Intent(mapsActivity, RideRequestService.class);
                                    mapsActivity.startService(intent);
                                    details.requestID = "9999";
                                    details.setStatus(PrefManager.FINDING_DRIVER);
                                    prefManager.setCurrentRide(details);
                                    Log.d(TAG, "startNewRide: Request ID: 9999");

                                    EventBus.getDefault().post(new RideStarted(details, mapsActivity));
                                    mapsActivity.setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, mapsActivity.getString(R.string.finding_a_driver));
                                } },
                delay);

    }


}
