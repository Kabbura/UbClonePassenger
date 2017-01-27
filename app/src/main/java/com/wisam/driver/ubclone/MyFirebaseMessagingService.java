package com.wisam.driver.ubclone;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.wisam.driver.POJO.Driver;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.DriverCanceled;
import com.wisam.driver.events.DriverLocation;
import com.wisam.driver.events.DriverRejected;
import com.wisam.driver.events.DriverUpdatedStatus;
import com.wisam.driver.events.LogoutRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

//current token : dKkBmm8H48A:APA91bFQQR2f-ibM1EfuLXbIRTItS2M3l5oV4AosbyEDZLdWm9un_-CJArBXNHo-lAonoXAqrlEy-tgbik4K3Hd5NJeKjgVjSG0tavW1_swW38oUIHbRN9uwVCPE06ujZh6szCH5glgi
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private PrefManager prefManager;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefManager = new PrefManager(this);

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (!prefManager.isLoggedIn()){
            Log.i(TAG, "onMessageReceived: User is not logged in");
            return;
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getData().get("status") == null) {
                Log.w(TAG, "onMessageReceived: No status found");
                return;
            }
            Integer status = Integer.parseInt(remoteMessage.getData().get("status"));
//            if (status != 5 &&
//                    (remoteMessage.getData().get("request_id") == null) || !remoteMessage.getData().get("request_id").equals(prefManager.getRideId())){
//                Log.w(TAG, "onMessageReceived: wrong request_id");
//                return;
//            }
            Intent intent = new Intent(this, RideRequestService.class);
            startService(intent);
            switch (status){
                case 0: // Driver reject:

                    Log.i(TAG, "onMessageReceived: 0 status");
                    EventBus.getDefault().post(new DriverRejected(
                            remoteMessage.getData().get("request_id")));
                    break;
                case 1: // Driver accepted
                    Log.d(TAG, "onMessageReceived: 1 status");

                    // This message stops the RideRequestService and update the UI
                    EventBus.getDefault().post(new DriverAccepted(new Driver(
                            remoteMessage.getData().get("name"),
                            remoteMessage.getData().get("phone"),
                            remoteMessage.getData().get("plate"),
                            remoteMessage.getData().get("request_id"),
                            remoteMessage.getData().get("vehicle")
                    )));
                    sendNotification("Driver " + remoteMessage.getData().get("name") + " accepted the ride");
                    break;
                case 2: // Driver location
                    Log.d(TAG, "onMessageReceived: 2 status");
                    String location = remoteMessage.getData().get("location");
                    String[] locations = location.split(Pattern.quote(","));
                    LatLng driverLocation = new LatLng(Double.valueOf(locations[0]),Double.valueOf(locations[1]));
                    EventBus.getDefault().post(new DriverLocation(driverLocation,remoteMessage.getData().get("request_id")));
                    break;
                case 3: // Driver status
                    EventBus.getDefault().post(new DriverUpdatedStatus(remoteMessage.getData().get("message"),remoteMessage.getData().get("request_id") ));
                    switch (remoteMessage.getData().get("message")){
                        case RestServiceConstants.ON_THE_WAY:
                            sendNotification(getString(R.string.on_the_way));
                            break;
                        case RestServiceConstants.ARRIVED_PICKUP:
                            sendNotification(getString(R.string.arrived_pickup));
                            break;
                        case RestServiceConstants.PASSENGER_ONBOARD:
                            sendNotification(getString(R.string.passenger_onboard));
                            break;
                        case RestServiceConstants.ARRIVED_DEST:
                            sendNotification(getString(R.string.arrived_dest));
                            break;
                        case RestServiceConstants.COMPLETED:
                            sendNotification(getString(R.string.completed));
                            break;
                    }
                    break;
                case 4: // Driver canceled
                    EventBus.getDefault().post(new DriverCanceled(remoteMessage.getData().get("request_id")));
                    sendNotification(getString(R.string.driver_canceled_message));
                    break;
                case 5: // Logout
                    prefManager.setIsLoggedIn(false);
                    EventBus.getDefault().post(new LogoutRequest());
                    sendNotification(getString(R.string.logged_in_from_another_device));
                    break;
                case 10: // Update BASEURL
                                String baseURL = remoteMessage.getData().get("base_url");
                                if(baseURL != null) {
                                        prefManager = new PrefManager(MyFirebaseMessagingService.this);
                                        prefManager.setBaseUrl(baseURL);
                                    }
                    break;

                default:
                    Log.d(TAG, "onMessageReceived: No status");
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}