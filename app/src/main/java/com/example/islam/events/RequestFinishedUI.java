package com.example.islam.events;

/**
 * Created by islam on 11/23/16.
 */
public class RequestFinishedUI {
    private String requestID;
    public RequestFinishedUI(String requestID){
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }
}
