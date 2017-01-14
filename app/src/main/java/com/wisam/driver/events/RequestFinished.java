package com.wisam.driver.events;

/**
 * Created by islam on 11/23/16.
 */
public class RequestFinished {
    private String requestID;
    public RequestFinished(String requestID){
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }
}
