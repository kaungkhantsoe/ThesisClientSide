package com.example.user.mythesisclient;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetDriverData {


    UrlSetting myurlsetting = new UrlSetting();
    String myurl = myurlsetting.getMyurl();

    private static final String mtag = GetDriverData.class.getSimpleName();

    public String[] getDriverInfoFromDB (String driver_id) {
        String[] driverInfo = new String[4];

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", driver_id)
                    .build();
            Request request = new Request.Builder()
                    .url(myurl+"getDriverInfo.php")
                    .post(body)
                    .build();

            Response response = null;
            response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String jsonData = responseBody.string();

            try {
                Log.w(mtag,"Json data " + jsonData.toString());
                driverInfo = parseJsonData(jsonData);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            responseBody.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return driverInfo;
    }

    private String[] parseJsonData(String jsonData) throws JSONException {

        String[] result = new String[6];
        JSONObject jsonObject = new JSONObject(jsonData);

        result[0] = jsonObject.getString("name");
        Log.w(mtag, "Name " + result[0]);

        result[1] = jsonObject.getString("phnum");
        Log.w(mtag, "Ph " + result[1]);

        result[2] = jsonObject.getString("plate");
        Log.w(mtag, "Plate number " + result[2]);

        result[3] = jsonObject.getString("nic");
        Log.w(mtag,"nrc " + result[3]);

        result[4] = jsonObject.getString("title");
        Log.w(mtag,"title " + result[4]);

        result[5] = jsonObject.getString("img");
        Log.w(mtag,"img " + result[5]);


        return result;
    }
}
