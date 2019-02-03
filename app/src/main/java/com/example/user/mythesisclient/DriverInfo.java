package com.example.user.mythesisclient;

public class DriverInfo {

    int did;
    double dlat,dlng,distance;

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public double getDlat() {
        return dlat;
    }

    public void setDlat(double dlat) {
        this.dlat = dlat;
    }

    public double getDlng() {
        return dlng;
    }

    public void setDlng(double dlng) {
        this.dlng = dlng;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public DriverInfo(int did, double dlat, double dlng, double distance) {
        this.did = did;
        this.dlat = dlat;
        this.dlng = dlng;
        this.distance = distance;
    }
}
