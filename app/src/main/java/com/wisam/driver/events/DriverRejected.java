package com.wisam.driver.events;

/**
 * Created by islam on 11/23/16.
 */
public class DriverRejected {
    private String requestID;
    public DriverRejected(String requestID) {
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }
}
