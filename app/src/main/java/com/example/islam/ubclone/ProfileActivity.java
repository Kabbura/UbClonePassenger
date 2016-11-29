package com.example.islam.ubclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.islam.POJO.User;
import com.example.islam.events.LogoutRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProfileActivity extends AppCompatActivity {
    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        PrefManager prefManager = new PrefManager(this);

        if (prefManager.isLoggedIn()) {
            user = prefManager.getUser();
            TextView fullname = (TextView) findViewById(R.id.profile_fullname);
            TextView phone = (TextView) findViewById(R.id.profile_phone);
            TextView email = (TextView) findViewById(R.id.profile_email);
            TextView gender = (TextView) findViewById(R.id.profile_gender);

            if (fullname != null) {
                fullname.setText(user.getFullName());
            }
            if (phone != null) {
                phone.setText(user.getPhone());
            }
            if (email != null) {
                email.setText(user.getEmail());
            }
            if (gender != null) {
                gender.setText(user.getGender());
            }

        } else {
            EventBus.getDefault().post(new LogoutRequest());
        }

    }


    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutRequest(LogoutRequest logoutRequest){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

}
