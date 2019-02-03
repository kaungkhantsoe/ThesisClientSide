package com.example.user.mythesisclient;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;

import java.util.ArrayList;

public class TripInfoActivity extends AppCompatActivity{

    UrlSetting myurlsetting = new UrlSetting();
    String myurl = myurlsetting.getMyurl();

    TextView driver_name,driver_phone,plate,fromtv,totv,faretv,datetv;
    ImageView driver_image;

    String uid,did,fname,fadd,tname,tadd,fare,tdate;
    private static final String mtag = TripInfoActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        uid = String.valueOf(bundle.getInt("uid"));
        did = String.valueOf(bundle.getInt("did"));
        fname = bundle.getString("fname");
        fadd = bundle.getString("fadd");
        tname = bundle.getString("tname");
        tadd = bundle.getString("tadd");
        fare = String.valueOf(bundle.getInt("fare"));
        tdate = bundle.getString("date");


        setContentView(R.layout.trip_info_activity_layout);

        driver_image = findViewById(R.id.trip_driverImg);
        driver_name = findViewById(R.id.trip_driver_name);
        driver_phone = findViewById(R.id.trip_ph);
        plate = findViewById(R.id.trip_plate);
        fromtv = findViewById(R.id.trip_from);
        totv = findViewById(R.id.trip_to);
        faretv = findViewById(R.id.trip_price);
        datetv = findViewById(R.id.trip_date);

        GetDriverData getDriverData = new GetDriverData();
        Log.w(mtag,"Driver id : " + did);
        String[] driverdata = getDriverData.getDriverInfoFromDB(did);

        String url = driverdata[5];
        Glide.with(this).load(myurl+url).into(driver_image);

        driver_name.setText(driverdata[0]);
        driver_phone.setText(driverdata[1]);
        plate.setText(driverdata[2]);

        fromtv.setText(fname + ", " + fadd);
        totv.setText(tname + ", " + tadd);
        faretv.setText(fare);
        datetv.setText(tdate);


    }
}
