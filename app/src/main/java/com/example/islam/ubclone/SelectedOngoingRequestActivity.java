package com.example.islam.ubclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.example.islam.concepts.Ride;

public class SelectedOngoingRequestActivity extends AppCompatActivity {
    private PrefManager prefManager;
    private String requestID;
     private Ride.RideDetails ride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_ongoing_request);

        Toolbar toolbar = (Toolbar) findViewById(R.id.incoming_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefManager = new PrefManager(this);
        Intent intent = getIntent();
        requestID = intent.getStringExtra("id");

        ride = prefManager.getRide(requestID);


//        Long unixTime = Long.valueOf(ride.getTime());
        String timeString; //= String.valueOf(DateUtils.getRelativeTimeSpanString(unixTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        String time = ride.getTime();
        if (!time.equals("now")){
            Long unixTime = Long.valueOf(ride.getTime());
            timeString = String.valueOf(DateUtils.getRelativeTimeSpanString(unixTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        } else {
            timeString = time;
        }


        ((TextView) findViewById(R.id.request_details_pickup)).setText(ride.pickupText);
        ((TextView) findViewById(R.id.request_details_dest)).setText(ride.destText);
        ((TextView) findViewById(R.id.request_details_price)).setText(ride.price);
        ((TextView) findViewById(R.id.request_details_time)).setText(timeString);
        ((TextView) findViewById(R.id.request_details_driver_name)).setText(ride.getDriver().getName());
        ((TextView) findViewById(R.id.request_details_plate)).setText(ride.getDriver().getPlate());

    }

    public void setAsCurrentRie(View view) {
        prefManager.setCurrentRide(ride);
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }


}
