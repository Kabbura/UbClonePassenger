package com.wisam.driver.POJO;

import com.wisam.driver.ubclone.HistoryEntry;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by islam on 11/19/16.
 */
public class RequestsResponse {
    @SerializedName(value = "status")
    private int status;

    @SerializedName(value = "rides")
    public List<HistoryEntry> rides;

    public List<HistoryEntry> getRides() {
        return rides;
    }

    public int getStatus() {
        return status;
    }
}
