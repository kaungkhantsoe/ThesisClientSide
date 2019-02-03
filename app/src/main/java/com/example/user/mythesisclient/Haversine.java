package com.example.user.mythesisclient;

import android.util.Log;

public class Haversine {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public static double distance(double startLat, double startLong,
                                  double endLat, double endLong) {

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        Log.w("1 lat ",String.valueOf(Math.toRadians(startLat)));
        Log.w("1 lng",String.valueOf(Math.toRadians(startLong)));
        Log.w("2 lat ",String.valueOf(Math.toRadians(endLat)));
        Log.w("2 lng",String.valueOf(Math.toRadians(endLong)));

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);

        Log.w("equation ",String.valueOf(haversin(dLat)) +"+"+ String.valueOf(Math.cos(startLat)) +"*"+ String.valueOf(Math.cos(endLat)) +"*"+ String.valueOf(haversin(dLong)));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        Log.w("distance ===", String.valueOf(EARTH_RADIUS * c));
        return EARTH_RADIUS * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
