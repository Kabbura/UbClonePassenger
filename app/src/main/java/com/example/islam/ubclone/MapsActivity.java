package com.example.islam.ubclone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.islam.POJO.Driver;
import com.example.islam.concepts.PriceSettings;
import com.example.islam.concepts.Ride;
import com.example.islam.concepts.RideLocation;
import com.example.islam.events.DriverAccepted;
import com.example.islam.events.DriverCanceled;
import com.example.islam.events.DriverLocation;
import com.example.islam.events.DriverUpdatedStatus;
import com.example.islam.events.LogoutRequest;
import com.example.islam.events.PriceUpdated;
import com.example.islam.events.RequestCanceled;
import com.example.islam.events.RequestCanceledFromService;
import com.example.islam.events.RideStarted;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String GOOGLE_DIRECTIONS_API = "AIzaSyDpJmpRN0BxJ76X27K0NLTGs-gDHQtoxXQ";
    private static final int GET_PICKUP_POINT = 0, GET_DESTINATION_POINT = 1,
            PLACE_AUTOCOMPLETE_REQUEST_CODE = 2, PERMISSION_REQUEST_LOCATION = 3, PERMISSION_REQUEST_CLIENT_CONNECT = 4;


    private GoogleMap mMap;
    static final private LatLng KHARTOUM_CORDS = new LatLng(15.592791, 32.534134) ;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    public String TAG = "UbClone";
    private PrefManager prefManager;
    private AddressResultReceiver mResultReceiver;

    // ====== Drivers markers ================= //
    private List<Marker> driversMarkers;
    private Marker driverMarker;


    // ====== pickup and destination points === //
    private Boolean pickupSelected;
    private Boolean destinationSelected;


    private LatLng pickupPoint;
    private Marker pickupMarker;
    private LatLng destinationPoint;
    private Marker destinationMarker;
    private Polyline routePolyline;

    private Boolean pickupTextSet;
    private Boolean destTextSet;

    // ============ Time ====================//
    private Calendar requestDate;
    private Time requestTime;
    private Boolean dateSet;

    // ============ Price ====================//
    public String price;

    public Toast toast;

    public void callDriver(View view) {
        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setMessage(getString(R.string.call_driver_message));
        alerBuilder.setPositiveButton(getString(R.string.call_driver), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:".concat(prefManager.getRideDriver().getPhone())));
                startActivity(intent);
            }
        });
        alerBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alerBuilder.show();
    }

    public void showRideInfo(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.requeset_info_layout, null);
        ((TextView) dialogView.findViewById(R.id.price_dialog_text)).setText(ride.details.price);
        ((TextView) dialogView.findViewById(R.id.note_dialog_text)).setText(ride.details.notes);

        alertDialogBuilder.setView(dialogView)

                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialogBuilder.show();
    }

    private enum PriceSet {
        NOTYET,
        SUCCESS,
        FAILURE
    }
    private PriceSet priceSet;
    private PriceSettings priceSettings;

    // =========== UI Elements ============== //
    private CardView locationsCard;
    private CardView detailsCard;
