package com.example.user.mythesisclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetDriverPositionData extends AsyncTask<Object,String,List> {

    private static PlaceInfo[] placeInfos = new PlaceInfo[2];

    private static final String mtag = GetDriverPositionData.class.getSimpleName();

    public interface  AsyncResponse {
        void processFinish(List output);
    }

    public AsyncResponse delegate = null;

    public GetDriverPositionData(AsyncResponse delegate) {
        this.delegate = delegate;
    }


    @Override
    protected List doInBackground(Object... objects) {

        placeInfos = (PlaceInfo[]) objects[0];

        List sortedData = null;

        UrlSetting myurlsetting = new UrlSetting();
        String myurl = myurlsetting.getMyurl();

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .build();

            Request request = new Request.Builder()
                    .url(myurl + "get_driver_positions.php")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String jsonData = responseBody.string();

            try {
                if (!jsonData.isEmpty()){

                    List parsedeData = parseJsonData(jsonData);
                    for (int i = 0 ; i < parsedeData.size() ; i++) {
                        DriverInfo driverInfo = (DriverInfo) parsedeData.get(i);
                        Log.w(mtag,"Unsorted Data" + String.valueOf(driverInfo.getDistance()));
                    }

                    // Sorting List
                    Collections.sort(parsedeData, new Comparator<DriverInfo>() {

                        @Override
                        public int compare(DriverInfo o1, DriverInfo o2) {
                            return Double.valueOf(o1.distance).compareTo(o2.distance);
                        }

                    });

                    sortedData = parsedeData;
                    for (int i = 0 ; i < sortedData.size() ; i++) {
                        DriverInfo driverInfo = (DriverInfo) sortedData.get(i);
                        Log.w(mtag,"Sorted Data"+ String.valueOf(driverInfo.getDistance()));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            responseBody.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return sortedData;
    }

    @Override
    protected void onPostExecute(List list) {
        delegate.processFinish(list);
    }

    private List parseJsonData(String jsonData) throws JSONException {

        JSONArray jsonArray = new JSONArray(jsonData);

        List<DriverInfo> list = new ArrayList<>();

        for (int i = 0 ; i < jsonArray.length() ; i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            //calculate distance between pick up point and driver position
            double driver_lat = jsonObject.getDouble("lat");
            double driver_lng = jsonObject.getDouble("lng");
            int driver_id = jsonObject.getInt("did");

            Log.w("Driver Lat ", String.valueOf(driver_lat));
            Log.w("Driver Lng ", String.valueOf(driver_lng));
            double distance = Haversine.distance(placeInfos[0].getLatLng().latitude,placeInfos[0].getLatLng().longitude,driver_lat,driver_lng);

            list.add(new DriverInfo(driver_id,driver_lat,driver_lng,distance));
        }

        return list;
    }
}
