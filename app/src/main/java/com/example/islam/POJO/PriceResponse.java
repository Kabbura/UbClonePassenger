package com.example.islam.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/30/16.
 */
public class PriceResponse {
    @SerializedName(value = "perkm")
    private Double perKm;

    @SerializedName(value = "permin")
    private Double perMinute;

    @SerializedName(value = "min")
    private Double minimumPrice;

    public Double getMinimumPrice() {
        return minimumPrice;
    }

    public Double getPerKm() {
        return perKm;
    }

    public Double getPerMinute() {
        return perMinute;
    }
}
