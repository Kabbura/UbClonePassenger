package com.wisam.driver.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/18/16.
 */
public class LoginResponse {
    @SerializedName(value = "status")
    private int status;

    @SerializedName(value = "user")
    private User user;

    @SerializedName(value = "error_msg")
    private String errorMessage;

    @SerializedName(value = "on_going_request")
    private String onGoingRequest;

    @SerializedName(value = "request_id")
    private String requestID;

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public String getOnGoingRequest() {
        return onGoingRequest;
    }

    public String getRequestID() {
        return requestID;
    }
}
