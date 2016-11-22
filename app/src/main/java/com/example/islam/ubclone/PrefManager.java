package com.example.islam.ubclone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.islam.POJO.User;
import com.google.firebase.iid.FirebaseInstanceId;

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

    private static final String IS_FIRST_TIME_LAUNCHED = "IsFirstTimeLaunch";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String REGISTRATION_TOKEN = "registrationToken";
    private static final String RIDE_STATUS = "RideStatus";

    // User data
    private static final String USER_FULLNAME = "UserName";
    private static final String USER_PASSWORD = "UserPassword";
    private static final String USER_EMAIL = "UserEmail";
    private static final String USER_PHONE = "UserPhone";
    private static final String USER_GENDER = "UserGender";

    public static final Integer NO_RIDE = 0,
        FINDING_DRIVER = 1,
        ON_THE_WAY = 2,
        ARRIVED_AT_PICKUP = 3,
        PASSENGER_PICKED_UP = 4,
        ARRIVED_AT_DEST = 5,
        COMPLETED = 6,
        ON_GOING_RIDE = 7;

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
