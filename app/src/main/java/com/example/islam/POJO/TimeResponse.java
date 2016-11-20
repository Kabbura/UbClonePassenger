package com.example.islam.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/19/16.
 */
public class TimeResponse {
    @SerializedName(value = "time")
    private Long time;

    public Long getTime() {
        return time;
    }
}
