package com.wisam.driver.ubclone;

import com.wisam.driver.concepts.RideLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by islam on 10/27/16.
 */
public class HistoryEntry {
    @SerializedName(value = "pickup")
    private String pickupPoint;

    @SerializedName(value = "dest")
    private String destinationPoint;

    @SerializedName(value = "time")
    private String time;

    @SerializedName(value = "price")
    private String price;

    @SerializedName(value = "request_id")
    private String id;

    @SerializedName(value = "status")
    private String status;

    @SerializedName(value = "pickup_text")
    private String pickupText;

    @SerializedName(value = "dest_text")
    private String destText;

    @SerializedName(value = "notes")
    private String notes;

    @SerializedName(value = "driver_name")
    private String driverName;

    @SerializedName(value = "driver_plate")
    private String plateNo;

    @SerializedName(value = "driver_vehicle")
    private String driverVehicle;


    public HistoryEntry(String destinationPoint, String driverName, String driverVehicle, String id, String pickupPoint, String price, String status, String time, String pickupText, String destText) {
        this.destinationPoint = destinationPoint;
        this.driverName = driverName;
        this.driverVehicle = driverVehicle;
        this.id = id;
        this.pickupPoint = pickupPoint;
        this.price = price;
        this.status = status;
        this.time = time;
        this.pickupText = pickupText;
        this.destText = destText;
    }

    public String getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(String destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public String getDriverName() {
        return (driverName == null )?"unknown":  driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverVehicle() {
        return driverVehicle;
    }

    public void setDriverVehicle(String driverVehicle) {
        this.driverVehicle = driverVehicle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(String pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDestText() {
        return destText;
    }

    public void setDestText(String destText) {
        this.destText = destText;
    }

    public String getPickupText() {
        return pickupText;
    }

    public void setPickupText(String pickupText) {
        this.pickupText = pickupText;
    }

    public String getPlateNo() {
        return (plateNo == null )?"unknown": plateNo;
    }

    public String getNotes() {
        return notes;
    }

    public RideLocation getPickupPointAsRideLocation() {
        String[] locations = pickupPoint.split(Pattern.quote(","));
        LatLng driverLocation = new LatLng(Double.valueOf(locations[0]),Double.valueOf(locations[1]));
        return new RideLocation(driverLocation);
    }

    public RideLocation getDestPointAsRideLocation() {
        String[] locations = destinationPoint.split(Pattern.quote(","));
        LatLng driverLocation = new LatLng(Double.valueOf(locations[0]),Double.valueOf(locations[1]));
        return new RideLocation(driverLocation);
    }

    public Calendar getTimeAsCalendar(){
        Long unixTime = Long.valueOf(getTime()) * 1000; // In this case, the server sends the date in seconds while unix date needs milliseconds
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unixTime);
        return calendar;
    }
}
