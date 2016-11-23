package com.example.islam.POJO;

/**
 * Created by islam on 11/23/16.
 */
public class Driver {
    private String name;
    private String phone;
    private String vehicle;
    private String plate;
    private String request_id;

    public Driver(String name, String phone, String plate, String request_id, String vehicle) {
        this.name = name;
        this.phone = phone;
        this.plate = plate;
        this.request_id = request_id;
        this.vehicle = vehicle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
