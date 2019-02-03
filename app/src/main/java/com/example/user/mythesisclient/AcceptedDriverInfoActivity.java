package com.example.user.mythesisclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AcceptedDriverInfoActivity extends AppCompatActivity {

    UrlSetting myurlsetting = new UrlSetting();
    String myurl = myurlsetting.getMyurl();

    private static final String mtag = AcceptedDriverInfoActivity.class.getSimpleName();

    private static UserSession userSession;


    TextView dname,dph,dnic,plateno;
    Button okBtn;
    FloatingActionButton floatingActionButtonPhone;
    ImageView driverIMG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.accepted_driver_info_layout);

        userSession = new UserSession(this);

        Bundle bundle = getIntent().getExtras();
        final String driver_id = bundle.getString("message");

        // Set Session For Accepted Driver ID
        userSession.setAcceptedDriverID(driver_id);

        // Get Driver Information From Database
        GetDriverData getDriverData = new GetDriverData();
        final String[] driverInfo = getDriverData.getDriverInfoFromDB(driver_id);

        driverIMG = findViewById(R.id.driver_img);
        String url = driverInfo[5];
        Glide.with(this).load(myurl+url).into(driverIMG);

        dname = findViewById(R.id.driver_name_txtView);
        dname.setText(driverInfo[0]);

        dph = findViewById(R.id.driver_ph_txtView);
        dph.setText(driverInfo[1]);

        dnic = findViewById(R.id.driver_nic_txtView);
        dnic.setText(driverInfo[3]);

        plateno = findViewById(R.id.plate_number_txtView);
        plateno.setText(driverInfo[2]);

        okBtn = findViewById(R.id.accepted_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSession.setIsAccepted(false);

                finish();
            }
        });


        floatingActionButtonPhone = findViewById(R.id.call_customer_floatingBtn);
        floatingActionButtonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+driverInfo[1]));
                startActivity(callIntent);
            }
        });


    }



}
