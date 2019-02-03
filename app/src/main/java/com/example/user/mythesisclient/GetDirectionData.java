package com.example.user.mythesisclient;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class GetDirectionData extends AsyncTask<Object,String,String> {

    private static String url;
    private static String googleDirectionData;
    private static String duration,distance;
    private static double price;
    private static final int INITIAL_PRICE = 1500;
    private static final int PRICE_PER_METER = 20;

    private static View view;
    private SimpleDateFormat simpleDateFormatHour = new SimpleDateFormat("hh");
    private SimpleDateFormat simpleDateFormatHourAMorPM = new SimpleDateFormat("a");
    private SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("EEE");
    private Date date;
    private int hour;
    private String ampm,day;

    private static final String mtag = GetDirectionData.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        date = new Date();
        hour = Integer.parseInt(simpleDateFormatHour.format(date));
        ampm = simpleDateFormatHourAMorPM.format(date);
        day = simpleDateFormatDay.format(date);

        Log.w(mtag, day + " " + hour + " " + ampm);

    }

    @Override
    protected String doInBackground(Object... objects) {

        url = (String) objects[0];
        view = (View) objects[1];
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googleDirectionData = downloadUrl.readUrl(url);
            Log.w(mtag, "Direction Data " + googleDirectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionData;
    }

    @Override
    protected void onPostExecute(String s) {

        TextView fees = view.findViewById(R.id.fees_txtview);

        HashMap<String,String> directionList = null;
        DataParser parser = new DataParser();
        directionList = parser.parseDirections(s);
        duration = directionList.get("duration");
        distance = directionList.get("distance");
        Log.w(mtag, "distance " + distance);

        // Split distance value from String
        String[] splitString = distance.split(" ");
        String measureString = splitString[1];
            double distanceString = Double.parseDouble(splitString[0]);

        switch (measureString) {
            case "mi":
                milesToMeters(distanceString);
                break;

            case "ft":
                feetToMeters(distanceString);
        }


        fees.setVisibility(View.VISIBLE);
        fees.setText(String.valueOf(transformPayablePrice(price)));
        //fees.setText(String.valueOf(transformPayablePrice(Math.toIntExact(Math.round(price))) + " Kyats "));
    }

    private void milesToMeters(double miles) {
        double distance_in_miles = miles;
        double distance_in_meters = distance_in_miles * 1609.344;

        // Calculate Price
        int TO_CHARGE = (int)distance_in_meters/100;
        Log.w("Meters to charge ", String.valueOf(TO_CHARGE));

        Log.w(mtag, " Price " + (INITIAL_PRICE + (TO_CHARGE * PRICE_PER_METER)));
        price =INITIAL_PRICE + (TO_CHARGE * PRICE_PER_METER);
    }

    private void feetToMeters(double feet) {
        double distance_in_feet = feet;
        double distance_in_meters = distance_in_feet/3.2808;

        // Calculate Price
        int TO_CHARGE = (int)distance_in_meters/100;
        Log.w("Feet to charge ", String.valueOf(TO_CHARGE));

        Log.w(mtag, " Price " + (INITIAL_PRICE + (TO_CHARGE * PRICE_PER_METER)));
        price = INITIAL_PRICE + (TO_CHARGE * PRICE_PER_METER);
    }

    // Changing Price into payable amount ( over 50 kyats will add 100 )
    private int transformPayablePrice(double dividend) {

       int quotient = (int)dividend/100;
       quotient *= 100;
       int reminder = (int)dividend%100;
       Log.w(mtag, "quotient = " +  String.valueOf(quotient));
       Log.w(mtag, "reminder = " + String.valueOf(reminder));

       if (reminder >= 50 ) {
           quotient +=100;
       }

        return quotient;
    }
}
