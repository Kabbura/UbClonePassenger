package com.wisam.driver.events;

import com.wisam.driver.concepts.Ride;

/**
 * Created by islam on 11/23/16.
 */
public class RideStarted {
    private Ride ride;
    public RideStarted(Ride.RideDetails details ){
        ride = new Ride();
        ride.details = details;
    }
    public Ride.RideDetails getDetails(){
        return ride.details;
    }

}