//    private CardView statusCard;
//    private LinearLayout bookLayout;
//    private LinearLayout statusLayout;
    private Button cancelButton;
    private Button bookButton;
    private RelativeLayout destinationLayout;
    private RelativeLayout pickupLayout;
    private ImageView constStartIcon;
    private ImageView constStopIcon;
    private CheckBox femaleOnlyBox;
    private TextView timeTextView;
    private TextView noteTextView;

    // =========== New UI ================== //
    private ViewGroup arrivedButtonBottomView;
    private ViewGroup cancelButtonBottomView;
    private ViewGroup detailsBottomView;
    private ViewGroup actionButtonBottomView;
    private ViewGroup driverDetailsBottomView;
    private TextView actionButtonBottom;
    private ProgressBar priceProgressBar;
    private LinearLayout priceTextBottomView;
    private TextView priceTextBottom;


    private UI_STATE UIState;

    private Ride ride;
    private Boolean firstMove;


    public enum UI_STATE{
        CONFIRM_PICKUP,
        CONFIRM_DESTINATION,
        DETAILED,
        STATUS_MESSAGE,
        FINISHED
    }

    public void setUI(UI_STATE state, String message) {
        setUI( state,  message,  prefManager.getRideDriver());
    }

    public void setUI(UI_STATE state, String message, Driver driver){
        if (state == UI_STATE.STATUS_MESSAGE){

            TextView driverStatus = (TextView) findViewById(R.id.driver_status);
            TextView driverName = (TextView) findViewById(R.id.driver_name);
            TextView vehicleNo = (TextView) findViewById(R.id.vehicle_no);
            driverStatus.setText(message);
            driverName.setText(driver.getName());
            vehicleNo.setText(driver.getPlate());

            // Setting button:
            if (message.equals(getString(R.string.passenger_onboard)) ||
                    message.equals(getString(R.string.arrived_dest)) ||
                    message.equals(getString(R.string.completed))){

                setUI(UI_STATE.FINISHED);
//                cancelButton.setText(R.string.arrived_safely);
//                cancelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                setUI(UI_STATE.STATUS_MESSAGE);
                cancelButton.setText(R.string.cancel_request);
                cancelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

            }

            // Drivers markers:
            if (!message.equals(getString(R.string.accepted_request))){
                clearDriversMarkers();
            }

        } else {
            setUI(state);

        }
    }

    public void setUI(UI_STATE state){
        UIState = state;
        switch (state){
            case CONFIRM_PICKUP:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.VISIBLE);
                destinationLayout.setVisibility(View.GONE);
//                detailsCard.setVisibility(View.GONE);
//                statusCard.setVisibility(View.VISIBLE);
//                statusCard.setVisibility(View.GONE);
//                bookLayout.setVisibility(View.VISIBLE);
//                statusLayout.setVisibility(View.INVISIBLE);
//                cancelButton.setVisibility(View.INVISIBLE);
//                bookButton.setText(R.string.confirm_pickup);
                constStartIcon.setVisibility(View.VISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                actionButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                actionButtonBottom.setText(R.string.confirm_pickup);
                arrivedButtonBottomView.setVisibility(View.GONE);
                cancelButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.GONE);


                break;

            case CONFIRM_DESTINATION:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.GONE);
                destinationLayout.setVisibility(View.VISIBLE);
//                detailsCard.setVisibility(View.GONE);
//                statusCard.setVisibility(View.VISIBLE);
//                bookLayout.setVisibility(View.VISIBLE);
//                statusLayout.setVisibility(View.INVISIBLE);
//                cancelButton.setVisibility(View.VISIBLE);
//                bookButton.setText(R.string.confirm_destination);
                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.VISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                actionButtonBottom.setText(R.string.confirm_destination);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.GONE);
                break;

            case DETAILED:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);
                if (prefManager.getUser().getGender().equals("female"))
                    femaleOnlyBox.setVisibility(View.VISIBLE);
                else femaleOnlyBox.setVisibility(View.GONE);
//                statusCard.setVisibility(View.VISIBLE);
//                bookLayout.setVisibility(View.VISIBLE);
//                bookButton.setText(R.string.book);
//                statusLayout.setVisibility(View.INVISIBLE);
//                cancelButton.setVisibility(View.VISIBLE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                actionButtonBottom.setText(R.string.book);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.VISIBLE);
                priceTextBottomView.setVisibility(View.GONE);
                priceProgressBar.setVisibility(View.VISIBLE);
                driverDetailsBottomView.setVisibility(View.GONE);
                break;

            case STATUS_MESSAGE:
