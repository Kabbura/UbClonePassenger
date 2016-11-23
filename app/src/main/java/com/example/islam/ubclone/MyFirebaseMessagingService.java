package com.example.islam.ubclone;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.islam.POJO.Driver;
import com.example.islam.events.DriverAccepted;
import com.example.islam.events.DriverRejected;
import com.example.islam.events.DriverUpdatedStatus;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Objects;

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
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Integer status = Integer.parseInt(remoteMessage.getData().get("status"));
            switch (status){
                case 0: // Driver reject:
                    Log.i(TAG, "onMessageReceived: 0 status");
                    EventBus.getDefault().post(new DriverRejected());
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
                    break;
                case 2: // Driver location

                    break;
                case 3: // Driver status
                    EventBus.getDefault().post(new DriverUpdatedStatus(remoteMessage.getData().get("message")));
                    break;
                case 4: // Driver canceled
                    break;
                case 5: // Logout
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
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}