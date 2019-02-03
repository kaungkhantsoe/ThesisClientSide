package com.example.user.mythesisclient;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MyLocationSearchByMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap gmap;

    private static Geocoder gc;

    private static final String mtag = MyLocationSearchByMapActivity.class.getSimpleName();

    private static double mlatitude,mlongitude;

    private static LatLng selectedPosition;

    private static int id;

    private static String address;

    private static ImageView back_ic;

    private static final float DEFAULT_ZOOM = 18f;

    int PLACE_PICKER_REQUEST = 1;


    //Widget
    private static TextView selected_search_place_txtview;
    private static Button search_place_confirm_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_by_map_layout);

        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()){
            id = bundle.getInt("id");
        }


        selected_search_place_txtview = findViewById(R.id.selected_search_place_txtview);
        search_place_confirm_btn = findViewById(R.id.search_place_confirm_btn);
        search_place_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                MyMapFragment myMapFragment = new MyMapFragment();
                myMapFragment.redrawMap2(new LatLng(mlatitude,mlongitude), String.valueOf(selected_search_place_txtview.getText()),address,id);

            }
        });

        back_ic = findViewById(R.id.back_ic);
        back_ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MyLocationSearchActivity.class);
                i.putExtra("id",id);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        gc = new Geocoder(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.search_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        Log.d(mtag,"Map is ready");
        gmap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            mlongitude = MyMapFragment.getMlongitude();
            mlatitude = MyMapFragment.getMlatitude();

            Log.w(mtag,mlatitude + " , " + mlongitude);

            if ( mlatitude != 0.0 && mlongitude != 0.0 ){

                Log.d(mtag,"Location not null");
                gmap.setMyLocationEnabled(true);
                gmap.getUiSettings().setMyLocationButtonEnabled(false);
                gmap.getUiSettings().setCompassEnabled(false);
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mlatitude,mlongitude),DEFAULT_ZOOM));
                redrawMap(mlatitude,mlongitude);

                gmap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        Log.w(mtag,"on Camera Move Started ");
                        search_place_confirm_btn.setBackground(getResources().getDrawable(R.drawable.rect_button_grey));
                        search_place_confirm_btn.setClickable(false);

                        Log.w(mtag,mlatitude + " , " + mlongitude);
                    }
                });

                gmap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        Log.w(mtag,"on Camera Move ");
                    }
                });

                gmap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                    @Override
                    public void onCameraMoveCanceled() {
                        Log.w(mtag,"on Camera Move Canceled ");
                    }
                });

                gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        Log.w(mtag,"on Camera Idle ");

                        selectedPosition = googleMap.getCameraPosition().target;
                        if (selectedPosition != null) {

                            mlatitude = selectedPosition.latitude;
                            mlongitude = selectedPosition.longitude;

                            Log.w(mtag,"Selected Lat,Lng " + mlatitude + " , " + mlongitude);
                            redrawMap(mlatitude,mlongitude);
                        }
                    }
                });



            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    private void redrawMap(double lat,double lon){

        Object object;
        String url =  getURL(lat,lon);
        object = url;
        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
        getNearbyPlaces.execute(object);

        // Set confirm button initial state not clickable
        search_place_confirm_btn.setBackground(getResources().getDrawable(R.drawable.rect_button_grey));
        search_place_confirm_btn.setClickable(false);


        // Search address with place picker
//        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//        try {
//            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }

        // Search address with geoCoding
//        List<Address> locationList = null;
//        try {
//            locationList = gc.getFromLocation(lat, lon, 1);
//
//            if (locationList != null) {
//                Log.w(mtag,locationList.get(0).getLocality()+" , "+locationList.get(0).getSubLocality()+ " , " +locationList.get(0).getThoroughfare()+" , "+locationList.get(0).getSubThoroughfare());
//
//                if (locationList.get(0).getLocality() != null
//                        && locationList.get(0).getAddressLine(0) != null
//                        && !locationList.get(0).getFeatureName().toString().equals("Unnamed Road")
//                        && locationList.get(0).getCountryCode().toString().equals("MM")) {
//
//                    if (locationList.get(0).getLocality().toString().equals("Yangon")
//                            || locationList.get(0).getLocality().toString().equals("ရန်ကုန်")) {
//
//
//                        if (locationList.get(0).getSubLocality() != null){
//                            selected_search_place_txtview.setText(locationList.get(0).getFeatureName()+" , "+locationList.get(0).getSubLocality());
//                        }else {
//                            selected_search_place_txtview.setText(locationList.get(0).getFeatureName());
//                        }
//
//                        address = locationList.get(0).getAddressLine(0);
//
//                        Log.d(mtag,"Address.... "+locationList);
//
//                        search_place_confirm_btn.setBackground(getResources().getDrawable(R.drawable.rect_button));
//                        search_place_confirm_btn.setClickable(true);
//
//                    }else {
//                        selected_search_place_txtview.setText(" Unavailable Location !");
//                    }
//                }else {
//                    selected_search_place_txtview.setText(" Unavailable Location !");
//                }
//            }else {
//                selected_search_place_txtview.setText(" Unavailable Location !");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private String getURL(double lat, double lng) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleDirectionUrl.append("location="+lat+","+lng);
        googleDirectionUrl.append("&radius=20");
        googleDirectionUrl.append("&key="+"AIzaSyD1RDCtBm9xVixlEaKZ_UIZKIJvXlsP42c");

        return googleDirectionUrl.toString();
    }


    public class GetNearbyPlaces extends AsyncTask<Object, String, String> {

        String googlePlacesData;
        String url;

        @Override
        protected String doInBackground(Object... objects) {
            url = (String) objects[0];

            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                googlePlacesData = downloadUrl.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String s) {
            List<HashMap<String,String>> nearbyPlaceList = null;
            DataParser parser = new DataParser();
            nearbyPlaceList = parser.parse(s);


            showNearByPlaces(nearbyPlaceList);
        }

        private void showNearByPlaces(List<HashMap<String,String>> nearbyPlaceList){

            for (int i = 0; i<nearbyPlaceList.size(); i++)
            {
                MarkerOptions markerOptions = new MarkerOptions();

                HashMap<String,String> googlePlace = nearbyPlaceList.get(i);

                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));

                if (!placeName.equals(vicinity)) {
                    Log.w(mtag , placeName + " , " + vicinity);

                    selected_search_place_txtview.setText(placeName);
                    search_place_confirm_btn.setBackground(getResources().getDrawable(R.drawable.rect_button));
                    search_place_confirm_btn.setClickable(true);
                    address = vicinity;
                    break;
                }else {
                    selected_search_place_txtview.setText("Unavailable Location..");
                    search_place_confirm_btn.setBackground(getResources().getDrawable(R.drawable.rect_button_grey));
                    search_place_confirm_btn.setClickable(false);
                }


            }
        }
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(this, data);
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
//                // Here, get the position of the place, mark it in map and moveCamera to that, couple lines of code.
//            }
//        }
//    }

}
