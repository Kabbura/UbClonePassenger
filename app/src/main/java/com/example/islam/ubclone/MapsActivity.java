package com.example.islam.ubclone;

import android.Manifest;
import android.app.Dialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.islam.POJO.DriversResponse;
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

import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String GOOGLE_DIRECTIONS_API = "AIzaSyDpJmpRN0BxJ76X27K0NLTGs-gDHQtoxXQ";
    private static final int GET_PICKUP_POINT = 0, GET_DESTINATION_POINT = 1, PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;

//    private static final String DRIVER_INCOMING = G
//    private static final String DRIVER_INCOMING = "Incoming";
//    private static final String DRIVER_INCOMING = "Incoming";
//    private static final String DRIVER_INCOMING = "Incoming";

    private GoogleMap mMap;
    static final private LatLng KHARTOUM_CORDS = new LatLng(15.592791, 32.534134) ;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    public String TAG = "UbClone";

    // ====== Drivers markers ================= //
    private List<Marker> driversMarkers;

    // ====== pickup and destination points === //
    private Boolean pickupSelected;
    private Boolean destinationSelected;


    private LatLng pickupPoint;
    private Marker pickupMarker;
    private LatLng destinationPoint;
    private Marker destinationMarker;
    private Polyline routePolyline;

    // ============ Time ====================//
    private Date requestDate;
    private Time requestTime;
    private Boolean dateSet;

    // ============ Price ====================//
    public String price;
    private Boolean priceSet;

    // =========== UI Elements ============== //
    private CardView locationsCard;
    private CardView detailsCard;
    private CardView statusCard;
    private LinearLayout bookLayout;
    private LinearLayout statusLayout;
    private Button cancelButton;
    private Button bookButton;
    private RelativeLayout destinationLayout;
    private RelativeLayout pickupLayout;
    private ImageView constStartIcon;
    private ImageView constStopIcon;

    private UI_STATE UIState;


    public enum UI_STATE{
        CONFIRM_PICKUP,
        CONFIRM_DESTINATION,
        ADD_DETAILS,
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
        UIState = state;
        switch (state){
            case SIMPLE:
                locationsCard.setVisibility(View.VISIBLE);
                detailsCard.setVisibility(View.INVISIBLE);
                statusCard.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);

                break;
            case CONFIRM_PICKUP:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.VISIBLE);
                destinationLayout.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                bookButton.setText(R.string.confirm_pickup);

                constStartIcon.setVisibility(View.VISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);
                break;
            case CONFIRM_DESTINATION:
                locationsCard.setVisibility(View.VISIBLE);
                pickupLayout.setVisibility(View.GONE);
                destinationLayout.setVisibility(View.VISIBLE);
                detailsCard.setVisibility(View.GONE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                bookButton.setText(R.string.confirm_destination);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.VISIBLE);
                break;
            case ADD_DETAILS:
                locationsCard.setVisibility(View.GONE);
                pickupLayout.setVisibility(View.GONE);
                destinationLayout.setVisibility(View.GONE);
                detailsCard.setVisibility(View.INVISIBLE);
                statusCard.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                break;
            case DETAILED:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.VISIBLE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.VISIBLE);
                bookButton.setText(R.string.book);
                statusLayout.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);
                break;

            case STATUS_MESSAGE:
                locationsCard.setVisibility(View.GONE);
                detailsCard.setVisibility(View.GONE);
                statusCard.setVisibility(View.VISIBLE);
                bookLayout.setVisibility(View.INVISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                constStartIcon.setVisibility(View.INVISIBLE);
                constStopIcon.setVisibility(View.INVISIBLE);
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
        priceSet = false;

        pickupPoint = new LatLng(0,0);
        destinationPoint = new LatLng(0,0);

        driversMarkers = new ArrayList<>();

        locationsCard = (CardView) findViewById(R.id.locations_card);
        detailsCard = (CardView) findViewById(R.id.details_card);
        statusCard = (CardView) findViewById(R.id.status_card);
        bookLayout = (LinearLayout) findViewById(R.id.book_layout);
        statusLayout = (LinearLayout) findViewById(R.id.status_layout);
        cancelButton = (Button) findViewById(R.id.cancel_btn);
        bookButton = (Button) findViewById(R.id.book_btn);
        destinationLayout = (RelativeLayout) findViewById(R.id.destination_layout);
        pickupLayout = (RelativeLayout) findViewById(R.id.pickup_layout);
        constStartIcon = (ImageView) findViewById(R.id.const_start_icon);
        constStopIcon = (ImageView) findViewById(R.id.const_stop_icon);

        UIState = UI_STATE.CONFIRM_PICKUP;

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

        // Set UI
        setUI(UI_STATE.CONFIRM_PICKUP);

        // getDrivers
        getDrivers(KHARTOUM_CORDS);

    }

    private void getDrivers(LatLng latLng) {
        //Creating Rest Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestServiceConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestService service = retrofit.create(RestService.class);
        String location = latLng.latitude +","+latLng.longitude;
        Call<DriversResponse> call = service.getDrivers(location);
        call.enqueue(new Callback<DriversResponse>() {
            @Override
            public void onResponse(Call<DriversResponse> call, Response<DriversResponse> response) {
                Log.d(TAG, "onResponse: Retrofit response success: Got "+ response.body().drivers.size());
                clearDriversMarkers();
                setDriversMarkers(response.body().drivers);

            }

            @Override
            public void onFailure(Call<DriversResponse> call, Throwable t) {

                Log.d(TAG, "onResponse: Retrofit response failed: "+ t.getLocalizedMessage());
//                Log.d(TAG, "onResponse: Retrofit response failed: "+ call.request().toString());
//                call.clone().enqueue(this);

            }
        });

    }

    private void setDriversMarkers(List<DriversResponse.DriverLocation> drivers) {
        for (int index = 0; index < drivers.size(); index++) {
            Marker driver = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(drivers.get(index).lat, drivers.get(index).lng))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_icon_smaller))
                    .draggable(true)
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
            // Handle the camera action
        } else if (id == R.id.nav_history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about){

        }else if (id == R.id.nav_logout){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();

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
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KHARTOUM_CORDS, 12.0f));
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
//            Toast.makeText(this, "Connected GPlServices "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            // Get drivers
            getDrivers(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
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
            LatLngBounds bounds = new LatLngBounds(new LatLng(12.951125, 35.404134), new LatLng(20.486998, 25.769819));
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

        // Always remove the route on the map



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
                    Log.i(TAG, "Place: " + place.getName());
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

    private void setPrice(Boolean set, String priceString){
        TextView priceValue = (TextView) findViewById(R.id.price_value);
        if (set) {
            priceSet = true;
            priceValue.setText(priceString + " SDG");
        } else {
            priceSet = false;
            priceValue.setText(R.string.calculating_price);
        }

    }
    private void showRoute() {
        Log.d(TAG, "showRoute: Called");

//        if (routePolyline != null) {
//            routePolyline.remove();
//        }

        GoogleDirection.withServerKey(GOOGLE_DIRECTIONS_API)
                .from(pickupPoint)
                .to(destinationPoint)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        // Do something here
                        Toast.makeText(MapsActivity.this, "Route successfully computed ", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "showRoute: Route successfully computed ");

                        if(direction.isOK()) {
                            // Do
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);

                            // Distance info
                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            String distance = distanceInfo.getValue();
                            String duration = durationInfo.getValue();

                            Double priceValue = (double) (Integer.valueOf(distance) *  Integer.valueOf(duration) / 3/60/1000);
                            price = String.format("%s", priceValue) ;
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, getResources().getColor(R.color.colorPrimary));
                            if (routePolyline != null) {
                                routePolyline.remove();
                            }
                            routePolyline = mMap.addPolyline(polylineOptions);
                            setPrice(true, price);

                        }

                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                        Toast.makeText(MapsActivity.this, "Route Failed ", Toast.LENGTH_SHORT).show();
                        setPrice(false, "0.0");
                        Log.d(TAG, "showRoute: Route Failed ");
                    }
                });
    }

    private void setPickupPointUI(Place place) {
        TextView textView = (TextView) findViewById(R.id.pickup_value);
        if (textView != null) {
            textView.setText(place.getName());
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setDestinationPointUI(Place place) {
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
        pickupMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
//                    .title(data.getStringExtra("name"))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_loc_smaller))
                        .draggable(true)
        );

        // For zooming automatically to the location of the marker
        LatLng newCameraLocation = new LatLng(pickupPoint.latitude+0.01, pickupPoint.longitude+0.01);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(newCameraLocation).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
            destinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(destinationPoint)
//                    .title(data.getStringExtra("name"))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.stop_loc_smaller))
                            .draggable(true)
            );
            showRoute();
            setUI(UI_STATE.DETAILED);
        } else if (UIState == UI_STATE.DETAILED)
        {
            setUI(UI_STATE.STATUS_MESSAGE, getString(R.string.finding_a_driver));
        }
    }

    public void cancelRequest(View view) {
        setUI(UI_STATE.CONFIRM_PICKUP);
        pickupSelected = false;
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        ((TextView) findViewById(R.id.pickup_value)).setText("Click to choose");

        destinationSelected = false;
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        ((TextView) findViewById(R.id.destination_value)).setText("Click to choose");

        // remove route
        if (routePolyline != null) {
            routePolyline.remove();
        }


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


    private void clearDriversMarkers(){
        for (int index = 0; index < driversMarkers.size(); index++) {
            Marker driver = driversMarkers.get(index);
            if (driver != null) {
                driver.remove();
            }
            driversMarkers.remove(index);
        }
    }


}
