package com.wisam.driver.ubclone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
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
import android.util.TypedValue;
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
import com.wisam.driver.POJO.AddressResponse;
import com.wisam.driver.POJO.Driver;
import com.wisam.driver.concepts.PriceSettings;
import com.wisam.driver.concepts.Ride;
import com.wisam.driver.concepts.RideLocation;
import com.wisam.driver.events.DriverAccepted;
import com.wisam.driver.events.DriverCanceledUI;
import com.wisam.driver.events.DriverLocation;
import com.wisam.driver.events.DriverUpdatedStatus;
import com.wisam.driver.events.LogoutRequest;
import com.wisam.driver.events.PriceUpdated;
import com.wisam.driver.events.RequestFinished;
import com.wisam.driver.events.RequestFinishedUI;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveListener
{
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
    private List<RideLocation> driversList;


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
    private int showRouteValidCode;

    // ============ Time ====================//
    private Calendar requestDate;
    private Time requestTime;
    private Boolean dateSet;

    // ============ Price ====================//
    public String price;



    public Toast toast;
    RelativeLayout.LayoutParams relocateButtonLayoutParams;
    View locationButton;

    public void callDriver(View view) {
        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setMessage(getString(R.string.call_driver_message));
        alerBuilder.setPositiveButton(getString(R.string.call_driver), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:".concat(prefManager.getCurrentRide().getDriver().getPhone())));
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

                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialogBuilder.show();
    }

    @Override
    public void onCameraIdle() {
//        Log.i(TAG, "onCameraIdle: called");
//        driversList = new ArrayList<>();
//        driversList.add(new RideLocation(15.601046,32.561790));
//        driversList.add(new RideLocation(15.604246,32.531558));

        if (driversList != null && !driversList.isEmpty()) {
            LatLng currentLocation = mMap.getCameraPosition().target;
            Location location = new Location("");
            location.setLatitude(currentLocation.latitude);
            location.setLongitude(currentLocation.longitude);
            List<Float> locations = new ArrayList<>();
            for (int index = 0; index < driversList.size(); index++) {
                Location driverLocation = new Location("");
                driverLocation.setLatitude(driversList.get(index).lat);
                driverLocation.setLongitude(driversList.get(index).lng);
                locations.add(driverLocation.distanceTo(location));
            }
            Float min = Collections.min(locations);
            Integer minutes = Math.round(min / 1000 * 5);
            String pickupIn = minutes  + getString(R.string.spaced_minutes);
            pickupTimeText.setText(pickupIn);
        }
    }

    @Override
    public void onCameraMove() {
//        Log.i(TAG, "onCameraMove: called");
        pickupTimeText.setText(R.string.dash_minutes);
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
    private LinearLayout pickupInBottomView;
    private TextView pickupTimeText;

    private boolean comingFromOnActivityResult;


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
        setUI( state,  message,  prefManager.getCurrentRide().getDriver());
    }

    public void setUI(UI_STATE state, String message, Driver driver){
        if (state == UI_STATE.STATUS_MESSAGE){

            TextView driverStatus = (TextView) findViewById(R.id.driver_status);
            TextView driverName = (TextView) findViewById(R.id.driver_name);
            TextView vehicleNo = (TextView) findViewById(R.id.vehicle_no);
            TextView vehicle = (TextView) findViewById(R.id.vehicle_name);
            driverStatus.setText(message);
            driverName.setText(driver.getName());
            vehicleNo.setText(driver.getPlate());;
            vehicle.setText(driver.getVehicle());

            // Setting button:
            if (message.equals(getString(R.string.passenger_onboard)) ||
                    message.equals(getString(R.string.arrived_dest)) ||
                    message.equals(getString(R.string.completed))){

                setUI(UI_STATE.FINISHED);
            } else {
                setUI(UI_STATE.STATUS_MESSAGE);
                cancelButton.setText(R.string.cancel_request);
                cancelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

            }

            // Drivers markers:
            if (!message.equals(getString(R.string.accepted_request))){
                clearDriversMarkers();
            }

            // Show pickup and destination points
//            showPickupAndDestPoints();

        } else {
            setUI(state);

        }
    }

    private void showPickupAndDestPoints() {

        if (prefManager.getCurrentRide().getStatus().equals(PrefManager.NO_RIDE)) {
            return;
        }
        LatLng newPickup = new LatLng(prefManager.getCurrentRide().pickup.lat,prefManager.getCurrentRide().pickup.lng);
        LatLng newDest = new LatLng(prefManager.getCurrentRide().dest.lat,prefManager.getCurrentRide().dest.lng);
        Boolean update = false;
        if (pickupMarker == null) {
            update = true;
        } else {
            if (!pickupMarker.getPosition().equals(newPickup)) update = true;
        }

        if (update) {
            clearDriversMarkers();
            if (pickupMarker != null) {
                pickupMarker.remove();
            }
            pickupSelected = true;
            pickupPoint = new LatLng(prefManager.getCurrentRide().pickup.lat,prefManager.getCurrentRide().pickup.lng);
            ride.details.pickup = new RideLocation(pickupPoint);

            pickupMarker = mMap.addMarker(new MarkerOptions()
                            .position(pickupPoint)
//                    .title(data.getStringExtra("name"))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup_loc_ic_small))
                            .draggable(false)
            );

            // For zooming automatically to the location of the marker
            LatLng newCameraLocation = new LatLng(pickupPoint.latitude+0.01, pickupPoint.longitude+0.01);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(newCameraLocation).zoom(mMap.getCameraPosition().zoom).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

           if (destinationMarker != null) {
                destinationMarker.remove();
            }
            destinationSelected = true;
            destinationPoint = newDest;
            ride.details.dest = new RideLocation(destinationPoint);
            destinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(destinationPoint)
//                    .title(data.getStringExtra("name"))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dest_loc_ic_small))
                            .draggable(false)
            );

            // Showing route:

            if (prefManager.getCurrentRide().getRoutePolylineOptions() != null) {
                if (routePolyline != null) {
                    routePolyline.remove();
                }

                routePolyline = mMap.addPolyline(prefManager.getCurrentRide().getRoutePolylineOptions());
//            showRoute();
            }

            }

    }

    public void setUI(UI_STATE state){
        UIState = state;
        switch (state){
            case CONFIRM_PICKUP:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.VISIBLE);
                destinationLayout.setVisibility(View.GONE);
                constStartIcon.setVisibility(View.VISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                actionButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                pickupInBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setText(R.string.confirm_pickup);
                arrivedButtonBottomView.setVisibility(View.GONE);
                cancelButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.GONE);


                setRelocateButtonLocation(60);
                break;

            case CONFIRM_DESTINATION:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.GONE);
                destinationLayout.setVisibility(View.VISIBLE);
                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.VISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.VISIBLE);
                pickupInBottomView.setVisibility(View.GONE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                actionButtonBottom.setText(R.string.confirm_destination);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.GONE);

                setRelocateButtonLocation(100);
                break;

            case DETAILED:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);
                if (prefManager.getUser().getGender().equals("female"))
                    femaleOnlyBox.setVisibility(View.VISIBLE);
                else femaleOnlyBox.setVisibility(View.GONE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottom.setVisibility(View.VISIBLE);
                pickupInBottomView.setVisibility(View.GONE);
                actionButtonBottom.setText(R.string.book);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.VISIBLE);
                priceTextBottomView.setVisibility(View.GONE);
                priceProgressBar.setVisibility(View.VISIBLE);
                driverDetailsBottomView.setVisibility(View.GONE);

                setRelocateButtonLocation(220);
                break;

            case STATUS_MESSAGE:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.GONE);
                actionButtonBottom.setVisibility(View.GONE);
                arrivedButtonBottomView.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.VISIBLE);

                setRelocateButtonLocation(220);
                break;

            case FINISHED:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);
                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);

                cancelButtonBottomView.setVisibility(View.GONE);
                arrivedButtonBottomView.setVisibility(View.VISIBLE);
                actionButtonBottomView.setVisibility(View.GONE);
                actionButtonBottom.setVisibility(View.GONE);
                detailsBottomView.setVisibility(View.GONE);
                driverDetailsBottomView.setVisibility(View.VISIBLE);

                setRelocateButtonLocation(220);
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
        priceSettings = new PriceSettings(this);
        firstMove = true;

        pickupTextSet = false;
        destTextSet = false;

        pickupPoint = new LatLng(0,0);
        destinationPoint = new LatLng(0,0);

        driversMarkers = new ArrayList<>();
        driversList = new ArrayList<>();
        showRouteValidCode = 0;

        comingFromOnActivityResult = false;



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

        pickupInBottomView = (LinearLayout) findViewById(R.id.pickup_in_bottom_view);
        pickupTimeText = (TextView) findViewById(R.id.pickup_time_bottom);

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



