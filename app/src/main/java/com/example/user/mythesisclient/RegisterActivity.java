package com.example.user.mythesisclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by User on 2/26/2018.
 */

public class RegisterActivity extends AppCompatActivity {

    private static final String mtag = RegisterActivity.class.getSimpleName();

    // Widgets
    private static Button buttonReg2;
    private static EditText txtUsername, txtPassword1, txtPassword2, txtPhoneNum;

    // UserSession session;
    private static String Token,name,ph,pass1,pass2,message,status;
    private static String[] regStatus;
    private static boolean thread_running = true;

    // Get Url for Okhttp
    private static UrlSetting myurlsetting = new UrlSetting();
    private static String myurl = myurlsetting.getMyurl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        txtUsername = (EditText) findViewById(R.id.reg_name_edittxt);
        txtPassword1 = (EditText) findViewById(R.id.reg_password1_edittxt);
        txtPassword2 = (EditText) findViewById(R.id.reg_password2_edittxt);
        txtPhoneNum = (EditText) findViewById(R.id.reg_phnum_edittxt);
        buttonReg2 = (Button) findViewById(R.id.reg_ok_btn);

// creating an shared Preference file for the information to be stored
// first argument is the name of file and second is the mode, 0 is private mode

//        sharedPreferences = getApplicationContext().getSharedPreferences("RegisterActivity", 0);
// get editor to edit in file
//        editor = sharedPreferences.edit();

        final Pattern pattern = Pattern.compile("^(09)([1-9][0-9]{6,8})$");

        buttonReg2.setOnClickListener(new View.OnClickListener() {

            public void onClick (View v) {
                name = txtUsername.getText().toString();
                ph = txtPhoneNum.getText().toString();
                pass1 = txtPassword1.getText().toString();
                pass2 = txtPassword2.getText().toString();

                if(txtUsername.getText().length()<=0){
                    Toast.makeText(RegisterActivity.this, "Enter name", Toast.LENGTH_SHORT).show();
                }
                else if( txtPhoneNum.getText().length()<=0){
                    Toast.makeText(RegisterActivity.this, "Enter phone number", Toast.LENGTH_SHORT).show();
                }
                else if (!pattern.matcher(txtPhoneNum.getText()).matches()) {
                    Toast.makeText(RegisterActivity.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                }
                else if( txtPassword1.getText().length()<=0){
                    Toast.makeText(RegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                }
                else if( txtPassword1.getText().length()<8){
                    Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                }
                else if( txtPassword2.getText().length()<=0){
                    Toast.makeText(RegisterActivity.this, "Enter a match password", Toast.LENGTH_SHORT).show();
                }
                else if (!pass1.equals(pass2)){
                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
                else {

                    FirebaseMessaging.getInstance().subscribeToTopic("test");


                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            while (thread_running){

                                Token = FirebaseInstanceId.getInstance().getToken();

                                if (Token != null) {
                                    System.out.println("=================== Device Token is  "+Token);




                                    thread_running = false;


                                }else{
                                    System.out.println("================= Token is not loaded -");
                                }
                                try {
                                    Thread.sleep(1000);
                                }catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });t.start();

                    register();
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register(){

        try {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("Token", Token)
                    .add("username", name)
                    .add("phnum", ph)
                    .add("password", pass1)
                    .build();

            Log.w("Register",myurl);
            Request request = new Request.Builder()
                    .url(myurl+"register.php")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            regStatus = parseJson(responseBody.string());
            message = regStatus[1];
            status = regStatus[0];
            Log.w("message = ", message);
            Log.w("status = ", status);
            if (status.equals("1")) {
                // after saving the value open next activity
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
            responseBody.close();

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "Cannot connect with server",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String[] parseJson(String jsonData) throws JSONException{
        String[] result = new String[2];
        JSONObject jsonObject = new JSONObject(jsonData);

        result[0] = jsonObject.getString("success");
        result[1] = jsonObject.getString("message");
        return result;
    }
}
