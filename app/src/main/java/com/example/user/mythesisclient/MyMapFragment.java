package com.example.user.mythesisclient;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by User on 4/18/2018.
 */

public class MyMapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static GoogleMap gmap;
    Context context;

    private static boolean isGPSEnabled = false;
    private static boolean isNetworkEnabled = false;
    private static boolean canGetLocation = false;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;//10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;//1 minute
    private static final float DEFAULT_ZOOM = 15f;

    private static double mlatitude = 0.0;
    private static double mlongitude = 0.0;
    private static LocationManager locationManager;
    private static Location previousLocation;
    private static Geocoder gc;

    private static PlaceInfo[] placeInfos = new PlaceInfo[2];
    private static Marker[] markers = new Marker[2];
    private static int width,height,padding;

    private static final String mtag = MyMapFragment.class.getSimpleName();
    private static UserSession userSession;

    GoogleApiClient mGoogleApiClient;

    List sortedDriverData;

    // Widget
    private static TextView from_edittxt, to_edittxt, arrival_time, fees;
    private Button bookButton;
    private static View MapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        gc = new Geocoder(context);
        userSession = new UserSession(context);

        mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        MapView = view;

        from_edittxt = view.findViewById(R.id.map_from_edittxt);
        from_edittxt.setSelected(true);
        from_edittxt.setOnClickListener(onSearchClickListener(R.id.map_from_edittxt));

        to_edittxt = view.findViewById(R.id.map_to_edittxt);
        to_edittxt.setSelected(true);
        to_edittxt.setOnClickListener(onSearchClickListener(R.id.map_to_edittxt));

        arrival_time = view.findViewById(R.id.arival_time_txtview);
        fees = view.findViewById(R.id.fees_txtview);
        fees.setVisibility(View.INVISIBLE);

        bookButton = view.findViewById(R.id.bookButton);
        bookButton.setOnClickListener(onBookClickListener());

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(mtag, "Map is ready");
        gmap = googleMap;

        calculatePadding();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.d(mtag, "Permissions granted");
            if (getLocation() != null) {

                Log.d(mtag, "Location not null");
                gmap.setMyLocationEnabled(true);
                gmap.getUiSettings().setMyLocationButtonEnabled(false);
                gmap.getUiSettings().setCompassEnabled(false);

                redrawMap(mlatitude, mlongitude);

            } else {
                Log.d(mtag, "Location is null");
                Snackbar.make(getView(), "Something went wrong !! Cannot get Location...\n Try turning on GPS...", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Click here", new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent gpsOptionsIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                                getActivity().finish();
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                try {
                                    Intent gpsOptionsIntent = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(gpsOptionsIntent);
                                    getActivity().finish();
                                }catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .show();
            }
        }

    }

    // Redraw Map for pick up point only
    public void redrawMap(double lat, double lon){

        mlatitude = lat;
        mlongitude = lon;
        LatLng mlatlong = new LatLng(lat, lon);

        gmap.clear();
        markers[0] = gmap.addMarker(new MarkerOptions()
                .position(mlatlong)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.from_marker)));
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(mlatlong, DEFAULT_ZOOM));

        List<Address> locationList = null;
        try {

            locationList = gc.getFromLocation(lat, lon, 1);

            if (locationList != null) {
                if (locationList.get(0).getSubLocality() != null) {
                    from_edittxt.setText(locationList.get(0).getFeatureName() + " , " + locationList.get(0).getSubLocality());
                } else {
                    from_edittxt.setText(locationList.get(0).getFeatureName());
                }
                Log.d(mtag, "Address.... " + locationList);


                placeInfos[0] = new PlaceInfo(mlatlong, from_edittxt.getText().toString(), locationList.get(0).getAddressLine(0));
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        // Set place likelihood
        guessCurrentPlace();

        // Set arrival time from nearest taxi
        setEstimateArrivalTime();

    }

    // Redraw Map for both points
    public void redrawMap2(LatLng latLng, String name, String address, int id) {


        if (id == R.id.map_from_edittxt) {

            placeInfos[0] = new PlaceInfo(latLng, name, address);
            from_edittxt.setText(name + " , " + address);

        } else if (id == R.id.map_to_edittxt) {

            placeInfos[1] = new PlaceInfo(latLng, name, address);
            to_edittxt.setText(name + " , " + address);

        }

        gmap.clear();

        for (int i = 0; i < 2; i++) {

            try {
                LatLng latLngForMap = placeInfos[i].getLatLng();

                if (i == 0) {

                    markers[0] = gmap.addMarker(new MarkerOptions()
                            .position(latLngForMap)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.from_marker)));
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngForMap, DEFAULT_ZOOM));


                } else {

                    markers[1] = gmap.addMarker(new MarkerOptions()
                            .position(latLngForMap)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.to_marker)));

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (Marker marker : markers) {

                        builder.include(marker.getPosition());

                    }

                    LatLngBounds bounds = builder.build();


                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,400);
                    gmap.moveCamera(cu);

                    // Calculate taxi fees between two points
                    calculateFees(placeInfos[0].getLatLng(),placeInfos[1].getLatLng());


                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        setEstimateArrivalTime();

    }

    // Location Section//////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    public Location getLocation() {
        try {
            if (canGetLocation()) {

                if (isNetworkEnabled) {

                    Log.d(mtag, "Network enabled");
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            previousLocation = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (previousLocation != null) {

                                mlatitude = previousLocation.getLatitude();
                                Log.d(mtag, "Network Latitude " + mlatitude);
                                mlongitude = previousLocation.getLongitude();
                                Log.d(mtag, "Network Longitude " + mlongitude);

                                try {
                                    //MyMapFragment.redrawMap(latitude,longitude);
                                    onLocationChanged(previousLocation);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }


                }

                if (isGPSEnabled) {

                    Log.d(mtag, "GPS enabled");
                    if (previousLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            previousLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (previousLocation != null) {
                                mlatitude = previousLocation.getLatitude();
                                Log.d(mtag, "GPS Latitude " + mlatitude);
                                mlongitude = previousLocation.getLongitude();
                                Log.d(mtag, "GPS Longitude " + mlongitude);

                                try {
                                    //MyMapFragment.redrawMap(latitude,longitude);
                                    onLocationChanged(previousLocation);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }else  {
                Log.d(mtag, "Location is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return previousLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

        if (isBetterLocation(location, previousLocation) && to_edittxt.getText().equals("")) {

            mlatitude = location.getLatitude();
            mlongitude = location.getLongitude();

            Log.w(mtag, "Location changed : " + location.getLatitude() + " , " + location.getLongitude());
            redrawMap(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public boolean canGetLocation() {

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d(mtag, "GpsEnabled " + isGPSEnabled);

        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d(mtag, "NetworkEnabled " + isNetworkEnabled);

        if (isGPSEnabled || isNetworkEnabled) {
            canGetLocation = true;
        }
        return this.canGetLocation;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private View.OnClickListener onSearchClickListener(final int id) {

        View.OnClickListener myListerner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MyLocationSearchActivity.class);
                i.putExtra("id", id);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity(i);
            }
        };
        return myListerner;
    }

    private View.OnClickListener onBookClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (arrival_time.getText().equals("Unavailable at the moment...")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(R.drawable.notaxi)
                            .setTitle("No Taxi Available")
                            .setMessage("Sorry, cannot call taxi at this moment. Try again later...")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                    Intent intent = new Intent(context,MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                }
                            }).show();


                }else {
                    if (from_edittxt.getText() != "" && to_edittxt.getText() != "") {

                        Intent intent = new Intent(context,WaitingDriverActivity.class);

                        // Sending pick up point data
                        intent.putExtra("name",placeInfos[0].getName());
                        intent.putExtra("addr",placeInfos[0].getAddress());
                        intent.putExtra("lat",placeInfos[0].getLatLng().latitude);
                        intent.putExtra("lng",placeInfos[0].getLatLng().longitude);

                        // Sending drop off point data
                        intent.putExtra("dname",placeInfos[1].getName());
                        intent.putExtra("daddr",placeInfos[1].getAddress());
                        intent.putExtra("dlat",placeInfos[1].getLatLng().latitude);
                        intent.putExtra("dlng",placeInfos[1].getLatLng().longitude);

                        intent.putExtra("price", fees.getText());

                        startActivity(intent);

                    } else if (from_edittxt.getText().equals("")) {
                        Toast.makeText(context, "Choose Pick Up point", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Choose Drop Off point", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        return listener;
    }

    public static double getMlongitude() {
        return mlongitude;
    }

    public static double getMlatitude() {
        return mlatitude;
    }

    public void guessCurrentPlace() {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {



            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback( new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult( PlaceLikelihoodBuffer likelyPlaces ) {

                    try {
                        PlaceLikelihood placeLikelihood = likelyPlaces.get( 0 );
                        String content = "";
                        if( placeLikelihood != null
                                && placeLikelihood.getPlace() != null
                                && !TextUtils.isEmpty( placeLikelihood.getPlace().getName() )
                                && placeLikelihood.getLikelihood() >= 0.5){

                            mlatitude = placeLikelihood.getPlace().getLatLng().latitude;
                            mlongitude = placeLikelihood.getPlace().getLatLng().longitude;
                            content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n" + "Percent change of being there: " + (int) ( placeLikelihood.getLikelihood() * 100 ) + "%";
                            Place place = placeLikelihood.getPlace();

                            from_edittxt.setText(place.getName());

                            placeInfos[0] = new PlaceInfo(place.getLatLng(), from_edittxt.getText().toString(), (String) place.getAddress());

                            gmap.clear();
                            markers[0] = gmap.addMarker(new MarkerOptions()
                                    .position(placeInfos[0].latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.from_marker)));

                            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeInfos[0].latLng, DEFAULT_ZOOM));

//                                //set arrival time from nearest taxi
//                                setEstimateArrivalTime();

                        }
                        Log.w(mtag,content);
                    }catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    //set arrival time from nearest taxi
                    setEstimateArrivalTime();

                    likelyPlaces.release();
                }
            });
        }

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void setEstimateArrivalTime() {

        Object object = new Object();
        object = placeInfos;
        GetDriverPositionData getDriverPositionData = (GetDriverPositionData) new GetDriverPositionData(new GetDriverPositionData.AsyncResponse() {
            @Override
            public void processFinish(List output) {
                sortedDriverData = output;

               if (sortedDriverData != null) {

                   // Add taxi icons to map
                   addNearestTaxiIcons(sortedDriverData);

                   // Calculate estimate arrival time and set text
                   DriverInfo nearestDriverInfo = (DriverInfo) sortedDriverData.get(0);

                   double nearestDistance = nearestDriverInfo.getDistance();
                   double avgSpeed = 30.0d;//30 Kilometer Per Hour

                   double speed_in_kilometers_per_minute = ( avgSpeed ) / 60;

                   // now calculate time in minutes
                   double time = nearestDistance / speed_in_kilometers_per_minute ;

                   if (Math.round(time) > 59){
                       int hr = (int) (Math.round(time) /60);
                       int min = (int) (Math.round(time) % 60);

                       arrival_time.setText(hr + " hr " + min + " min");
                   }else {
                       arrival_time.setText(Math.round(time) + " min");
                   }
               }else {
                   from_edittxt.setText("");
                   from_edittxt.setHint("Unavailable at the moment... Try again later....");
                   to_edittxt.setText("");
                   to_edittxt.setHint("Unavailable at the moment... Try again later....");
                   fees.setVisibility(View.INVISIBLE);

                   from_edittxt.setEnabled(false);
                   to_edittxt.setEnabled(false);

                   arrival_time.setText("Unavailable at the moment...");

               }

            }
        }).execute(object);

    }

    private void addNearestTaxiIcons(List sortedList) {

        if (sortedList.size() >= 10) {
            for (int i = 0 ; i < 10 ; i++ ) {

                DriverInfo driverInfo = (DriverInfo) sortedList.get(i);
                gmap.addMarker(new MarkerOptions()
                        .position(new LatLng(driverInfo.getDlat(), driverInfo.getDlng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_ic)));
            }
        }else {
            for (int i = 0 ; i < sortedList.size() ; i++ ) {

                DriverInfo driverInfo = (DriverInfo) sortedList.get(i);
                gmap.addMarker(new MarkerOptions()
                        .position(new LatLng(driverInfo.getDlat(), driverInfo.getDlng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_ic)));
            }
        }

    }

    private void calculateFees(LatLng start, LatLng end) {
        Object[] object = new Object[2];
        String url =  getDirectionUrl(start.latitude,start.longitude,end.latitude,end.longitude);
        object[0] = url;
        object[1] = MapView;
        GetDirectionData getDirectionData = new GetDirectionData();
        getDirectionData.execute(object);
    }

    private String getDirectionUrl(double sLat,double sLng,double eLat,double eLng) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin="+sLat+","+sLng);
        googleDirectionUrl.append("&destination="+eLat+","+eLng);
        googleDirectionUrl.append("&key="+"AIzaSyD1RDCtBm9xVixlEaKZ_UIZKIJvXlsP42c");

        return googleDirectionUrl.toString();
    }

    private void calculatePadding() {

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;
        final int minMetric = Math.min(width, height);
        padding = (int) (minMetric * 0.40); // offset from edges of the map in pixels

    }
}
