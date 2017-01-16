package com.wisam.driver.ubclone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        String versionName = getString(R.string.unable_to_get_version);
             try {
                     versionName = String.format("Driver v%s",getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                   } catch (PackageManager.NameNotFoundException e) {
                      e.printStackTrace();
                }
               ((TextView)(findViewById(R.id.versionName))).setText(versionName);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
