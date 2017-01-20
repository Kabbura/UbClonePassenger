package com.wisam.driver.POJO;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by islam on 1/20/17.
 */
public class AddressResponse {

    @SerializedName(value = "results")
    private ArrayList<GeocodeAddress> results;

    public String getAddress() {
        if (results != null && results.size() > 0) {
            return results.get(0).getFormattedAddress();
        }
        return null;
    }

    class GeocodeAddress{
        @SerializedName(value = "formatted_address")
        private String formattedAddress;

        public String getFormattedAddress() {
            if (formattedAddress != null)
            return formattedAddress;
            else return "";
        }
    }
}
