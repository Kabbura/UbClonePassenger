package com.example.islam.ubclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

public class SelectedRequestActivity extends AppCompatActivity {

    private static final String TAG = "Selected Request";
    private Intent intent;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        Log.d(TAG, "onCreate: status: "+intent.getStringExtra("status"));
        if (intent.getStringExtra("status").equals("completed"))
            setTheme(R.style.AppTheme_details_completed);
        else if (intent.getStringExtra("status").equals("canceled"))
            setTheme(R.style.AppTheme_details_canceled);
        else setTheme(R.style.AppTheme_details_missed);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.request_details_toolbar);
        ((TextView) findViewById(R.id.request_details_toolbar_title)).setTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefManager = new PrefManager(this);


        ImageView icon = (ImageView) findViewById(R.id.derails_icon);
        if (intent.getStringExtra("status").equals("completed")) {
//                    getTheme().applyStyle(R.style.AppTheme_details_completed,true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_request_completed));
            ((TextView) findViewById(R.id.request_details_toolbar_title)).setText(R.string.status_completed);

//                    icon.setBackground(getResources().getDrawable(R.drawable.ic_request_completed));
        } else if (intent.getStringExtra("status").equals("canceled")) {
//                    getTheme().applyStyle(R.style.AppTheme_details_canceled,true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorRed));
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_request_canceled));
            ((TextView) findViewById(R.id.request_details_toolbar_title)).setText(R.string.status_cancelled);
        } else  {
            getTheme().applyStyle(R.style.AppTheme_details_missed,true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            icon.setImageDrawable(getResources().getDrawable(R.drawable.request_missed));
            ((TextView) findViewById(R.id.request_details_toolbar_title)).setText(R.string.status_no_driver);
        }



        ((TextView) findViewById(R.id.request_details_pickup)).setText(intent.getStringExtra("pickup_text"));
        ((TextView) findViewById(R.id.request_details_dest)).setText(intent.getStringExtra("dest_text"));
        ((TextView) findViewById(R.id.request_details_price)).setText(intent.getStringExtra("price"));
        ((TextView) findViewById(R.id.request_details_time)).setText(intent.getStringExtra("time"));
        ((TextView) findViewById(R.id.request_details_driver_name)).setText(intent.getStringExtra("driver_name"));
        ((TextView) findViewById(R.id.request_details_plate)).setText(intent.getStringExtra("plate"));

//        ((TextView) findViewById(R.id.request_details_notes)).setText(intent.getStringExtra("notes"));



    }
}
