package com.example.islam.ubclone;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.ref.WeakReference;

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
                String requestURL = PLACES_BASE_URL + "?" + "key" + "=" + PLACES_API_KEY
                        + "&" + "location" + "=" + "15.592791,32.534134"
                        + "&" + "rankby" + "=" + "distance";

                request = new Request.Builder()
                        .url(requestURL)
                        .build();

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
        Log.i("IEC", "onPostExecute: " + s);
        super.onPostExecute(s);
        //TODO: parse response

    }


}