package com.wisam.driver.events;

import android.util.Log;

import com.wisam.driver.POJO.Driver;

/**
 * Created by islam on 11/23/16.
 */
public class DriverAccepted {
    private Driver driver;
    private String requestID;
    public DriverAccepted(String requestID) {
        this.requestID = requestID;
    }

    public DriverAccepted(Driver mDriver) {
        driver = mDriver;
        requestID = driver.getRequest_id();
        Log.d("TEST", "DriverAccepted: id: " + requestID);
    }


    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getRequestID() {
        return requestID;
    }
}
