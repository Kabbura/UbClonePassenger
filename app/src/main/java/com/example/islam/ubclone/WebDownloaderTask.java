package com.example.islam.ubclone;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by islam on 10/19/16.
 */
public class WebDownloaderTask extends AsyncTask<String, Void, String> {
    private static final String BASE_URL = "http://192.168.43.155/wp-json/iec-api/v1";
    private static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private final static String PLACES_API_KEY = "AIzaSyDjUStZ4XtLcS2PIqYcu5fdggnbweYUvtY";


    private int action;
    WeakReference<Activity> activityWeakReference;
    WeakReference<Fragment> fragmentWeakReference;


    final static public int NEARBY = 0;

    public WebDownloaderTask(Activity activity, int mAction) {
        // If called from an activity.
        activityWeakReference = new WeakReference<>(activity);
        action = mAction;
    }

    public WebDownloaderTask(Fragment fragment, int mAction) {
        // If called from a fragment, such is the case when action is get events.
        fragmentWeakReference = new WeakReference<>(fragment);
        action = mAction;

    }

    @Override
    protected String doInBackground(String... urls) {

        String route;
        OkHttpClient client = new OkHttpClient();
        Request request;
        RequestBody requestBody;
        request = new Request.Builder()
                .url(BASE_URL )
                .build();


        switch (action) {
            case NEARBY:
                try {
                    PlacesFragment placesFragment = (PlacesFragment) fragmentWeakReference.get();

                    LocationPicker locationPicker = (LocationPicker) placesFragment.getActivity();
                    String requestURL = PLACES_BASE_URL + "?" + "key" + "=" + PLACES_API_KEY
                            + "&" + "location" + "=" + Double.toString(locationPicker.mLastLocation.latitude) +","+Double.toString(locationPicker.mLastLocation.longitude)
                            + "&" + "rankby" + "=" + "distance";

                    request = new Request.Builder()
                            .url(requestURL)
                            .build();
                } catch (Exception e){
                        e.printStackTrace();
                }

                break;
            default:
                route = "/";
                request = new Request.Builder()
                        .url(BASE_URL + route)
                        .build();
        }

        Log.d("IEC", "doInBackground: " + request.toString());
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
            else
                Log.i("IEC", "doInBackground: "+response.body().string());
        } catch (Exception e) {
            return e.toString();
        }
        return "Download failed";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("UbClone", "onPostExecute: " + s);
        JSONObject response = null;
        try {
            response = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (action){
            case NEARBY:

                PlacesFragment fragment = (PlacesFragment) fragmentWeakReference.get();
                if (fragment != null) {
                    try {
                        if (response != null && 0 == response.optInt("status")) {

                            LocationPicker locationPicker = (LocationPicker) fragment.getActivity();
                            fragment.clearPlaces();
                            JSONArray placesJSONArray = response.optJSONArray("results");
                            ArrayList<MapPlace> placesList = new ArrayList<>();
                            placesList = parsePlaces(placesJSONArray, placesList, locationPicker.mLastLocation.latitude, locationPicker.mLastLocation.longitude);

                            Gson gson = new Gson();
                            String json = gson.toJson(placesList);
                            PrefManager prefManager = new PrefManager(fragment.getContext());
//                            prefManager.setPlacesList(json);
                            Log.d("UbClone", "onPostExecute: Setting EventList: " + json);


                            fragment.setPlaces(placesList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }


    }

    private ArrayList<MapPlace> parsePlaces(JSONArray placesJSONArray, ArrayList<MapPlace> placesList, Double lat, Double ltd)  throws JSONException {

        placesList.add(new MapPlace(
                    lat,
                    ltd,
                    "My current location",
                    ""
            ));
        for (int index = 0; index < placesJSONArray.length(); index++) {
            JSONObject place = placesJSONArray.getJSONObject(index);
            JSONObject geometry = place.optJSONObject("geometry");
            JSONObject location = geometry.optJSONObject("location");
            placesList.add(new MapPlace(
                    location.optDouble("lat"),
                    location.optDouble("lng"),
                    place.optString("name"),
                    place.optString("vicinity")
            ));
        }
        return placesList;

    }


}