//                locationsCard.setVisibility(View.GONE);
//                detailsCard.setVisibility(View.GONE);
//                statusCard.setVisibility(View.GONE);
//                bookLayout.setVisibility(View.INVISIBLE);
//                statusLayout.setVisibility(View.VISIBLE);
//                cancelButton.setVisibility(View.VISIBLE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.GONE);
                actionButtonBottom.setVisibility(View.GONE);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.VISIBLE);
                break;

            case FINISHED:
                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.GONE);
                arrivedButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.GONE);
                actionButtonBottom.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_CLIENT_CONNECT);
        } else {
            mGoogleApiClient.connect();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickupSelected = false;
        destinationSelected = false;
        dateSet = false;
        priceSet = PriceSet.NOTYET;
        priceSettings = new PriceSettings();
        firstMove = true;

        pickupTextSet = false;
        destTextSet = false;

        pickupPoint = new LatLng(0,0);
        destinationPoint = new LatLng(0,0);

        driversMarkers = new ArrayList<>();

        locationsCard = (CardView) findViewById(R.id.locations_card);
        detailsCard = (CardView) findViewById(R.id.details_card);
//        statusCard = (CardView) findViewById(R.id.status_card);
//        bookLayout = (LinearLayout) findViewById(R.id.book_layout);
//        statusLayout = (LinearLayout) findViewById(R.id.status_layout);
        cancelButton = (Button) findViewById(R.id.cancel_btn);
        bookButton = (Button) findViewById(R.id.book_btn);
        destinationLayout = (RelativeLayout) findViewById(R.id.destination_layout);
        pickupLayout = (RelativeLayout) findViewById(R.id.pickup_layout);
        constStartIcon = (ImageView) findViewById(R.id.const_start_icon);
        constStopIcon = (ImageView) findViewById(R.id.const_stop_icon);
        femaleOnlyBox = (CheckBox) findViewById(R.id.female_only);
        timeTextView = (TextView) findViewById(R.id.time_value);
        noteTextView = (TextView) findViewById(R.id.note_value);

        arrivedButtonBottomView = (LinearLayout) findViewById(R.id.arrived_btn_bottom_view);
        cancelButtonBottomView = (LinearLayout) findViewById(R.id.cancel_btn_bottom_view);
        detailsBottomView = (LinearLayout) findViewById(R.id.details_bottom_view);
        actionButtonBottomView = (LinearLayout) findViewById(R.id.action_btn_bottom_view);
        driverDetailsBottomView = (LinearLayout) findViewById(R.id.driver_details_view);
        actionButtonBottom = (TextView) findViewById(R.id.action_btn_bottom);
        priceProgressBar = (ProgressBar) findViewById(R.id.price_progress);
        priceProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        priceTextBottomView = (LinearLayout) findViewById(R.id.price_text_bottom_view);
        priceTextBottom = (TextView) findViewById(R.id.price_text_bottom);

        UIState = UI_STATE.CONFIRM_PICKUP;

        prefManager = new PrefManager(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        // Nav drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
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

        Intent intent = new Intent(this, RideRequestService.class);
        startService(intent);
        // Request permissions

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_LOCATION);
        } else {
            initializeLocation();
        }

    }

    private void initializeLocation() {
        // Location Request
        mLocationRequest = new LocationRequest();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(1000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(500);


        // Check device location settings
        LocationSettingsRequest.Builder locationSettingsReqBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        locationSettingsReqBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
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

        // Set UI
        setUI(UI_STATE.CONFIRM_PICKUP);

        ride = new Ride();


        // getDrivers
        ride.getDrivers(this, KHARTOUM_CORDS);
        mGoogleApiClient.connect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_REQUEST_LOCATION);
            }
            else {
                initializeLocation();

            }
        } else if(requestCode == PERMISSION_REQUEST_CLIENT_CONNECT){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_REQUEST_LOCATION);
            }
            else {
                mGoogleApiClient.connect();
            }
        }
    }




    public void setDriversMarkers(List<RideLocation> drivers) {
        for (int index = 0; index < drivers.size(); index++) {
            Marker driver = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(drivers.get(index).lat, drivers.get(index).lng))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_icon_smaller))

            );
            driversMarkers.add(driver);
        }
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
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_logout){
            logout();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        prefManager.setIsLoggedIn(false);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

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
            return;
        }
        mMap.setMyLocationEnabled(true);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KHARTOUM_CORDS, 12.0f));
    }


    @Override
    public Uri onProvideReferrer() {
        return super.onProvideReferrer();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected: I am connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.d(TAG, "onConnected: Connected");
        if (mLastLocation != null) {
            mCurrentLocation = mLastLocation;
//            Toast.makeText(this, "Connected GPlServices "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            // Get drivers
            ride.getDrivers(MapsActivity.this, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
//        else {
//            Toast.makeText(this, "Sorry, it's null", Toast.LENGTH_SHORT).show();
//
//        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if (firstMove && mCurrentLocation != null){
            Log.d(TAG, "onConnected: Moving cam");
            Log.d(TAG, "onLocationChanged: mLocation: "+mCurrentLocation.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 12.0f));
            firstMove = false;
        }
//        if(null!= mCurrentLocation)
//        Toast.makeText(this, "Updated: "+mCurrentLocation.getLatitude()+" "+mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getLocation(View view) {
        Log.d(TAG, "getLocation: Called");
//        Intent intent = new Intent(this, LocationPicker.class);
        // TODO: get latitude and longitude crashes the system when the GPS is off
//        intent.putExtra("lat",mCurrentLocation.getLatitude());
//        intent.putExtra("ltd",mCurrentLocation.getLongitude());



        try {
            LatLngBounds bounds = new LatLngBounds(new LatLng(11.616428, 24.326453), new LatLng(21.381160, 36.991820));
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setBoundsBias(bounds)
                            // .setFilter(typeFilter)
                            .build(this);
            if (view.getId() == R.id.pickup_layout){
                startActivityForResult(intent, GET_PICKUP_POINT);
            } else {
                startActivityForResult(intent, GET_DESTINATION_POINT);
            }

        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GET_PICKUP_POINT:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    setPickupPointUI(place);
                    Log.i(TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case GET_DESTINATION_POINT:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    setDestinationPointUI(place);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }

    }

    private void setPrice(PriceSet set, String priceString){
        TextView priceValue = (TextView) findViewById(R.id.price_value);
        priceSet = set;
        if (set == PriceSet.SUCCESS) {
            //TODO: handle number properly
//            priceString = priceString + "SDG";
//            priceValue.setText(priceString);
            ride.details.price = priceString;
            priceTextBottom.setText(priceString);
            priceProgressBar.setVisibility(View.GONE);
            priceTextBottomView.setVisibility(View.VISIBLE);
        } else if (set == PriceSet.NOTYET){
            priceValue.setText(R.string.calculating_price);
            ride.details.price = null;
        } else if (set == PriceSet.FAILURE){
            priceValue.setText(R.string.price_failed_to_connect);
            ride.details.price = null;
        }
    }

    private void showRoute() {
        Log.d(TAG, "showRoute: Called");
        setPrice(PriceSet.NOTYET, "0.0");
        GoogleDirection.withServerKey(GOOGLE_DIRECTIONS_API)
                .from(pickupPoint)
                .to(destinationPoint)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        // Do something here
//                        Toast.makeText(MapsActivity.this, "Route successfully computed ", Toast.LENGTH_SHORT).show();
                        toast.setText("Route successfully computed ");
                        toast.show();
                        Log.d(TAG, "showRoute: Route successfully computed ");

                        if(direction.isOK()) {
                            // Check if user hasn't cancelled:
                            if (UIState != UI_STATE.DETAILED){
                                return;
                            }

                            // Do
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);


//                            Double priceValue = (double) (Integer.valueOf(distance) *  Integer.valueOf(duration) / 3/60/1000);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, getResources().getColor(R.color.colorPrimary));
                            if (routePolyline != null) {
                                routePolyline.remove();
                            }
                            routePolyline = mMap.addPolyline(polylineOptions);


                            // Distance info
                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            String distance = distanceInfo.getValue();
                            String duration = durationInfo.getValue();
                            calculatePrice(Integer.valueOf(duration), Integer.valueOf(distance));
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                        toast.setText( "Route Failed ");
                        toast.show();
                        showRoute();
                        Log.d(TAG, "showRoute: Route Failed ");
                    }
                });
    }

    public void calculatePrice(Integer duration, Integer distance){
        if (priceSettings.isUpdatedFromServer()){
            Double priceValue = priceSettings.getPrice(duration, distance);
//            price = String.format("%s",  priceValue.intValue()) ;
            price = String.valueOf(priceValue.intValue());
            setPrice(PriceSet.SUCCESS, price);
        } else {
            priceSettings.updateFromServer(duration, distance, true);
        }
    }



    private void setPickupPointUI(Place place) {
        if (place == null) {
            return;
        }
        pickupTextSet = true;
        ride.details.pickupText = (String) place.getName();
        TextView textView = (TextView) findViewById(R.id.pickup_value);
        if (textView != null) {
            textView.setText(place.getName());
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    private void setDestinationPointUI(Place place) {
        if (place == null) {
            return;
        }
        destTextSet = true;
        ride.details.destText = (String) place.getName();
        TextView textView = (TextView) findViewById(R.id.destination_value);
        if (textView != null) {
            textView.setText(place.getName());
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setPickupPoint(LatLng point){

        // Setting marker
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        pickupSelected = true;
        pickupPoint = point;
        ride.details.pickup = new RideLocation(point);

        pickupMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
//                    .title(data.getStringExtra("name"))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup_loc_ic_small))
                        .draggable(true)
        );

        // For zooming automatically to the location of the marker
        LatLng newCameraLocation = new LatLng(pickupPoint.latitude+0.01, pickupPoint.longitude+0.01);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(newCameraLocation).zoom(mMap.getCameraPosition().zoom).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Get text:
        Location location = new Location("");
        location.setLatitude(point.latitude);
        location.setLongitude(point.longitude);
        startIntentService(RestServiceConstants.PICKUP, location);
    }

    public void getNextAction(View view) {
        if (UIState == UI_STATE.CONFIRM_PICKUP){
            setPickupPoint(mMap.getCameraPosition().target);
            setUI(UI_STATE.CONFIRM_DESTINATION);

        } else if (UIState == UI_STATE.CONFIRM_DESTINATION) {

            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            destinationSelected = true;
            destinationPoint = mMap.getCameraPosition().target;
            ride.details.dest = new RideLocation(destinationPoint);
            destinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(destinationPoint)
//                    .title(data.getStringExtra("name"))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dest_loc_ic_small))
                            .draggable(true)
            );

            // Get text
            Location location = new Location("");
            location.setLatitude(destinationPoint.latitude);
            location.setLongitude(destinationPoint.longitude);
            startIntentService(RestServiceConstants.DEST, location);

            priceSettings.updateFromServer();
            showRoute();
            setUI(UI_STATE.DETAILED);
        } else if (UIState == UI_STATE.DETAILED)
        {
            //Check if request is ready:
            if (priceSet == PriceSet.SUCCESS ){
//                ride.details.price="4";
                if (pickupTextSet && destTextSet){
                    ride.details.femaleOnly = femaleOnlyBox.isChecked();
                    ride.makeRequest(MapsActivity.this);
                } else {
                    toast.setText("Connecting...");
                    toast.show();
                    if (!pickupTextSet){
                        // Get text
                        Location location = new Location("");
                        location.setLatitude(pickupPoint.latitude);
                        location.setLongitude(pickupPoint.longitude);
                        startIntentService(RestServiceConstants.PICKUP, location);
                    }
                    if (!destTextSet){
                        // Get text
                        Location location = new Location("");
                        location.setLatitude(destinationPoint.latitude);
                        location.setLongitude(destinationPoint.longitude);
                        startIntentService(RestServiceConstants.DEST, location);

                    }
                }
            } else {
                toast.setText("Wait until price is calculated");
                toast.show();
            }
        }
    }

    public void setArrived(View view) {
        if (prefManager.getRideStatus().equals(PrefManager.PASSENGER_ONBOARD) ||
                prefManager.getRideStatus().equals(PrefManager.ARRIVED_DEST)) {
            ride.arrived(this);
        }  else if (prefManager.getRideStatus().equals(PrefManager.COMPLETED)) {
            EventBus.getDefault().post(new RequestCanceled());
            Toast.makeText(this, "Thank you for booking with us.", Toast.LENGTH_LONG).show();
        }
    }


    public void cancelRequest(View view) {
        // The cancel button behavior depends on the UI state:
        Log.i(TAG, "cancelRequest");
        if (prefManager.getRideStatus().equals(PrefManager.NO_RIDE)){
//            resetRequest();
            EventBus.getDefault().post(new RequestCanceled());
        } else if (prefManager.getRideStatus().equals(PrefManager.ARRIVED_PICKUP)){
            Toast.makeText(this, "Driver has arrived. Contact driver to cancel.", Toast.LENGTH_LONG).show();
//        } else if (prefManager.getRideStatus().equals(PrefManager.PASSENGER_ONBOARD) ||
//                prefManager.getRideStatus().equals(PrefManager.ARRIVED_DEST)) {
//            ride.arrived(this);
//        } else if (prefManager.getRideStatus().equals(PrefManager.COMPLETED)) {
//            resetRequest();
//            Toast.makeText(this, "Thank you for booking with us.", Toast.LENGTH_LONG).show();
        } else {
            ride.cancelRequest(this);
        }
    }


    public void resetRequestUI(){
        Log.i(TAG, "resetRequest");
        setUI(UI_STATE.CONFIRM_PICKUP);
        pickupSelected = false;
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        ((TextView) findViewById(R.id.pickup_value)).setText(R.string.search_placeholder);

        destinationSelected = false;
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        ((TextView) findViewById(R.id.destination_value)).setText(R.string.search_placeholder);

        // remove route
        if (routePolyline != null) {
            routePolyline.remove();
        }

        pickupTextSet = false;
        destTextSet = false;

        ride.details.reset();
        ride.getDrivers(this, KHARTOUM_CORDS);
        setPrice(PriceSet.NOTYET,"0.0");
        timeTextView.setText(R.string.now);
        noteTextView.setText(R.string.note_place_holder);

        // Moving cam
        LatLng newCameraLocation;
        if (mCurrentLocation != null) {
            newCameraLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            newCameraLocation = KHARTOUM_CORDS;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(newCameraLocation).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Reset cancel button
        cancelButton.setText(R.string.cancel_request);
        cancelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));


    }

    public void getTime(View view) {
        dateSet = false;

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.time_picker);
        dialog.setTitle(R.string.select_date);
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
                        requestDate =  Calendar.getInstance(TimeZone.getTimeZone("Africa/Khartoum"));
                        //new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    } else {
                        int hour = 0;
                        int min = 0;

                        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                            hour = timePicker.getHour();
                            min = timePicker.getMinute();
                        } else {
                            hour = timePicker.getCurrentHour();
                            min = timePicker.getCurrentMinute();
                        }
                        requestDate.set(datePicker.getYear(),datePicker.getMonth(), datePicker.getDayOfMonth(),hour,min);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        format.setTimeZone(TimeZone.getTimeZone("Africa/Khartoum"));
                        String formatted = format.format(requestDate.getTime());
                        timeTextView.setText(formatted);
