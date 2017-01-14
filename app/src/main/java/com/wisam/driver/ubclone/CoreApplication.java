package com.wisam.driver.ubclone;

import android.support.multidex.MultiDexApplication;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by islam on 11/10/16.
 */
public class CoreApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("Roboto-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //....
    }
}
