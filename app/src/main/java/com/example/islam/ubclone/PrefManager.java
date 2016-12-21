package com.example.islam.ubclone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.islam.POJO.Driver;
import com.example.islam.POJO.User;
import com.example.islam.concepts.Ride;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by islam on 9/27/16.
 */
public class PrefManager {
    private static final String TAG = "PrefManager";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "main_pref";

    private static final String OTHER_LANGUAGE = "OtherLanguage";
    private static final String IS_FIRST_TIME_LAUNCHED = "IsFirstTimeLaunch";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String REGISTRATION_TOKEN = "registrationToken";
    private static final String RIDE_STATUS = "RideStatus";
    private static final String RIDE_ID = "RideId";
    private static final String RIDE_DRIVER = "RideDriver";

    // User data
    private static final String USER_FULLNAME = "UserName";
    private static final String USER_PASSWORD = "UserPassword";
    private static final String USER_EMAIL = "UserEmail";
    private static final String USER_PHONE = "UserPhone";
    private static final String USER_GENDER = "UserGender";

    private static final String CURRENT_RIDE = "RideDetails";
    private static final String ONGOING_RIDES = "OngoingRides";

    public static final Integer NO_RIDE = 0,
            FINDING_DRIVER = 1,
            ON_THE_WAY = 2,
            ARRIVED_PICKUP = 3,
            PASSENGER_ONBOARD = 4,
            ARRIVED_DEST = 5,
            COMPLETED = 6,
            ON_GOING_RIDE = 7,
            DRIVER_ACCEPTED = 8;

