package com.wisam.driver.events;

/**
 * Created by islam on 11/25/16.
 */
public class DriverCanceled {
    private String requestID;

    public String getRequestID() {
        return requestID;
    }

    public DriverCanceled(String requestID){
        this.requestID = requestID;
    }
}
