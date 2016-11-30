package com.example.islam.ubclone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by islam on 11/30/16.
 */
public class OnBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, RideRequestService.class);
        context.startService(startServiceIntent);
    }
}
