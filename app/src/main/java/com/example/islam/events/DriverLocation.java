package com.example.islam.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by islam on 11/24/16.
 */
public class DriverLocation {
    private LatLng driverLocation;
    private String requestId;
    public DriverLocation(LatLng mDriverLocation, String mRequestId) {
        driverLocation = mDriverLocation;
        requestId = mRequestId;
    }

    public LatLng getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(LatLng driverLocation) {
        this.driverLocation = driverLocation;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
