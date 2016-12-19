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

/**
 * Created by islam on 9/27/16.
 */
public class PrefManager {
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

    // Ride details
//    private static final String RIDE_PICKUP = "RidePickup";
//    private static final String RIDE_DEST = "RideDest";
//    private static final String RIDE_TIME = "RideTime";
//    private static final String RIDE_FEMALE_DRIVER = "RideFemaleDriver";
//    private static final String RIDE_NOTES = "RideNotes";
//    private static final String RIDE_PRICE = "RidePrice";
//    private static final String RIDE_PICKUP_TEXT = "RidePickupText";
//    private static final String RIDE_DEST_TEXT = "RideDestText";
    private static final String RIDE_DETAILS = "RideDetails";

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

    public void setRideStatus(Integer status){
        editor.putInt(RIDE_STATUS, status);
        editor.apply();
    }
    public Integer getRideStatus(){
        return pref.getInt(RIDE_STATUS, NO_RIDE);
    }

    public void setRideId(String rideId){
        editor.putString(RIDE_ID, rideId);
        editor.apply();
    }
    public String getRideId(){
        return pref.getString(RIDE_ID, null);
    }

    public void setRideDriver(Driver driver) {
        Gson gson = new Gson();
        String json = gson.toJson(driver);
        editor.putString(RIDE_DRIVER, json);
        editor.apply();
    }

    public Driver getRideDriver(){
        Gson gson = new Gson();
        String json = gson.toJson(new Driver("---","","---","---",""));
        json = pref.getString(RIDE_DRIVER, json);
        return gson.fromJson(json, Driver.class);
    }

    public void setRideDetails(Ride.RideDetails rideDetails) {
        Log.d("PrefManager", "setRideDetails: Called");
        Gson gson = new Gson();
        String json = gson.toJson(rideDetails);
        editor.putString(RIDE_DETAILS, json);
        editor.apply();
    }

    public Ride.RideDetails getRideDetails(){
        Log.d("PrefManager", "getRideDetails: Called");
        Gson gson = new Gson();
        Ride ride = new Ride();
        String json = gson.toJson(ride.details);
        json = pref.getString(RIDE_DETAILS, json);
        return gson.fromJson(json, Ride.RideDetails.class);
    }


    public void setUsingEnglish(boolean otherLanguage) {
        editor.putBoolean(OTHER_LANGUAGE, otherLanguage);
        editor.commit();
    }

    public boolean usingEnglish() {
        return pref.getBoolean(OTHER_LANGUAGE, true);
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
