package com.wisam.driver.ubclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wisam.driver.POJO.SimpleResponse;
import com.wisam.driver.POJO.User;
import com.wisam.driver.events.LogoutRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    public User user;
    private TextView fullname;
    private TextView phone;
    private TextView email;
    private TextView gender;
    private TextView password;

    public Boolean updated;
    private RestService service;
    String userEmail;
    String userPassword;
    private ProgressDialog progressDialog;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        prefManager = new PrefManager(this);
        updated = false;

        RestServiceConstants constants = new RestServiceConstants();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(constants.getBaseUrl(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);

        if (prefManager.isLoggedIn()) {
            user = prefManager.getUser();
            fullname = (TextView) findViewById(R.id.profile_fullname);
            phone = (TextView) findViewById(R.id.profile_phone);
            email = (TextView) findViewById(R.id.profile_email);
            gender = (TextView) findViewById(R.id.profile_gender);
            password = (TextView) findViewById(R.id.profile_password);

            userEmail = user.getEmail();
            userPassword = user.getPassword();

            if (fullname != null) {
                fullname.setText(user.getFullName());
            }
            if (phone != null) {
                phone.setText(user.getPhone());
            }
            if (email != null) {
                email.setText(user.getEmail());
            }
            if (password != null) {
                password.setText(R.string.click_to_update_password);
            }
            if (gender != null) {
                if (user.getGender().equals("male"))
                    gender.setText(getString(R.string.male));
                else gender.setText(getString(R.string.female));
            }

        } else {
            EventBus.getDefault().post(new LogoutRequest());
        }

    }


    public void updateInput(View view) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(ProfileActivity.this);
        LinearLayout.LayoutParams lp;
        final View dialogView;
        LayoutInflater inflater = getLayoutInflater();
        switch (view.getId()) {


            case R.id.profile_password_layout:
                alertDialogBuilder.setMessage(R.string.update_your_password_message);
                dialogView = inflater.inflate(R.layout.password_dialog, null);
                alertDialogBuilder.setView(dialogView)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!((EditText) dialogView.findViewById(R.id.dialog_old_password)).getText().toString().equals(user.getPassword())) {
                                    Toast.makeText(ProfileActivity.this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                } else if (((EditText) dialogView.findViewById(R.id.dialog_new_password)).getText().toString().length() < 4){
                                    Toast.makeText(ProfileActivity.this, "Password should at least be 4 digits", Toast.LENGTH_SHORT).show();
                                } else if (!((EditText) dialogView.findViewById(R.id.dialog_new_password)).getText().toString().equals(((EditText) dialogView.findViewById(R.id.dialog_confirm_password)).getText().toString())){

                                    Toast.makeText(ProfileActivity.this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
                                } else {

                                    Toast.makeText(ProfileActivity.this, R.string.password_updated, Toast.LENGTH_SHORT).show();
//                                    password.setText(((EditText) dialogView.findViewById(R.id.dialog_new_password)).getText());
                                    user.setPassword(((EditText) dialogView.findViewById(R.id.dialog_new_password)).getText().toString());
                                    updated = true;
                                }
                            }
                        })

                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                break;
            case R.id.profile_phone_layout:
                alertDialogBuilder.setMessage(R.string.update_your_phone_message);
                dialogView = inflater.inflate(R.layout.update_dialog, null);
                ((EditText)dialogView.findViewById(R.id.dialog_input)).setInputType(EditorInfo.TYPE_CLASS_PHONE);
                ((EditText)dialogView.findViewById(R.id.dialog_input)).setText(user.getPhone());
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        phone.setText(((EditText)dialogView.findViewById(R.id.dialog_input)).getText());
                        user.setPhone(((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString());
                        updated = true;
                    }
                });

                alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                break;

            case R.id.profile_fullname_layout:
                alertDialogBuilder.setMessage(R.string.update_your_fullname);
                dialogView = inflater.inflate(R.layout.update_dialog, null);
                ((EditText)dialogView.findViewById(R.id.dialog_input)).setText(user.getFullName());
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        fullname.setText(((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString());
                        user.setFullName(((EditText)dialogView.findViewById(R.id.dialog_input)).getText().toString());
                        updated = true;
                    }
                });

                alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                break;

        }
        alertDialogBuilder.show();
    }

    private void showProgress(boolean b) {
        if (b) progressDialog = ProgressDialog.show(this, getString(R.string.updating_profile_data), getString(R.string.please_wait));
        else if(progressDialog != null) progressDialog.dismiss();
    }
    public void updateUserData(View view) {

        showProgress(true);

        Call<SimpleResponse> call = service.updateProfile("Basic "+ Base64.encodeToString((userEmail + ":" + userPassword).getBytes(),Base64.NO_WRAP),
                user.getFullName(),
                user.getPhone(),
                user.getPassword());
        Log.d(TAG, "getDriver: raw: " + call.request().toString());
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                Log.d(TAG, "onResponse: " + response.raw());
                Log.d(TAG, "onResponse: " + response.toString());
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0){
                    Toast.makeText(ProfileActivity.this, R.string.profile_updated_successfully, Toast.LENGTH_LONG).show();
                    userEmail = user.getEmail();
                    userPassword = user.getPassword();
                    updated = false;
                    prefManager.setUser(user);
                    showProgress(false);
                } else if (response.body() != null && response.body().getErrorMessage()!= null){
                    Toast.makeText(ProfileActivity.this, response.body().getErrorMessage(), Toast.LENGTH_SHORT).show();
                    showProgress(false);
                } else {
                    Toast.makeText(ProfileActivity.this, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    Log.w(TAG, "onFailure: " + t.toString() );
                    Log.w(TAG, "onFailure: " + call.request().toString() );
                    Log.w(TAG, "onFailure: " + t.getMessage() );
                    Log.w(TAG, "onFailure: " + t.getCause() );
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d("IEC", "onBackPressed: Back is clicked. Updated: "+ updated.toString());

        if(updated){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.you_did_not_update_profile);
            alertDialogBuilder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


//                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                }
            });

            alertDialogBuilder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProfileActivity.super.onBackPressed();

                }
            });
            alertDialogBuilder.show();
        } else {

            ProfileActivity.super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean returnFlag = false;
        if (item.getItemId() == android.R.id.home) {
            // this.onBackPressed();
            // return false;
            Log.d("IEC", "onOptionsItemSelected: Back is pressed");
            this.onBackPressed();

        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

}
