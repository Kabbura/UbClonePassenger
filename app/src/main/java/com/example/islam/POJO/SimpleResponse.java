package com.example.islam.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/19/16.
 */
public class SimpleResponse {
    @SerializedName(value = "status")
    private int status;

    @SerializedName(value = "error_msg")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatus() {
        return status;
    }

}
