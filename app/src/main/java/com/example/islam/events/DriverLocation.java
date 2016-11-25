package com.example.islam.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by islam on 11/24/16.
 */
public class DriverLocation {
    private LatLng driverLocation;
    public DriverLocation(LatLng mDriverLocation) {
        driverLocation = mDriverLocation;
    }

    public LatLng getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(LatLng driverLocation) {
        this.driverLocation = driverLocation;
    }

}
