package com.example.islam.events;

import com.example.islam.concepts.Ride;

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
