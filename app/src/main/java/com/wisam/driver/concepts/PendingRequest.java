package com.wisam.driver.concepts;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.wisam.driver.POJO.Driver;
import com.wisam.driver.POJO.DriverResponse;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.LogoutRequest;
import com.wisam.driver.events.RequestFinished;
import com.wisam.driver.exceptions.InvalidRideDetailsException;
import com.wisam.driver.ubclone.PrefManager;
import com.wisam.driver.ubclone.R;
import com.wisam.driver.ubclone.RestService;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by islam on 2/5/17.
 */
public class PendingRequest extends Ride {
    private final static String TAG = "PendingRequest";
//    private final static int CALL_WAIT_TIME = 36000;
    private final static int CALL_WAIT_TIME = 3600;
    private final static int LOOKING_FOR_DRIVER = 0;
    private final static int AUTHENTICATION_FAILURE = 1;
    private final static int NO_DRIVER_FOUND = 3;
    private final static int REQUEST_COMPLETED = 5;
    private final static int REQUEST_ACCEPTED = 6;

    private Ride.RideDetails rideDetails;
    final PrefManager prefManager;
    private Handler handler;
    private Call<DriverResponse> driverRequestCall;
    Call<DriverResponse> cloneCall;
    private Context context;

    public PendingRequest(Context context, RideDetails details) {
        super(context);
        this.context = context;
        rideDetails = details;
        prefManager = new PrefManager(context);
        handler = new Handler();
    }

    public void start() throws InvalidRideDetailsException{
        RestService service = getRestService();
        driverRequestCall = createDriverRequestCall(service);
        runPostDelayed();

    }

    private Call<DriverResponse> createDriverRequestCall(RestService restService) throws InvalidRideDetailsException {
        validateRideDetails();
        String email = prefManager.getUser().getEmail();
        String password = prefManager.getUser().getPassword();
        return restService.getDriver("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP),
                rideDetails.pickup.toString(),
                rideDetails.dest.toString(),
                (rideDetails.now)?"now":String.valueOf(rideDetails.time.getTime().getTime()),
                rideDetails.femaleOnly,
                rideDetails.notes,
                rideDetails.price,
                rideDetails.requestID,
                rideDetails.pickupText,
                rideDetails.destText);
    }

    private void validateRideDetails() throws InvalidRideDetailsException {
        if (rideDetails.pickup == null) { throw new InvalidRideDetailsException("Pickup is null");}
        if (rideDetails.dest == null){ throw new InvalidRideDetailsException("dest is null");}
        if (rideDetails.time == null && !rideDetails.now){ throw new InvalidRideDetailsException("date is null");}
        if (rideDetails.femaleOnly == null){ throw new InvalidRideDetailsException("femaleOnly is null");}
        if (rideDetails.notes == null){  throw new InvalidRideDetailsException("notes is null");}
        if (rideDetails.price == null){  throw new InvalidRideDetailsException("price is null");}
        if (rideDetails.requestID.equals("-1")){ throw new InvalidRideDetailsException("requestID is -1");}
        if (rideDetails.pickupText == null){ throw new InvalidRideDetailsException("pickupText is null");}
        if (rideDetails.destText == null){  throw new InvalidRideDetailsException("destText is null");}
    }


    private void runPostDelayed() {
        if (!prefManager.isLoggedIn()) return;
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enqueueDriverRequestCall();
                                } },
                CALL_WAIT_TIME);

    }

    private void enqueueDriverRequestCall() {
        Log.i(TAG, "enqueueDriverRequestCall: Calling " + rideDetails.requestID);
        cloneCall = driverRequestCall.clone();
        cloneCall.enqueue(new Callback<DriverResponse>() {
            @Override
            public void onResponse(Call<DriverResponse> call, Response<DriverResponse> response) {
                Log.d(TAG, "onResponse: " + response.raw());
                if (response.isSuccessful()){
                    switchStatusAndAct(response);
                } else {
                    Toast.makeText(context, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                    //TODO: cancel the request.
                }
            }

            @Override
            public void onFailure(Call<DriverResponse> call, Throwable t) {
                Toast.makeText(context, R.string.failed_to_connect_to_the_server, Toast.LENGTH_LONG).show();
                runPostDelayed();
            }
        });
    }

    private void switchStatusAndAct(Response<DriverResponse> response) {
        switch (response.body().getStatus()){
            case LOOKING_FOR_DRIVER:
                runPostDelayed();
                break;
            case REQUEST_COMPLETED:
                EventBus.getDefault().post(new RequestFinished(rideDetails.requestID));
                //TODO: prefManager.clearCurrentRide();
                break;
            case REQUEST_ACCEPTED:
                rideDetails.requestID = response.body().getErrorMessage();
                prefManager.updateOngoingRide(rideDetails);
                //TODO: update current ride.
                EventBus.getDefault().post(new DriverAccepted(
                        new Driver("unknown", "unknown", "unknown", "unknown", "unknown")));
                break;
            case AUTHENTICATION_FAILURE:
                EventBus.getDefault().post(new LogoutRequest());
                break;
        }
    }

    public void cancel(){
        tryCancelCloneCall();
        tryRemoveHandlerCallbacksAndMessages();
    }

    private void tryCancelCloneCall() {
        try {
            cloneCall.cancel();
        }catch (Exception e){
            Log.i(TAG, "cancel: Clone call is null.");
        }
    }

    private void tryRemoveHandlerCallbacksAndMessages() {
        try {
            handler.removeCallbacksAndMessages(null);
        }catch (NullPointerException e){
            Log.i(TAG, "cancel: Handler messages removed.");
        }
    }

    public String getId() {
        return rideDetails.requestID;
    }

    public boolean hasId(String requestId) {
        return rideDetails.requestID.equals(requestId);
    }

    public void restart() throws InvalidRideDetailsException {
        cancel();
        start();
    }
}
