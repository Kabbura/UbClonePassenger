package com.wisam.driver.events;

/**
 * Created by islam on 11/30/16.
 */
public class PriceUpdated {
    Integer duration;
    Integer distance;

    public PriceUpdated(Integer duration, Integer distance) {
        this.distance = distance;
        this.duration = duration;
    }

    public Integer getDistance() {
        return distance;
    }

    public Integer getDuration() {
        return duration;
    }
}
