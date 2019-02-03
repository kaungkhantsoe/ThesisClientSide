package com.example.user.mythesisclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by User on 2/26/2018.
 */

public class LoginActivity extends AppCompatActivity {
    
    UserInfo userInfo = new UserInfo();
    UrlSetting myurlsetting = new UrlSetting();
    String myurl = myurlsetting.getMyurl();

    private final static int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private final static int MY_PERMISSIONS_ACCESS_CORSE_LOCATION = 2;

    private static boolean isFineLocationGranted = false;
    private static boolean isCorseLocationGranted = false;

    Button buttonLogin;
    EditText txtPhoneNumber, txtPassword;

    private static UserSession session;
    private static final String mtag = LoginActivity.class.getSimpleName();

    private static Context ctx = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        ctx = this;
        // User Session Manager
        session = new UserSession(this);

        checkPermissions();

        //request permissions if not granted
        if (isFineLocationGranted && isCorseLocationGranted) {
            session.createFineLocatinSession(isFineLocationGranted);
            session.createCorseLocatinSession(isCorseLocationGranted);

        }else {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    3);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        TextView register = findViewById(R.id.register_textview);
        register.setOnClickListener (new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        txtPhoneNumber = findViewById(R.id.phonenumber_edittext);
        txtPassword = findViewById(R.id.password_editext);

        txtPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_iphone_black_24dp, 0, 0, 0);
        txtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_outline_black_24dp,0,0,0);

        // User LoginActivity button
        buttonLogin = (Button) findViewById(R.id.login_button);

        // LoginActivity button click event
        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                checkPermissions();
                if (isFineLocationGranted){

                    // Get username, password from EditText
                    String phoneNumber = txtPhoneNumber.getText().toString();
                    String password = txtPassword.getText().toString();

                    try {

                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = new FormBody.Builder()
                                .add("ph_num", phoneNumber)
                                .add("password", password)
                                .build();
                        Request request = new Request.Builder()
                                .url(myurl+"login.php")
                                .post(body)
                                .build();

                        Response response = client.newCall(request).execute();
                        ResponseBody responseBody = response.body();
                        String jsonData = responseBody.string();

                        try {
                            userInfo = parseJsonData(jsonData);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        responseBody.close();

                        // Validate if phnumber, password is filled
                        if(phoneNumber.trim().length() > 0 && password.trim().length() > 0){
                            int uid = userInfo.getUser_id();
                            Log.w(mtag,"id : " + uid);
                            String uname = userInfo.getUser_name();
                            String utoken = userInfo.getToken();
                            String uPhone = userInfo.getUser_phone();
                            String uPassword =userInfo.getUser_password();

                            if(phoneNumber.equals(uPhone) && password.equals(uPassword)){

                                session.createUserLoginSession(uid,utoken,uname,uPhone,
                                        uPassword);
                                session.createCorseLocatinSession(isCorseLocationGranted);
                                session.createFineLocatinSession(isFineLocationGranted);

                                Toast.makeText(getApplicationContext(),
                                        "Logged In !",
                                        Toast.LENGTH_LONG).show();

                                // Starting MainActivity
                                Intent i = new  Intent(LoginActivity.this,MainActivity.class);

                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                // Add new Flag to start new Activity
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);


                                finish();

                            }else{

                                // username / password doesn't match&
                                Toast.makeText(getApplicationContext(),
                                        "Phone Number/Password is incorrect",
                                        Toast.LENGTH_LONG).show();

                            }
                        }else{

                            // user didn't entered username or password
                            Toast.makeText(getApplicationContext(),
                                    "Please enter phone number and password",
                                    Toast.LENGTH_LONG).show();

                        }

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),
                                "Cannot connect with server",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }else {
                    ActivityCompat.requestPermissions((Activity) ctx,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                }

            }
        });


    }

    private void checkPermissions(){
        //permissions check
        isCorseLocationGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        isFineLocationGranted = (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

    }

    private UserInfo parseJsonData(String jsonData) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonData);

        UserInfo user = new UserInfo();
        user.setUser_id(jsonObject.getInt("id"));
        user.setToken(jsonObject.getString("Token"));
        user.setUser_name(jsonObject.getString("uname"));
        user.setUser_phone(jsonObject.getString("phnum"));
        user.setUser_password(jsonObject.getString("password"));
        return user;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length < 0) {

                    for (int i = 0 ; i<grantResults.length ; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                        }
                    }
                    finish();
                }else {
                    session.createFineLocatinSession(isFineLocationGranted);
                }
                return;
            }

            case MY_PERMISSIONS_ACCESS_CORSE_LOCATION: {

                if (grantResults.length < 0) {

                    for (int i = 0 ; i<grantResults.length ; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_ACCESS_CORSE_LOCATION);
                        }
                    }
                    finish();
                }else {
                    session.createCorseLocatinSession(isCorseLocationGranted);
                }
                return;
            }
        }
    }
}
