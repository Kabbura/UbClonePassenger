package com.wisam.driver.ubclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.wisam.driver.POJO.AddressResponse;
import com.wisam.driver.POJO.RequestsResponse;
import com.wisam.driver.concepts.Ride;
import com.wisam.driver.events.LogoutRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OngoingRequestsActivity extends AppCompatActivity {
    private static final String TAG = "OngoingRequestsActivity";
    private RecyclerView ongoingRequestRecyclerView;
    private OngoingRequestAdapter ongoingRequestAdapter;
    private RecyclerView.LayoutManager ongoingRequestLayoutManager;
    private List<Ride.RideDetails> ongoingRequestList;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_requests);
        prefManager = new PrefManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.incoming_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ongoingRequestRecyclerView = (RecyclerView) findViewById(R.id.future_requests);
        ongoingRequestRecyclerView.setHasFixedSize(true);
        ongoingRequestLayoutManager = new LinearLayoutManager(this);
        ongoingRequestRecyclerView.setLayoutManager(ongoingRequestLayoutManager);

        ongoingRequestList = new ArrayList<>();
        ongoingRequestList = prefManager.getOngoingRides();
        ongoingRequestAdapter = new OngoingRequestAdapter(ongoingRequestList, this);

        ongoingRequestRecyclerView.setAdapter(ongoingRequestAdapter);

        String email = "";
        String password = "";
        if (prefManager.isLoggedIn()){
            email = prefManager.getUser().getEmail();
            password = prefManager.getUser().getPassword();
        } else {
            Log.i(TAG, "onCreate: User not logged in");
            prefManager.setIsLoggedIn(false);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


        // Server request

        RestServiceConstants constants = new RestServiceConstants();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(constants.getBaseUrl(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));

        progressDialog.show();



        RestService service = retrofit.create(RestService.class);
        Call<RequestsResponse> call = service.getRequests("Basic "+ Base64.encodeToString((email + ":" + password).getBytes(),Base64.NO_WRAP));
        Log.d(TAG, "onCreate: " + call.request().toString());
        call.enqueue(new Callback<RequestsResponse>() {
            @Override
            public void onResponse(Call<RequestsResponse> call, Response<RequestsResponse> response) {


                if (progressDialog.isShowing()) progressDialog.dismiss();

                Log.d(TAG, "onResponse: raw: " + response.body());
                if (response.isSuccessful() && response.body() != null){

                    List <HistoryEntry> rides = response.body().getRides();
                    List <HistoryEntry> acceptedRequests = new ArrayList<HistoryEntry>(){{}};
                    for (HistoryEntry entry : rides){
                        if (entry.getStatus().equals("accepted")) {
                            long unixTime;
                            Log.d(TAG,"Time is :" + entry.getTime());
                            unixTime = Long.valueOf(entry.getTime()) * 1000; // In this case, the server sends the date in seconds while unix date needs milliseconds

//                            entry.setTime(String.valueOf(DateUtils.getRelativeTimeSpanString(unixTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));
//                            entry.setPrice(entry.getPrice() + " " + getString(R.string.currency));
                            acceptedRequests.add(0, entry);
                        }
                    }
                    prefManager.setOngoingRidesAsHistoryEntries(acceptedRequests);
                    OngoingRequestsActivity.this.setHistoryEntries(prefManager.getOngoingRides());
                    if (acceptedRequests.size() == 0) {
                        Toast.makeText(OngoingRequestsActivity.this, R.string.no_requests_found, Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401){
                    Toast.makeText(OngoingRequestsActivity.this, R.string.please_login_again, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onCreate: User not logged in");
                    prefManager.setIsLoggedIn(false);
                    Intent intent = new Intent(OngoingRequestsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    clearHistoryEntries();
                    Toast.makeText(OngoingRequestsActivity.this, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<RequestsResponse> call, Throwable t) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(OngoingRequestsActivity.this, R.string.failed_to_connect_to_the_server , Toast.LENGTH_SHORT).show();

            }
        });




    }
    public void setHistoryEntries(List<Ride.RideDetails> historyEntries) {
        Log.i(TAG, "setHistoryEntries: Set");
//        if (historyEntries.isEmpty()) showNoTicketsIndicator();
//        else hideNoTicketsIndicator();
        if (historyEntries != null) {
            ongoingRequestAdapter.updateDataSet((ArrayList<Ride.RideDetails>) historyEntries);
            ongoingRequestAdapter.notifyDataSetChanged();
        }
//        swipeRefreshLayout.setRefreshing(false);
    }
    public void clearHistoryEntries(){
        Log.i(TAG, "clearTickets: Cleared first called");
        ongoingRequestAdapter.clearDataSet();
//        showNoTicketsIndicator();
    }
    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutRequest(LogoutRequest logoutRequest){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
