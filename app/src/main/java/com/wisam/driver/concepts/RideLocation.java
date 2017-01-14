package com.wisam.driver.concepts;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/17/16.
 */
public class RideLocation {
    @SerializedName(value = "lat")
    public Double lat;
    @SerializedName(value = "lng")
    public Double lng;

    public RideLocation(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public RideLocation(LatLng latLng) {
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
    }

    @Override
    public String toString() {
        return lat.toString()+','+lng.toString();
    }
}