//                        timeTextView.setText(String.valueOf(requestDate.getTime().getTime()/1000));

                        ride.details.now = false;
                        ride.details.time = requestDate;
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
                ride.details.notes = ((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialogBuilder.show();

    }


    public void clearDriversMarkers(){
        for (int index = 0; index < driversMarkers.size(); index++) {
            Marker driver = driversMarkers.get(index);
            if (driver != null) {
                driver.remove();
            }
            driversMarkers.remove(index);
        }
    }

    @Override
    protected void onResume() {
        validateSession();
        Log.i(TAG, "onResume: RideStatus: "+ prefManager.getRideStatus());
        if (prefManager.getRideStatus().equals(PrefManager.FINDING_DRIVER)) {
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.finding_a_driver), prefManager.getRideDriver());
            EventBus.getDefault().post(new RideStarted());
        } else if (prefManager.getRideStatus().equals(PrefManager.DRIVER_ACCEPTED)){
            clearDriversMarkers();
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.accepted_request), prefManager.getRideDriver());
        } else if (prefManager.getRideStatus().equals(PrefManager.ON_THE_WAY)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.on_the_way), prefManager.getRideDriver());
        } else if (prefManager.getRideStatus().equals(PrefManager.ARRIVED_PICKUP)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_pickup), prefManager.getRideDriver());
        } else if (prefManager.getRideStatus().equals(PrefManager.PASSENGER_ONBOARD)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.passenger_onboard), prefManager.getRideDriver());
        } else if (prefManager.getRideStatus().equals(PrefManager.ARRIVED_DEST)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_dest), prefManager.getRideDriver());
        } else if (prefManager.getRideStatus().equals(PrefManager.COMPLETED)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.completed), prefManager.getRideDriver());
        } else {
            // When the activity is resuming after onActivityResults, we do not want to reset it.
            if (UIState != UI_STATE.CONFIRM_PICKUP && UIState != UI_STATE.CONFIRM_DESTINATION){
                EventBus.getDefault().post(new RequestCanceled());
            }
        }
        super.onResume();
    }

    protected void startIntentService(Boolean point, Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RestServiceConstants.RECEIVER, mResultReceiver);
        intent.putExtra(RestServiceConstants.LOCATION_DATA_EXTRA, location);
        intent.putExtra(RestServiceConstants.POINT, point);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
