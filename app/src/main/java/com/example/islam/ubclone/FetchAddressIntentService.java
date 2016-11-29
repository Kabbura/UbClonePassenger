package com.example.islam.ubclone;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIntService";
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String errorMessage = "";

            // Get the location passed to this service through an extra.
            Location location = intent.getParcelableExtra(
                    RestServiceConstants.LOCATION_DATA_EXTRA);
            mReceiver = intent.getParcelableExtra(RestServiceConstants.RECEIVER);
            Boolean pickup = intent.getBooleanExtra(RestServiceConstants.POINT, RestServiceConstants.PICKUP);

            if (mReceiver == null) {
                Log.e(TAG, "onHandleIntent: it is null");
                return;
            }
            List<Address> addresses = null;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = getString(R.string.no_address_found);
                    Log.e(TAG, errorMessage);
                }
                if (pickup)deliverResultToReceiver(RestServiceConstants.FAILURE_RESULT_PICKUP, errorMessage);
                else deliverResultToReceiver(RestServiceConstants.FAILURE_RESULT_DEST, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i(TAG, getString(R.string.address_found));
               if (pickup) deliverResultToReceiver(RestServiceConstants.SUCCESS_RESULT_PICKUP,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
                else deliverResultToReceiver(RestServiceConstants.SUCCESS_RESULT_DEST,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
            }

        }
    }
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RestServiceConstants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

}
