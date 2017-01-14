package com.wisam.driver.ubclone;

/**
 * Created by islam on 11/15/16.
 */
public class RestServiceConstants {
//    public final static String BASE_URL = "http://192.168.43.155:8080/";
//    public final static String BASE_URL = "https://ubclone.000webhostapp.com/server/server/public/index.php/";
//    public final static String BASE_URL = "http://uberclone.000webhostapp.com/uber_apis/uber_apis/public/";
    public final static String BASE_URL = "http://wissamapps.esy.es/public/";

    public final static String
        ON_THE_WAY = "on_the_way",
        ARRIVED_PICKUP = "arrived_pickup",
        PASSENGER_ONBOARD = "passenger_onboard",
        ARRIVED_DEST = "arrived_dest",
        COMPLETED = "completed";

    public static final boolean PICKUP = true;
    public static final boolean DEST = false;
    public static final String POINT = "POINT";
    public static final int SUCCESS_RESULT_PICKUP = 0;
    public static final int FAILURE_RESULT_PICKUP = 1;
    public static final int SUCCESS_RESULT_DEST = 10;
    public static final int FAILURE_RESULT_DEST = 11;
    public static final String PACKAGE_NAME = "com.example.islam.ubclone";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

}
