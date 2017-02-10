package com.wisam.driver.events;

/**
 * Created by islam on 2/10/17.
 */
public class ControlCanceledRequest {
    private String requestID;

    public String getRequestID() {
        return requestID;
    }
    public ControlCanceledRequest(String requestId) {
        this.requestID = requestId;
    }
}
