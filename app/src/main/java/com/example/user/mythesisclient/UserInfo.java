package com.example.user.mythesisclient;

/**
 * Created by User on 3/1/2018.
 */

public class UserInfo {

    int user_id;
    String Token,user_name,user_phone,user_password;

    public int getUser_id() {
        return user_id;
    }

    public String getToken() {
        return Token;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_password() {
        return user_password;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setToken(String token) {
        Token = token;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public void setUserInfo(int user_id, String Token, String user_name, String user_phone, String user_password){
        this.user_id = user_id;
        this.Token = Token;
        this.user_name = user_name;
        this.user_phone = user_phone;
        this.user_password = user_password;
    }

    public UserInfo getUserInfo(){
        return this;
    }
}
