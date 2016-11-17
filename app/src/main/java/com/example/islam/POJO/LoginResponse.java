package com.example.islam.POJO;

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
}
