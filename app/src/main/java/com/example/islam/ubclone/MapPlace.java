package com.example.islam.ubclone;

/**
 * Created by islam on 10/19/16.
 */
public class MapPlace {
    private double lat;
    private double lng;
    private String name;
    private String vicinity;

    public MapPlace(double lat, double lng, String name, String vicinity) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.vicinity = vicinity;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

