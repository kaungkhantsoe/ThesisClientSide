package com.example.user.mythesisclient;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by User on 4/26/2018.
 */

public class PlaceInfo {

    LatLng latLng;
    String name,address;

    public PlaceInfo(LatLng latLng, String name, String address) {
        this.latLng = latLng;
        this.name = name;
        this.address = address;
    }

    public LatLng getLatLng() {

        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
