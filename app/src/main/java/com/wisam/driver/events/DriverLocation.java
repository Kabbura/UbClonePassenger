package com.wisam.driver.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by islam on 11/24/16.
 */
public class DriverLocation {
    private LatLng driverLocation;
    private String requestID;

    public String getRequestID() {
        return requestID;
    }

    public DriverLocation(LatLng mDriverLocation, String requestID) {
        driverLocation = mDriverLocation;
        this.requestID = requestID;
    }

    public LatLng getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(LatLng driverLocation) {
        this.driverLocation = driverLocation;
    }

}
