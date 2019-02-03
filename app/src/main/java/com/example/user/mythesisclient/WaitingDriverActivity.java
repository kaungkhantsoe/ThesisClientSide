package com.example.user.mythesisclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WaitingDriverActivity extends AppCompatActivity {

    private static ProgressBar progressBar;
    private static final String mtag = WaitingDriverActivity.class.getSimpleName();
    private static PlaceInfo[] placeInfos = new PlaceInfo[2];
    private static List sortedDriverData;
    private boolean thread_running = true;
    private static UserSession userSession;
    private int count;
    private static String fees;
    long startTime = 0;
    private String[] deniedList;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            Log.w(mtag, String.valueOf(seconds) + " seconds");

            if (userSession.isAccepted()) {
                thread_running = false;
                finish();
            }
            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.waiting_layout);

        Bundle bundle = getIntent().getExtras();

        placeInfos[0] = new PlaceInfo(new LatLng(bundle.getDouble("lat"),bundle.getDouble("lng")) , bundle.getString("name") , bundle.getString("addr"));
        placeInfos[1] = new PlaceInfo(new LatLng(bundle.getDouble("dlat"),bundle.getDouble("dlng")) , bundle.getString("dname") , bundle.getString("daddr"));
        fees = bundle.getString("price");

        userSession = new UserSession(this);

        progressBar = findViewById(R.id.progressBar1);

        // Get Sorted Driver Data
        getData();

    }

    public void getData() {

        Object object = new Object();
        object = placeInfos;
        GetDriverPositionData getDriverPositionData = (GetDriverPositionData) new GetDriverPositionData(new GetDriverPositionData.AsyncResponse() {
            @Override
            public void processFinish(List output) {
                sortedDriverData = output;

                if (sortedDriverData != null) {
                    for (int i = 0 ; i < sortedDriverData.size() ; i++) {
                        DriverInfo driverInfo = (DriverInfo) sortedDriverData.get(i);
                        Log.w("Sorted Data", String.valueOf(driverInfo.getDid()));
                    }
                    sendRequest();
                }
            }
        }).execute(object);


    }

    public void sendRequest() {

        progressBar.setVisibility(View.VISIBLE);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                if (thread_running == false) {
                    stopTimer();
                }

                while (thread_running) {

                  if (!userSession.isAccepted()) {
                      stopTimer();
                      Log.w(mtag,"Preparing for count = " + count);
                      if (count < sortedDriverData.size()) {

                          sendRequestToDriver(count);

                          Log.w(mtag, "Threading.... to Count " + count);
                          startTimer();
                      }else {

                          stopTimer();
//                          progressBar.setVisibility(View.INVISIBLE);
                          thread_running = false;
                          Intent noTaxiIntent = new Intent(WaitingDriverActivity.this, NoTaxiActivity.class);
                          startActivity(noTaxiIntent);
                          finish();

                      }


                      try {
                          count++;
                          Thread.sleep(20000);// Twenty second waiting
                      }catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
                }

            }
        });t.start();


    }

    public void sendRequestToDriver(int c) {

        UrlSetting myurlsetting = new UrlSetting();
        String myurl = myurlsetting.getMyurl();
        UserInfo userInfo = userSession.getUserDetails();

        DriverInfo driverInfo = (DriverInfo) sortedDriverData.get(c);
        int driverID = driverInfo.getDid();


        Log.w(mtag, "Sending to Driver ID : " + driverID);

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("did",String.valueOf(driverID))
                    .add("uid", String.valueOf(userInfo.getUser_id()))

                    .add("from_lat", String.valueOf(placeInfos[0].getLatLng().latitude))
                    .add("from_lng", String.valueOf(placeInfos[0].getLatLng().longitude))
                    .add("from_name", placeInfos[0].getName())
                    .add("from_address", placeInfos[0].getAddress())

                    .add("to_lat", String.valueOf(placeInfos[1].getLatLng().latitude))
                    .add("to_lng", String.valueOf(placeInfos[1].getLatLng().longitude))
                    .add("to_name", placeInfos[1].getName())
                    .add("to_address", placeInfos[1].getAddress())

                    .add("price",fees)
                    .build();

            Request request = new Request.Builder()
                    .url(myurl + "request_to_driver.php")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String jsonData = responseBody.string();
            Log.w(mtag, " Request body : " + jsonData);

            responseBody.close();
        } catch (IOException e) {
            Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startTimer() {
        Log.w(mtag,"Start Time");
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable,0);
    }

    private void stopTimer() {
        Log.w(mtag, "Stop Time");
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onBackPressed() {
        //thread_running = false;
        stopTimer();
        thread_running = false;
        Log.w(mtag, "Thread Stopped... ");
        Toast.makeText(this," Cancelled Booking ",Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }
}