//            mAddressOutput = resultData.getString(RestServiceConstants.RESULT_DATA_KEY);
//            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == RestServiceConstants.SUCCESS_RESULT_PICKUP) {
                pickupTextSet = true;
                ride.details.pickupText = resultData.getString(RestServiceConstants.RESULT_DATA_KEY);
            } else if (resultCode == RestServiceConstants.SUCCESS_RESULT_DEST){
                destTextSet = true;
                ride.details.destText = resultData.getString(RestServiceConstants.RESULT_DATA_KEY);
            } else {
                Log.w(TAG, "onReceiveResult: Address: "+ resultData.getString(RestServiceConstants.RESULT_DATA_KEY));
            }

        }
    }

    // ==================== FCM Events ==================== //:

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverAccepted(DriverAccepted driverAccepted){
        Log.d(TAG, "onDriverAccepted: A driver has accepted");
        validateSession();
        ride.setDriver(driverAccepted.getDriver());
        setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.accepted_request), driverAccepted.getDriver());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void validateSession() {
        if (!prefManager.isLoggedIn()){
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverUpdatedStatus(DriverUpdatedStatus driverUpdatedStatus){
        Log.i(TAG, "onDriverUpdatedStatus: called");
        validateSession();
        switch (driverUpdatedStatus.getMessage()){
            case RestServiceConstants.ON_THE_WAY:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.on_the_way), prefManager.getRideDriver());
                break;
            case RestServiceConstants.ARRIVED_PICKUP:

                if (driverMarker != null) driverMarker.remove();
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_pickup), prefManager.getRideDriver());
                break;
            case RestServiceConstants.PASSENGER_ONBOARD:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.passenger_onboard), prefManager.getRideDriver());
                break;
            case RestServiceConstants.ARRIVED_DEST:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_dest), prefManager.getRideDriver());
                break;
            case RestServiceConstants.COMPLETED:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.completed), prefManager.getRideDriver());
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverLocation(DriverLocation driverLocation){
        Log.i(TAG, "onDriverLocation: called");
        if (driverMarker != null) driverMarker.remove();

        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLocation.getDriverLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_coming_icon_smaller))
        );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverCanceled(DriverCanceled driverCanceled){
        Log.i(TAG, "onDriverCanceled: called");
        Toast.makeText(this, R.string.driver_canceled_message, Toast.LENGTH_LONG).show();
        EventBus.getDefault().post(new RequestCanceled());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutRequest(LogoutRequest logoutRequest){
        logout();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestCanceled(RequestCanceled requestCanceled) {
        resetRequestUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPriceUpdated(PriceUpdated priceUpdated){
        calculatePrice(priceUpdated.getDuration(), priceUpdated.getDistance());
    }


}
