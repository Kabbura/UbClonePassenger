package com.example.islam.events;

/**
 * Created by islam on 11/23/16.
 */
public class DriverUpdatedStatus {
    private String message;
    public DriverUpdatedStatus(String mMessage){
        message = mMessage;
    }

    public String getMessage() {
        return message;
    }
}
