package com.wisam.driver.events;

/**
 * Created by islam on 11/23/16.
 */
public class DriverUpdatedStatus {
    private String message;
    private String requestID;
    public DriverUpdatedStatus(String mMessage, String requestID){
        message = mMessage;
        this.requestID = requestID;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestID() {
        return requestID;
    }

}
