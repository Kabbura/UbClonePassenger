package com.example.islam.POJO;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by islam on 11/15/16.
 */
public class DriversResponse {


    @SerializedName(value = "drivers")
    public List<DriverLocation> drivers;


    public List<DriverLocation> getDrivers() {
        return drivers;
    }

    public void setDrivers(List<DriverLocation> drivers) {
        this.drivers = drivers;
    }


    public class DriverLocation{

        public Double lat;
        public Double lng;

        @Override
        public String toString() {
            return lat.toString()+','+lng.toString();
        }
    }


}
