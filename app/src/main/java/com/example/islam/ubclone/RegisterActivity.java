package com.example.islam.ubclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.islam.POJO.LoginResponse;
import com.example.islam.POJO.SimpleResponse;
import com.example.islam.POJO.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {
    private PrefManager prefManager;
    EditText fullname ;
    EditText email ;
    EditText phone;
    EditText password ;
    EditText confirmPassword;
    Spinner genderSpinner;
    private static final String TAG = "RegisterActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        prefManager = new PrefManager(this);
    }

    public void register(View view) {
        fullname = (EditText) findViewById(R.id.fullname_input);
        email = (EditText) findViewById(R.id.email_input);
        phone = (EditText) findViewById(R.id.phone_input);
        password = (EditText) findViewById(R.id.password_input);
        confirmPassword = (EditText) findViewById(R.id.confirm_password_input);
        genderSpinner = (Spinner) findViewById(R.id.gender_spinner);

        if(checkFields()){
            showProgress(true);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RestServiceConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RestService service = retrofit.create(RestService.class);
            Call<SimpleResponse> call = service.register(email.getText().toString(),
                    fullname.getText().toString(),
                    password.getText().toString(),
                    phone.getText().toString(),
                    (genderSpinner.getSelectedItem().toString().equals(getString(R.string.male))?"male":"female"),
                    prefManager.getRegistrationToken()
            );
            call.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {

                    Log.d(TAG, "onResponse: " + response.raw());
                    Log.d(TAG, "onResponse: " + response.toString());
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 0){
                        User user = new User(email.getText().toString(),
                                fullname.getText().toString(),
                                genderSpinner.getSelectedItem().toString(),
                                password.getText().toString(),
                                phone.getText().toString());
                        prefManager.setIsLoggedIn(true);
                        prefManager.setUser(user);

                        Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
                        startActivity(intent);
//                showProgress(false);
                        finish();
                    } else if (response.body() != null && response.body().getErrorMessage()!= null){
                        Toast.makeText(RegisterActivity.this, response.body().getErrorMessage(), Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.unkown_error_occured, Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, R.string.failed_to_connect_to_the_server, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    Log.w(TAG, "onFailure: " + t.toString() );
                    Log.w(TAG, "onFailure: " + call.request().toString() );
                    Log.w(TAG, "onFailure: " + t.getMessage() );
                    Log.w(TAG, "onFailure: " + t.getCause() );
                }
            });
        }

    }

    private void showProgress(boolean b) {
        if (b) progressDialog = ProgressDialog.show(this, getString(R.string.registering), getString(R.string.please_wait));
        else if(progressDialog != null) progressDialog.dismiss();
    }

    private Boolean checkFields() {
        // Check fields

        if (email == null ||
                fullname == null ||
                phone == null ||
                password == null ||
                confirmPassword == null) {

            Log.e("IEC", "register: Views not found." );
            return false;
        }

        if (email.getText().toString().length() == 0 ||
                fullname.getText().toString().length() == 0 ||
                phone.getText().toString().length() == 0 ) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if ( password.getText().toString().length() < 4) {

            Toast.makeText(this, R.string.password_should_be_at_least_4, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
