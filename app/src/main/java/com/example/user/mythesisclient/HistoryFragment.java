package com.example.user.mythesisclient;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private CustomArrayAdapter adapter;
    private Context context;

    // Get Url for Okhttp
    private static UrlSetting myurlsetting = new UrlSetting();
    private static String myurl = myurlsetting.getMyurl();

    private static final String mtag = HistoryFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.history_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(android.R.id.list);

        final List list = getTripInfo();
        final List listInAscendingorder = new ArrayList();
        for (int i = list.size()-1; i>=0 ; i--) {
            listInAscendingorder.add(list.get(i));
        }
        adapter=new CustomArrayAdapter(view.getContext(),listInAscendingorder);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TripInfo tripInfo = (TripInfo) listInAscendingorder.get(i);

                Intent intent = new Intent(context,TripInfoActivity.class);
                intent.putExtra("uid",tripInfo.getUid());
                intent.putExtra("did",tripInfo.getDid());
                intent.putExtra("fname",tripInfo.getFrom_name());
                intent.putExtra("fadd",tripInfo.getFrom_address());
                intent.putExtra("tname",tripInfo.getTo_name());
                intent.putExtra("tadd",tripInfo.getTo_address());
                intent.putExtra("fare",tripInfo.getFare());
                intent.putExtra("date",tripInfo.getTdate());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

    }

    private List getTripInfo() {
        UserSession userSession = new UserSession(context);
        UserInfo userInfo = userSession.getUserDetails();
        String id = String.valueOf(userInfo.getUser_id());

        List parsedeData = new ArrayList();

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id",id)
                    .build();

            Request request = new Request.Builder()
                    .url(myurl + "get_tripInfo_customer.php")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String jsonData = responseBody.string();

            try {
                if (!jsonData.isEmpty()){

                    parsedeData = parseJsonData(jsonData);
                    for (int i = 0 ; i < parsedeData.size() ; i++) {
                        TripInfo tripInfo = (TripInfo) parsedeData.get(i);
                        Log.w(mtag,"History Data" + String.valueOf(tripInfo.getTdate()));
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            responseBody.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsedeData;
    }

    private List parseJsonData(String jsonData) throws JSONException {

        JSONArray jsonArray = new JSONArray(jsonData);

        List<TripInfo> list = new ArrayList<>();

        for (int i = 0 ; i < jsonArray.length() ; i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int tid = jsonObject.getInt("tid");
            int uid = jsonObject.getInt("uid");
            int did = jsonObject.getInt("did");
            int fare = jsonObject.getInt("fare");
            String from_name = jsonObject.getString("fromname");
            String from_address = jsonObject.getString("fromadd");
            String to_name = jsonObject.getString("toname");
            String to_address = jsonObject.getString("toadd");
            String tdate = jsonObject.getString("tdate");

            list.add(new TripInfo(tid,uid,did,fare,from_name,from_address,to_name,to_address,tdate));
        }

        return list;
    }
}
