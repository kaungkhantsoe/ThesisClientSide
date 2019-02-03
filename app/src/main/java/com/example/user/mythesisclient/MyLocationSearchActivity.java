package com.example.user.mythesisclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by User on 4/29/2018.
 */

public class MyLocationSearchActivity extends FragmentActivity  {

    private AutoCompleteTextView autoCompleteTextView;
    private LatLngBounds latLngBounds = new LatLngBounds( new LatLng(16.76728,96.05028),new LatLng(17.00328,96.33583));
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GeoDataClient geoDataClient;
    private final String mtag = MyLocationSearchActivity.class.getSimpleName();
    private static int id;

    //widgets
    private static ImageView search_by_map_imgview;
    private static TextView search_by_map_txtview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()){
            id = bundle.getInt("id");
        }

        search_by_map_imgview = findViewById(R.id.search_by_map_imgview);
        search_by_map_imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MyLocationSearchByMapActivity.class);
                i.putExtra("id",id);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

            }
        });
        search_by_map_txtview = findViewById(R.id.search_by_map_txtview);
        search_by_map_txtview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MyLocationSearchByMapActivity.class);
                i.putExtra("id",id);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        autoCompleteTextView = findViewById(R.id.mautoCompleteSearcBar);
        initAutoComplete();
    }

    private void initAutoComplete(){

        geoDataClient = Places.getGeoDataClient(this);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("MM")
                .build();

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, geoDataClient,latLngBounds, typeFilter);

        autoCompleteTextView.setAdapter(placeAutocompleteAdapter);

        // Register a listener that receives callbacks when a suggestion has been selected
        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    geoLocate();
                }
                return false;
            }
        });
    }

    private void geoLocate(){
        String searchString = autoCompleteTextView.getText().toString();
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(mtag, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = geoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

//            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
//                    Toast.LENGTH_SHORT).show();
//            Log.i(mtag, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);


                MyMapFragment myMapFragment = new MyMapFragment();

                myMapFragment.redrawMap2(place.getLatLng(),place.getName().toString(),place.getAddress().toString(),id);


                Log.d(mtag, String.valueOf(place.getLatLng()));
                Log.d(mtag, "Place details received: " + place.getName());

                places.release();
                finish();

            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(mtag, "Place query did not complete.", e);
                return;
            }
        }
    };

}
