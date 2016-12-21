package com.example.islam.events;

/**
 * Created by islam on 11/23/16.
 */
public class RequestCanceled {
    private String requestID;
    public RequestCanceled(String requestID){
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }
}
