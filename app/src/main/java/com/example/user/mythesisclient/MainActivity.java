package com.example.user.mythesisclient;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    UserSession userSession;
    UserInfo user = new UserInfo();

    UrlSetting myurlsetting = new UrlSetting();
    String myurl = myurlsetting.getMyurl();

    private static boolean isCorseGained = false;
    private static boolean isFineGained = false;

    private static final String mtag = MainActivity.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static boolean thread_running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //create user session
        userSession = new UserSession(this);
        isCorseGained = userSession.isCorseLocationEnabled();
        isFineGained = userSession.isFineLocationEnabled();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //remove focus on edit text views
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (isOnline()){
            Log.w(mtag, "Device is online");

            if (isServiceOK()){
                Log.w(mtag,"Google Service Ok");

                if (userSession.checkLogin()){
                    Log.w(mtag,"User is logged in");

                    user = userSession.getUserDetails();

                    FirebaseMessaging.getInstance().subscribeToTopic("test");

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            while (thread_running){

                                String token = FirebaseInstanceId.getInstance().getToken();

                                if (token != null) {
                                    System.out.println("=================== Device Token is  "+token);

                                    int user_id = user.getUser_id();
                                    Log.w(mtag, String.valueOf(user_id));
                                    String user_token = user.getToken();

                                    Log.w(mtag, user_token);

                                    if (!token.equals(user_token)) {
                                        updateToken(user_id,token);
                                        userSession.updateToken(token);
                                    }
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


                    View header = navigationView.getHeaderView(0);


                    TextView nav_header_uname = header.findViewById(R.id.nav_header_uname);
                    TextView nav_header_uph = (TextView) header.findViewById(R.id.nav_header_uphone);

                    nav_header_uname.setText("Name: "+user.getUser_name());
                    nav_header_uph.setText("Phone: "+user.getUser_phone());

                    if (isFineGained || isCorseGained){

                        replaceFragment("map");
                        if (userSession.isAccepted()) {
                            Intent acceptedDriverInfoIntent = new Intent(this,AcceptedDriverInfoActivity.class);
                            acceptedDriverInfoIntent.putExtra("message",userSession.getAcceptedDriverID());
                            acceptedDriverInfoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
                            startActivity(acceptedDriverInfoIntent);
                        }

                    }
                }
            }
        }else {

            Intent i = new Intent(this, NoConnectionActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(String item){

        FragmentManager fragmentManager = getFragmentManager();
        switch (item){
            case "map":
                fragmentManager.beginTransaction().replace(R.id.content_main_frame, new MyMapFragment()).commit();
                break;

            case "history":
                fragmentManager.beginTransaction().replace(R.id.content_main_frame, new HistoryFragment()).commit();
                break;
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.logout:
                userSession.logoutUser();

                Toast.makeText(getApplicationContext(),
                        "Logged Out !",
                        Toast.LENGTH_LONG).show();
                break;

            case R.id.history:
                replaceFragment("history");
                break;

            default:
                replaceFragment("map");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isServiceOK(){

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS){

            Log.d(mtag,"isServiceOK: Google Play Services is working");
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(mtag,"isServiceOk: an error occured but can be fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else {
            Toast.makeText(this,"You can't make map requests",Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null ) {
            if (netInfo.isConnectedOrConnecting()) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .build();
                    Request request = new Request.Builder()
                            .url(myurl+"dummy.php")
                            .post(body)
                            .build();

                    client.newCall(request).execute();
                    Log.w(mtag,"connected to server");
                    return true;
                }catch (IOException e){
                    e.printStackTrace();
                    Log.w(mtag,"cannot connect to server");
                    return false;
                }
            }
        }

        return false;
    }

    private void updateToken(int id, String token) {


        Log.w(mtag,"Tokens are not identical, Updating..." );
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("uid", String.valueOf(id))
                .add("Token", token)
                .build();

        Request request = new Request.Builder()
                .url(myurl+"updateToken.php")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            responseBody.close();
        } catch (IOException e) {
            Log.w(mtag, "Database Error !!");
            e.printStackTrace();
        }
    }

}
