package com.example.islam.events;

import com.example.islam.POJO.Driver;

/**
 * Created by islam on 11/23/16.
 */
public class DriverAccepted {
    private Driver driver;
    public DriverAccepted() {
    }

    public DriverAccepted(Driver mDriver) {
        driver = mDriver;
    }


    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