    @SuppressLint("CommitPrefEdits")
    public PrefManager(Context context) {
        if (context != null){
            this._context = context;
            pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = pref.edit();
        }
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCHED, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCHED, true);
    }

    public void setIsLoggedIn(boolean IsLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, IsLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void setUser(User user){
        editor.putString(USER_FULLNAME, user.getFullName());
        editor.putString(USER_PASSWORD, user.getPassword());
        editor.putString(USER_EMAIL, user.getEmail());
        editor.putString(USER_PHONE, user.getPhone());
        editor.putString(USER_GENDER, user.getGender());
        editor.apply();
    }

    public User getUser() {
        return new User(pref.getString(USER_EMAIL, "No data"),
                pref.getString(USER_FULLNAME, "No data"),
                pref.getString(USER_GENDER, "No data"),
                pref.getString(USER_PASSWORD, "No data"),
                pref.getString(USER_PHONE, "No data")
        );
    }


    public void setRegistrationToken(String registrationToken) {
        editor.putString(REGISTRATION_TOKEN, registrationToken);
        editor.apply();
    }

    public String getRegistrationToken() {
        return pref.getString(REGISTRATION_TOKEN, FirebaseInstanceId.getInstance().getToken());
    }

    private void setRideStatus(Integer status){
        editor.putInt(RIDE_STATUS, status);
        editor.apply();
    }
    private Integer getRideStatus(){
        return pref.getInt(RIDE_STATUS, NO_RIDE);
    }

    private void setRideId(String rideId){
        editor.putString(RIDE_ID, rideId);
        editor.apply();
    }
    private String getRideId(){
        return pref.getString(RIDE_ID, null);
    }

    private void setRideDriver(Driver driver) {
        Gson gson = new Gson();
        String json = gson.toJson(driver);
        editor.putString(RIDE_DRIVER, json);
        editor.apply();
    }

    private Driver getRideDriver(){
        Gson gson = new Gson();
        String json = gson.toJson(new Driver("---","","---","---",""));
        json = pref.getString(RIDE_DRIVER, json);
        return gson.fromJson(json, Driver.class);
    }



    public void setCurrentRide(Ride.RideDetails rideDetails) {
//        Log.d("PrefManager", "setCurrentRide: Called");
        Gson gson = new Gson();
        String json = gson.toJson(rideDetails);
        editor.putString(CURRENT_RIDE, json);
        editor.apply();
        updateOngoingRide(rideDetails);
    }

    public Ride.RideDetails getCurrentRide(){
//        Log.d("PrefManager", "getCurrentRide: Called");
        Gson gson = new Gson();
        Ride ride = new Ride();
        String json = gson.toJson(ride.details);
        json = pref.getString(CURRENT_RIDE, json);
        return gson.fromJson(json, Ride.RideDetails.class);
    }

    public void clearCurrentRide(){
//        Log.d(TAG, "clearCurrentRide: called: ");
        Gson gson = new Gson();
        Ride ride = new Ride();
        String json = gson.toJson(ride.details);
        editor.putString(CURRENT_RIDE, json);
        editor.apply();
    }



    public void setOngoingRides(List<Ride.RideDetails> ongoingRides) {
//        Log.d("PrefManager", "setOngoingRides: Called");
        Gson gson = new Gson();
        String json = gson.toJson(ongoingRides);
        editor.putString(ONGOING_RIDES, json);
        editor.apply();
    }
    public List<Ride.RideDetails> getOngoingRides(){
//        Log.d("PrefManager", "getOngoingRides: Called");
        Gson gson = new Gson();
        Ride ride = new Ride();
        List<Ride.RideDetails> ongoingRides = new ArrayList<>();

        String json = gson.toJson(ongoingRides);
        json = pref.getString(ONGOING_RIDES, json);
        return gson.fromJson(json, new TypeToken<List<Ride.RideDetails>>(){}.getType());
    }

    // Ride is saved when the user orders a ride and the response is successful.
    private void addOngoingRide(Ride.RideDetails rideDetails){
//        Log.d(TAG, "addOngoingRide: called");
        List<Ride.RideDetails> ongoingRides = getOngoingRides();
        ongoingRides.add(rideDetails);
        setOngoingRides(ongoingRides);
    }

    public void removeOngoingRide(String requestID){
//        Log.d(TAG, "removeOngoingRide: called");
        List<Ride.RideDetails> ongoingRides = getOngoingRides();
        for (Ride.RideDetails ride : ongoingRides) {
            if (ride.requestID.equals(requestID)) {
                ongoingRides.remove(ride);
                break;
            }
        }
        setOngoingRides(ongoingRides);

        if (requestID.equals(getCurrentRide().requestID)) {
            clearCurrentRide();
        }
    }

    public Ride.RideDetails getRide(String requestID){
//        Log.d(TAG, "getRide: called");
        List<Ride.RideDetails> ongoingRides = getOngoingRides();
        for (Ride.RideDetails ride : ongoingRides) {
            if (ride.requestID.equals(requestID)) {
                return ride;
            }
        }
        return null;
    }

    /**
     * Update a ride, add it if does not exist.
     * @param rideDetails new ride
     */
    public void updateOngoingRide(Ride.RideDetails rideDetails){
//        Log.d(TAG, "updateOngoingRide: called");
        boolean found = false;
        List<Ride.RideDetails> ongoingRides = getOngoingRides();
        for (Ride.RideDetails ride : ongoingRides) {
            if (ride.requestID.equals(rideDetails.requestID)) {
                found = true;
                ongoingRides.add(ongoingRides.indexOf(ride), rideDetails);
                ongoingRides.remove(ride);
                break;
            }
        }
        if (!found){
            ongoingRides.add(rideDetails);
        }
        setOngoingRides(ongoingRides);
    }


//    public void setTicketsList(String ticketsList){
//        editor.putString(TICKETS_LIST, ticketsList);
//        editor.apply();
//    }

//    public String getTicketsList(){
//        ArrayList<EventTicket> eventsList = new ArrayList<>();
//        Gson gson = new Gson();
//        String json = gson.toJson(eventsList);
//        return pref.getString(TICKETS_LIST,json);
//    }


//    public void setPlacesList(String placesList){
//        editor.putString(PLACES_LIST, placesList);
//        editor.apply();
//    }
//
//    public String getPlacesList(){
//        ArrayList<MapPlace> placesList = new ArrayList<>();
//        Gson gson = new Gson();
//        String json = gson.toJson(placesList);
//        return pref.getString(PLACES_LIST,json);
//    }

}