//        prefManager.setBaseUrl("http://192.168.43.155:8080/");
//        prefManager.setBaseUrl("http://wissamapps.esy.es/public/");


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






//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RestService service = retrofit.create(RestService.class);
//
//        Call<AddressResponse> call = service.fetchAddress("15.592791,32.534134", GOOGLE_DIRECTIONS_API);
//        call.enqueue(new Callback<AddressResponse>() {
//            @Override
//            public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
//                if (response.body().getAddress() != null)
//                Log.d(TAG, "onResponse: Geocoding: " + response.body().getAddress());
//                else Log.d(TAG, "onResponse: Geocoding Null: " + response.raw());
//            }
//
//            @Override
//            public void onFailure(Call<AddressResponse> call, Throwable t) {
//                Log.d(TAG, "onFailure: Geocoding Error: " + t.getMessage());
//
//            }
//        });

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

        ride = new Ride(this);


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
        driversList = drivers;
        for (int index = 0; index < drivers.size(); index++) {
            Marker driver = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(drivers.get(index).lat, drivers.get(index).lng))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_nearby))

            );
            driversMarkers.add(driver);
        }

        onCameraIdle();
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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ongoing_requests){
            Intent intent = new Intent(this, OngoingRequestsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_new_request){
            startNewRequest();

        } else if (id == R.id.nav_history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout){
            logout();
        } else if (id == R.id.nav_change_lang){
            Configuration config = new Configuration();
            String languageToLoad;
            String deviceLocale = Locale.getDefault().getLanguage();
            if (deviceLocale.equals("en")){
                languageToLoad = "ar";
            } else {
                languageToLoad = "en";
            }

            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            config.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }

            Context context = getApplicationContext();
            context.getResources().updateConfiguration(config,context.getResources().getDisplayMetrics());
            Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startNewRequest() {
        // reset UI and sharedPreferences
        if (prefManager.getCurrentRide().getStatus().equals(PrefManager.FINDING_DRIVER)){
            toast.setText(R.string.wait_till_driver_accepts);
            toast.show();
            return;
        }
        prefManager.updateOngoingRide(prefManager.getCurrentRide());
        prefManager.clearCurrentRide();

        EventBus.getDefault().post(new RequestFinishedUI("-1"));
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

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KHARTOUM_CORDS, 12.0f));

        onCameraIdle();
        // Get the button view
        locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        // and next place it, for example, on bottom right (as Google Maps app)
        relocateButtonLayoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        relocateButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        relocateButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        relocateButtonLayoutParams.setMargins(0, 0, right, bottom);
        showPickupAndDestPoints();
    }

    public void setRelocateButtonLocation(int bottomDimension){

        if (relocateButtonLayoutParams != null && locationButton != null){
            int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomDimension, getResources().getDisplayMetrics());
            int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            relocateButtonLayoutParams.setMargins(0, 0, right, bottom);
            locationButton.requestLayout();

//            Log.d(TAG, "setRelocateButtonLocation: bottomMargin: "+ bottomDimension);
        }
    }


    @Override
    public Uri onProvideReferrer() {
        return super.onProvideReferrer();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

//        Log.d(TAG, "onConnected: I am connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
//        Log.d(TAG, "onConnected: Connected");
        if (mLastLocation != null) {
            mCurrentLocation = mLastLocation;
//            Toast.makeText(this, "Connected GPlServices "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            // Get drivers
//            ride.getDrivers(MapsActivity.this, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

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
                    comingFromOnActivityResult = true;
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    setPickupPointUI(place);
                    Log.i(TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
            case GET_DESTINATION_POINT:
                if (resultCode == RESULT_OK) {
                    comingFromOnActivityResult = true;
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    setDestinationPointUI(place);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e(TAG, status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }
    }

    private void setPrice(PriceSet set, String priceString) {
        TextView priceValue = (TextView) findViewById(R.id.price_value);
        priceSet = set;
        if (set == PriceSet.SUCCESS) {
            ride.details.price = priceString;
            priceTextBottom.setText(priceString);
            priceProgressBar.setVisibility(View.GONE);
            priceTextBottomView.setVisibility(View.VISIBLE);
        } else if (set == PriceSet.NOTYET){
            priceValue.setText(R.string.calculating_price);
            ride.details.price = null;
            priceProgressBar.setVisibility(View.VISIBLE);
            priceTextBottomView.setVisibility(View.GONE);
        } else if (set == PriceSet.FAILURE){
            priceValue.setText(R.string.price_failed_to_connect);
            ride.details.price = null;
            priceProgressBar.setVisibility(View.VISIBLE);
            priceTextBottomView.setVisibility(View.GONE);
        }
    }

    private void showRoute(final int mValidCode) {
        Log.d(TAG, "showRoute: Called");
        setPrice(PriceSet.NOTYET, "0.0");
//        GoogleDirection.withServerKey(GOOGLE_DIRECTIONS_API)
        GoogleDirection.withServerKey(getString(R.string.google_maps_key))
                .from(pickupPoint)
                .to(destinationPoint)
                .execute(new DirectionCallback() {

                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {

                        if (UIState == UI_STATE.DETAILED) {
                            // Do something here
//                        Toast.makeText(MapsActivity.this, "Route successfully computed ", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "showRoute: Route successfully computed ");

                            if (direction.isOK()) {
                                toast.setText(R.string.route_successfully_computed);
                                toast.show();
                                // Check if user hasn't cancelled:
                                if (UIState != UI_STATE.DETAILED || mValidCode != showRouteValidCode) {
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
                                Log.d(TAG, "onDirectionSuccess: Route Displayed");



                                // Distance info
                                Info distanceInfo = leg.getDistance();
                                Info durationInfo = leg.getDuration();
                                String distance = distanceInfo.getValue();
                                String duration = durationInfo.getValue();

                                ride.details.distance = Integer.valueOf(distance);
                                ride.details.duration = Integer.valueOf(duration);
                                ride.details.setRoutePolylineOptions(polylineOptions);

                                calculatePrice();
                            }else {
                                toast.setText(rawBody);
                                toast.show();
                                Log.w(TAG, "onDirectionSuccess: Directions failed");
                                Log.w(TAG, "onDirectionSuccess: " + rawBody);
                            }
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                        if (UIState != UI_STATE.DETAILED || mValidCode != showRouteValidCode) {
                            return;
                        }
                        toast.setText( R.string.route_failed);
                        toast.show();
                        showRoute(showRouteValidCode);
                        Log.d(TAG, "showRoute: Route Failed ");

                    }
                });
    }

    public void calculatePrice(){
        Integer duration = ride.details.duration;
        Integer distance = ride.details.distance;
        if (distance == null || duration == null) {
            Log.e(TAG, "calculatePrice: distance or duration is null.");
            return;
        }

        if (priceSettings.isUpdatedFromServer()){
            Double priceValue = priceSettings.getPrice(duration, distance);
//            price = String.format("%s",  priceValue.intValue()) ;
            price = String.valueOf(priceValue.intValue());
            setPrice(PriceSet.SUCCESS, price);
        } else {
            priceSettings.updateFromServer(true, "now");
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
        //startIntentService(RestServiceConstants.PICKUP, location);
        getAddress(point, true, false);
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
//            startIntentService(RestServiceConstants.DEST, location);
            getAddress(destinationPoint, false, false);

            priceSettings.updateFromServer();
            showRoute(showRouteValidCode);
            setUI(UI_STATE.DETAILED);
        } else if (UIState == UI_STATE.DETAILED)
        {
            // TESTING
//            priceSet = PriceSet.SUCCESS;
//            ride.details.price = "4";
            ////////////////////////////////////

            //Check if request is ready:
            if (priceSet == PriceSet.SUCCESS ){
//                ride.details.price="4";
                if (pickupTextSet && destTextSet){
                    ride.details.femaleOnly = femaleOnlyBox.isChecked();
                    ride.makeRequest(MapsActivity.this);
                } else {
//                    toast.setText(R.string.connecting);
//                    toast.show();
                    if (!pickupTextSet){
                        // Get text
//                        Location location = new Location("");
//                        location.setLatitude(pickupPoint.latitude);
//                        location.setLongitude(pickupPoint.longitude);
//                        startIntentService(RestServiceConstants.PICKUP, location);

                        getAddress(new LatLng(ride.details.pickup.lat, ride.details.pickup.lng), true, true);
                    }
                    if (!destTextSet){
//                        // Get text
//                        Location location = new Location("");
//                        location.setLatitude(destinationPoint.latitude);
//                        location.setLongitude(destinationPoint.longitude);
//                        startIntentService(RestServiceConstants.DEST, location);
                        getAddress(new LatLng(ride.details.dest.lat, ride.details.dest.lng), false, true);

                    }
                }
            } else {
                toast.setText(R.string.wait_until_price_is_calculating);
                toast.show();
            }
        }
    }

    public void setArrived(View view) {
        if (prefManager.getCurrentRide().getStatus().equals(PrefManager.PASSENGER_ONBOARD) ||
                prefManager.getCurrentRide().getStatus().equals(PrefManager.ARRIVED_DEST)) {
            ride.arrived(this);
        }  else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.COMPLETED)) {
            EventBus.getDefault().post(new RequestFinished(prefManager.getCurrentRide().requestID));
//            EventBus.getDefault().post(new RequestFinishedUI(prefManager.getCurrentRide().requestID));
            Toast.makeText(this, R.string.thank_you_for_booking, Toast.LENGTH_LONG).show();
        }
    }


    public void cancelRequest(View view) {
        // The cancel button behavior depends on the UI state:
        Log.i(TAG, "cancelRequest");
        if (prefManager.getCurrentRide().getStatus().equals(PrefManager.NO_RIDE)){
//            resetRequest();
            prefManager.clearCurrentRide();
            EventBus.getDefault().post(new RequestFinishedUI(prefManager.getCurrentRide().requestID));
            EventBus.getDefault().post(new RequestFinished(prefManager.getCurrentRide().requestID));
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.ARRIVED_PICKUP)){
            Toast.makeText(this, "Driver has arrived. Contact driver to cancel.", Toast.LENGTH_LONG).show();
        } else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.cancel_request_dialog_message);
            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ride.cancelRequest(MapsActivity.this);

                }
            });

            alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialogBuilder.show();


        }
    }


    public void resetRequestUI(){
        Log.i(TAG, "resetRequest");
        setUI(UI_STATE.CONFIRM_PICKUP);
        pickupSelected = false;
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (driverMarker != null) driverMarker.remove();
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
        ride.getDrivers(this, newCameraLocation);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(newCameraLocation).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Reset cancel button
        cancelButton.setText(R.string.cancel_request);
        cancelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

        showRouteValidCode++;
        comingFromOnActivityResult = false;


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

        timePicker.setIs24HourView(false);

        final Button pickButton = (Button)  dialog.findViewById(R.id.pick_btn);
        if (pickButton != null) {
            pickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!dateSet){
                        dateSet = true;
                        timePicker.setVisibility(View.VISIBLE);
                        datePicker.setVisibility(View.GONE);
                        pickButton.setText(R.string.pick_time);
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
                        SimpleDateFormat formatForServer = new SimpleDateFormat("HH:mm:ss");

                        format.setTimeZone(TimeZone.getTimeZone("Africa/Khartoum"));
                        formatForServer.setTimeZone(TimeZone.getTimeZone("Africa/Khartoum"));

                        String formatted = format.format(requestDate.getTime());
                        timeTextView.setText(formatted);
//                        timeTextView.setText(String.valueOf(requestDate.getTime().getTime()/1000));

                        ride.details.now = false;
                        ride.details.time = requestDate;

                        updatePrice(formatForServer.format(requestDate.getTime()));
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

    private void updatePrice(String time) {
        setPrice(PriceSet.NOTYET,"0.0");
        Log.d(TAG, "updatePrice: time: " + time);
        priceSettings.updateFromServer(true, time);
    }


    public void writeNote(View view) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final View dialogView;
        LayoutInflater inflater = getLayoutInflater();


        alertDialogBuilder.setMessage(R.string.note_place_holder);
        dialogView = inflater.inflate(R.layout.update_dialog, null);
        EditText driverNoteInput = (EditText)dialogView.findViewById(R.id.dialog_input);
        driverNoteInput.setHint(R.string.any_note_or_incentive);
        assert noteTextView != null;

        if (!noteTextView.getText().equals(getString(R.string.note_place_holder))) driverNoteInput.setText(noteTextView.getText());
        driverNoteInput.setSingleLine(false);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteTextView.setText(((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString());
                ride.details.notes = ((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString();
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
        super.onResume();
        Log.i(TAG, "onResume: RideStatus: "+ prefManager.getCurrentRide().getStatus());
        if (prefManager.getCurrentRide().getStatus() == null ||
                prefManager.getCurrentRide().requestID == null) {
                EventBus.getDefault().post(new RequestFinished("-1"));
                EventBus.getDefault().post(new RequestFinishedUI("-1"));
            return;
        }
        if (prefManager.getCurrentRide().getStatus().equals(PrefManager.FINDING_DRIVER)) {
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.finding_a_driver), prefManager.getCurrentRide().getDriver());
//            EventBus.getDefault().post(new RideStarted());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.DRIVER_ACCEPTED)){
            clearDriversMarkers();
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.accepted_request), prefManager.getCurrentRide().getDriver());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.ON_THE_WAY)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.on_the_way), prefManager.getCurrentRide().getDriver());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.ARRIVED_PICKUP)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_pickup), prefManager.getCurrentRide().getDriver());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.PASSENGER_ONBOARD)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.passenger_onboard), prefManager.getCurrentRide().getDriver());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.ARRIVED_DEST)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_dest), prefManager.getCurrentRide().getDriver());
        } else if (prefManager.getCurrentRide().getStatus().equals(PrefManager.COMPLETED)){
            setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.completed), prefManager.getCurrentRide().getDriver());
        } else {
            // When the activity is resuming after onActivityResults, we do not want to reset it.
            if (!isComingFromOnActivityResult()){
                comingFromOnActivityResult = false;
                EventBus.getDefault().post(new RequestFinished("-1"));
                EventBus.getDefault().post(new RequestFinishedUI("-1"));
            }
        }
    }

    private boolean isComingFromOnActivityResult() {
        return comingFromOnActivityResult;
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getAddress(LatLng latLng, final boolean pickup, final boolean showProgressAndRequestRide){
        Log.d(TAG, "getAddress: Geocoding pickup: " + pickup + " Showing Progress: " + showProgressAndRequestRide);

        final ProgressDialog progressDialog;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestService service = retrofit.create(RestService.class);

        Call<AddressResponse> call = service.fetchAddress(latLng.latitude+","+latLng.longitude, GOOGLE_DIRECTIONS_API);
        progressDialog   = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));
        if (showProgressAndRequestRide)
        progressDialog.show();
        call.enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                if (response.body().getAddress() != null){
                    Log.d(TAG, "onResponse: Geocoding: " + response.body().getAddress());
                    if (pickup) {
                        pickupTextSet = true;
                        ride.details.pickupText = response.body().getAddress();

                    } else {
                        destTextSet = true;
                        ride.details.destText = response.body().getAddress();
                    }

                }
                else {
                    Log.d(TAG, "onResponse: Geocoding Null: " + response.raw());
                    Toast.makeText(MapsActivity.this, R.string.failed_to_get_location, Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (showProgressAndRequestRide) {
                    if (pickupTextSet && destTextSet){
                        ride.details.femaleOnly = femaleOnlyBox.isChecked();
                        ride.makeRequest(MapsActivity.this);

                    } else if (!pickupTextSet){
                        Log.d(TAG, "onResponse: Getting address for pickup");
                        getAddress(new LatLng(ride.details.pickup.lat, ride.details.pickup.lng), true, true);
                    }else if (!destinationSelected){
                        Log.d(TAG, "onResponse: Getting address for dest");
                        getAddress(new LatLng(ride.details.dest.lat, ride.details.dest.lng), false, true);
                    }
                }
            }

            @Override
            public void onFailure(Call<AddressResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: Geocoding Error: " + t.getMessage());
                Toast.makeText(MapsActivity.this, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();

            }
        });
    }


    // ==================== FCM Events ==================== //:

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverAccepted(DriverAccepted driverAccepted){
        Log.d(TAG, "onDriverAccepted: A driver has accepted");
        validateSession();
        if (!prefManager.getCurrentRide().requestID.equals(driverAccepted.getRequestID())) {
            return;
        }

        ride.setDriver(driverAccepted.getDriver());
        setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.accepted_request), driverAccepted.getDriver());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void validateSession() {
        if (!prefManager.isLoggedIn()){
            Toast.makeText(this, R.string.please_login_again, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverUpdatedStatus(DriverUpdatedStatus driverUpdatedStatus){
        if (!prefManager.getCurrentRide().requestID.equals(driverUpdatedStatus.getRequestID())) {
            return;
        }
        Log.i(TAG, "onDriverUpdatedStatus: called");
        validateSession();
        switch (driverUpdatedStatus.getMessage()){
            case RestServiceConstants.ON_THE_WAY:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.on_the_way), prefManager.getCurrentRide().getDriver());
                break;
            case RestServiceConstants.ARRIVED_PICKUP:

                if (driverMarker != null) driverMarker.remove();
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_pickup), prefManager.getCurrentRide().getDriver());
                break;
            case RestServiceConstants.PASSENGER_ONBOARD:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.passenger_onboard), prefManager.getCurrentRide().getDriver());
                break;
            case RestServiceConstants.ARRIVED_DEST:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.arrived_dest), prefManager.getCurrentRide().getDriver());
                break;
            case RestServiceConstants.COMPLETED:
                setUI(MapsActivity.UI_STATE.STATUS_MESSAGE, getString(R.string.completed), prefManager.getCurrentRide().getDriver());
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverLocation(DriverLocation driverLocation){
        if (!prefManager.getCurrentRide().requestID.equals(driverLocation.getRequestID())) {
            return;
        }
        Log.i(TAG, "onDriverLocation: called");
        if (driverMarker != null) driverMarker.remove();

        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLocation.getDriverLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_coming_icon_new_small))
        );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverCanceled(DriverCanceledUI driverCanceled){
        Log.i(TAG, "onDriverCanceled: called");
        Toast.makeText(this, R.string.driver_canceled_message, Toast.LENGTH_LONG).show();
//        EventBus.getDefault().post(new RequestFinishedUI(driverCanceled.getRequestID()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutRequest(LogoutRequest logoutRequest){
        logout();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestCanceled(RequestFinishedUI requestCanceled) {
        resetRequestUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPriceUpdated(PriceUpdated priceUpdated){
        calculatePrice();
    }
}
