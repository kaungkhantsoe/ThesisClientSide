package com.example.user.mythesisclient;

public class TripInfo {

    int tid,uid,did,fare;
    String from_name,from_address,to_name,to_address,tdate;

    public int getTid() {

        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getTdate() {
        return tdate;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public TripInfo getTripInfo() {
        return this;
    }
    public TripInfo(int tid, int uid, int did, int fare, String from_name, String from_address, String to_name, String to_address, String tdate) {
        this.tid = tid;
        this.uid = uid;
        this.did = did;
        this.fare = fare;
        this.from_name = from_name;
        this.from_address = from_address;
        this.to_name = to_name;
        this.to_address = to_address;
        this.tdate = tdate;
    }
}
