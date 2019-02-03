package com.example.user.mythesisclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.ArraySet;

import java.util.List;
import java.util.Set;

/**
 * Created by User on 2/26/2018.
 */

class UserSession {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared preferences mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    public static final String PREFER_NAME = "LoginActivity";

    // All Shared Preferences Keys
    public static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_PHONE = "Phone";

    // Email address (make variable public to access from outside)
    public static final String KEY_NAME = "Name";

    // password
    public static final String KEY_PASSWORD = "Password";

    public  static final String KEY_TOKEN = "Token";

    public static final String KEY_ID = "Id";


    public static final String ACCESS_FINE_LOCATION = "Fine";

    public static final String ACCESS_CORSE_LOCATION = "Corse";

    public static final String GPS_ACCESS ="Gps";

    public static final String  IS_DRIVER_ACCEPTED = "accp";

    public static final String  ACCEPTED_DRIVER_ID = "did";

    public static final String DRIVER_LIST = "deniedList";

    // Constructor
    public UserSession(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(int uid,String utoken, String uname, String uPhone, String uPassword){
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing phone in preferences
        editor.putString(KEY_PHONE, uPhone);

        // Storing password in preferences
        editor.putString(KEY_PASSWORD,  uPassword);

        editor.putInt(KEY_ID, uid);

        editor.putString(KEY_TOKEN, utoken);

        editor.putString(KEY_NAME, uname);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){

            // user is not logged in redirect him to LoginActivity Activity
            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Staring LoginActivity Activity
            _context.startActivity(i);

            return false;
        }
        return true;
    }


    public UserInfo getUserDetails() {

        UserInfo user = new UserInfo();

        user.setUser_id(pref.getInt(KEY_ID,0));
        user.setToken(pref.getString(KEY_TOKEN,""));
        user.setUser_name(pref.getString(KEY_NAME,""));
        user.setUser_phone(pref.getString(KEY_PHONE,""));
        user.setUser_password(pref.getString(KEY_PASSWORD,""));

        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // set login false
        editor.putBoolean(IS_USER_LOGIN,false);
        editor.commit();

        // After logout redirect user to MainActivity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring LoginActivity Activity
        _context.startActivity(i);


    }


    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }


    //access fine location getter setter
    public void createFineLocatinSession(boolean isFineEnabled){
        editor.putBoolean(ACCESS_FINE_LOCATION,isFineEnabled);
        editor.commit();
    }

    public boolean isFineLocationEnabled() {
        return pref.getBoolean(ACCESS_FINE_LOCATION,false);
    }

    //access corse location gettter setter
    public void createCorseLocatinSession(boolean isCorseEnabled){
        editor.putBoolean(ACCESS_FINE_LOCATION,isCorseEnabled);
        editor.commit();
    }

    public boolean isCorseLocationEnabled() {
        return pref.getBoolean(ACCESS_CORSE_LOCATION,false);
    }

//    //gps turned on getter setter
//    public void createGPSSession(boolean isGPSEnabled){
//        editor.putBoolean(ACCESS_FINE_LOCATION,isGPSEnabled);
//        editor.commit();
//    }
//
//    public boolean isGPSLocationEnabled() {
//        return pref.getBoolean(GPS_ACCESS,false);
//    }

    public void setIsAccepted(boolean isAccepted) {
        editor.putBoolean(IS_DRIVER_ACCEPTED,isAccepted);
        editor.commit();
    }

    public boolean isAccepted() {
        return pref.getBoolean(IS_DRIVER_ACCEPTED,false);
    }

    public void updateToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void setAcceptedDriverID(String did) {
        editor.putString(ACCEPTED_DRIVER_ID,did);
        editor.commit();
    }

    public String getAcceptedDriverID() {
        return pref.getString(ACCEPTED_DRIVER_ID,"");
    }

}
