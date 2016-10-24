package com.example.islam.ubclone;

import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int GET_PICKUP_POINT = 0;
    private static final int GET_DESTINATION_POINT = 1;

//    private static final String DRIVER_INCOMING = G
//    private static final String DRIVER_INCOMING = "Incoming";
//    private static final String DRIVER_INCOMING = "Incoming";
//    private static final String DRIVER_INCOMING = "Incoming";

    private GoogleMap mMap;
    private LatLng KhartoumCords;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    public String TAG = "UbClone";

    private Boolean pickupSelected;
    private Boolean destinationSelected;

    private LatLng pickupPoint;
    private LatLng destinationPoint;

    // ============ Time ====================//
    private Date requestDate;
    private Time requestTime;
    private Boolean dateSet;

    // =========== UI Elements ============== //
    private CardView locationsCard;
    private CardView detailsCard;
    private CardView statusCard;
    private LinearLayout bookLayout;
    private LinearLayout statusLayout;
    private Button cancelButton;


    public enum UI_STATE{
        SIMPLE,
        DETAILED,
        STATUS_MESSAGE
    }

    public void setUI(UI_STATE state, String message){
        setUI(state);
        TextView driverStatus = (TextView) findViewById(R.id.driver_status);
        if (driverStatus != null) {
            driverStatus.setText(message);
        }
    }

    public void setUI(UI_STATE state){
        switch (state){
            case SIMPLE:
                locationsCard.setVisibility(View.VISIBLE);
                detailsCard.setVisibility(View.INVISIBLE);
                statusCard.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);

                break;
            case DETAILED:
                locationsCard.setVisibility(View.VISIBLE);
                detailsCard.setVisibility(View.VISIBLE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                break;

            case STATUS_MESSAGE:
                locationsCard.setVisibility(View.INVISIBLE);
                detailsCard.setVisibility(View.INVISIBLE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.INVISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickupSelected = false;
        destinationSelected = false;
        dateSet = false;

        pickupPoint = new LatLng(0,0);
        destinationPoint = new LatLng(0,0);

        locationsCard = (CardView) findViewById(R.id.locations_card);
        detailsCard = (CardView) findViewById(R.id.details_card);
        statusCard = (CardView) findViewById(R.id.status_card);
        bookLayout = (LinearLayout) findViewById(R.id.book_layout);
        statusLayout = (LinearLayout) findViewById(R.id.status_layout);
        cancelButton = (Button) findViewById(R.id.cancel_btn);
        // Nav drawer

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // End Drawer

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // ==================== To get location ================

        // Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Location Request
        mLocationRequest = new LocationRequest();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(5000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);


        // Check device location settings
        LocationSettingsRequest.Builder locationSettingsReqBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        locationSettingsReqBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    0x1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });

        setUI(UI_STATE.SIMPLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_about){

        } else if (id == R.id.nav_logout){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        KhartoumCords = new LatLng(15.592791, 32.534134);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KhartoumCords, 12.0f));
    }


    @Override
    public Uri onProvideReferrer() {
        return super.onProvideReferrer();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mCurrentLocation = mLastLocation;
            Toast.makeText(this, "Connected GPlServices "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Sorry, it's null", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if(null!= mCurrentLocation)
        Toast.makeText(this, "Updated: "+mCurrentLocation.getLatitude()+" "+mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getLocation(View view) {
        Log.d(TAG, "getLocation: Called");
        Intent intent = new Intent(this, LocationPicker.class);

        if (view.getId() == R.id.pickup_layout){
            startActivityForResult(intent, GET_PICKUP_POINT);
        } else {
            startActivityForResult(intent, GET_DESTINATION_POINT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_PICKUP_POINT:
                if (resultCode == RESULT_OK){
                    pickupSelected = true;
                    pickupPoint =  new LatLng(data.getDoubleExtra("lat",0),data.getDoubleExtra("ltd",0) );
                    TextView textView = (TextView) findViewById(R.id.pickup_value);
                    if (textView != null) {
                        textView.setText(data.getStringExtra("name"));
                    }
                    Log.d(TAG, "onActivityResult: Pickup: "+ pickupPoint.toString());

                    // Check destination to update the UI

                    if (destinationSelected) setUI(UI_STATE.DETAILED);
                    else setUI(UI_STATE.SIMPLE);
                }
                break;
            case GET_DESTINATION_POINT:
                if (resultCode == RESULT_OK){
                    destinationSelected = true;
                    destinationPoint =  new LatLng(data.getDoubleExtra("lat",0),data.getDoubleExtra("ltd",0) );
                    TextView textView = (TextView) findViewById(R.id.destination_value);
                    if (textView != null) {
                        textView.setText(data.getStringExtra("name"));
                    }
                    Log.d(TAG, "onActivityResult: Destination: "+ destinationPoint.toString());


                    // Check destination to update the UI

                    if (pickupSelected) setUI(UI_STATE.DETAILED);
                    else setUI(UI_STATE.SIMPLE);

                }
                break;
        }

    }


    public void bookDriver(View view) {
        setUI(UI_STATE.STATUS_MESSAGE, getString(R.string.finding_a_driver));
    }

    public void cancelRequest(View view) {
        setUI(UI_STATE.SIMPLE);
        pickupSelected = false;
        ((TextView) findViewById(R.id.pickup_value)).setText("Click to choose");

        destinationSelected = false;
        ((TextView) findViewById(R.id.destination_value)).setText("Click to choose");


    }

    public void getTime(View view) {
        dateSet = false;
        final TextView timeTextView = (TextView) findViewById(R.id.time_value);

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.time_picker);
        dialog.setTitle("Select date");
        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
        final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);

        assert datePicker != null;
        datePicker.setVisibility(View.VISIBLE);
        timePicker.setVisibility(View.GONE);

        timePicker.setIs24HourView(true);

        final Button pickButton = (Button)  dialog.findViewById(R.id.pick_btn);
        if (pickButton != null) {
            pickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!dateSet){
                        dateSet = true;
                        timePicker.setVisibility(View.VISIBLE);
                        datePicker.setVisibility(View.GONE);
                        pickButton.setText("Pick Time");
                        requestDate = new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    } else {
                        timeTextView.setText( requestDate.toString());
                        dialog.dismiss();
                    }
                }
            });
        }

        Button cancelButton = (Button)  dialog.findViewById(R.id.cancel_btn);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }


    public void writeNote(View view) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final View dialogView;
        LayoutInflater inflater = getLayoutInflater();
        final TextView noteTextView = (TextView) findViewById(R.id.note_value);


        alertDialogBuilder.setMessage("Note to driver");
        dialogView = inflater.inflate(R.layout.update_dialog, null);
        EditText driverNoteInput = (EditText)dialogView.findViewById(R.id.dialog_input);
        driverNoteInput.setHint("Any note or incentive for the driver");
        assert noteTextView != null;

        if (!noteTextView.getText().equals(getString(R.string.note_place_holder))) driverNoteInput.setText(noteTextView.getText());
        driverNoteInput.setSingleLine(false);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteTextView.setText(((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString());
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    alertDialogBuilder.show();

    }



}
