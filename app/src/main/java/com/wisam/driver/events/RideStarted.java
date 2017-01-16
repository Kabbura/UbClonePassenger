package com.wisam.driver.events;

import android.content.Context;

import com.wisam.driver.concepts.Ride;

/**
 * Created by islam on 11/23/16.
 */
public class RideStarted {
    private Ride ride;
    public RideStarted(Ride.RideDetails details, Context context ){
        ride = new Ride(context);
        ride.details = details;
    }
    public Ride.RideDetails getDetails(){
        return ride.details;
    }

}
