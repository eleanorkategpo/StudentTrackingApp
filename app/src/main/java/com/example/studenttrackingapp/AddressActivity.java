package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddressActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String SCHOOL_ID,  address, latitude, longitude;
    private EditText Latitude, Longitude, Address;
    private Button AddLocation;
    //private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private ProgressDialog progressDialog;
    private DatabaseReference schoolsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        setupUIViews();
        initLayout();
        getStrings();

    }

    private void setupUIViews() {
        Address = (EditText) findViewById(R.id.address);
        Latitude = (EditText) findViewById(R.id.schoolLatitude);
        Longitude = (EditText) findViewById(R.id.schoolLongitude);
        AddLocation = (Button) findViewById(R.id.addLocation);
        /*Address.setEnabled(false);
        Latitude.setEnabled(false);
        Longitude.setEnabled(false);*/

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.maps_api_key));
        }

        //placesClient = Places.createClient(this);

        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocompleteFragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        schoolsTable = FirebaseDatabase.getInstance().getReference("Schools");

    }
    private void initLayout(){

        AddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address = Address.getText().toString().trim();
                latitude = Latitude.getText().toString().trim();
                longitude = Longitude.getText().toString().trim();

                if (address.isEmpty()) {
                    Toast.makeText(AddressActivity.this, "Please input address.", Toast.LENGTH_SHORT).show();
                } else if (latitude.isEmpty() || longitude.isEmpty()){
                    Toast.makeText(AddressActivity.this, "Please input latitude and longitude.", Toast.LENGTH_SHORT).show();
                } else {
                    addLocation();
                }
            }
        });

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                progressDialog.setMessage("Loading...");
                progressDialog.show();
                LatLng latLng = place.getLatLng();
                Latitude.setText(String.valueOf(latLng.latitude));
                Longitude.setText(String.valueOf(latLng.longitude));
                Address.setText(place.getAddress());
                progressDialog.dismiss();
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


    }

    private void getStrings(){
        SCHOOL_ID = getIntent().getStringExtra("SCHOOL_ID");
    }


    /*private void addLocation(String address, String latitude, String longitude) {

        Query request =  FirebaseDatabase.getInstance().getReference("School").child(SCHOOL_ID);
        request.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    School school = snap.getValue(School.class);
                    school.setSchoolAddress(address);
                    school.setSchoolLat(latitude);
                    school.setSchoolLong(longitude);

                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("School").child(SCHOOL_ID);
                    dR.setValue(schoolsTable);

                    Toast.makeText(AddressActivity.this, "Address added..", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    Intent intent = new Intent(AddressActivity.this, SchoolActivity.class);
                    intent.putExtra("SCHOOL_ID", SCHOOL_ID);
                    intent.putExtra("SCHOOL_STATUS", "edit");
                    intent.putExtra("ADDRESS", address);
                    intent.putExtra("LATITUDE", latitude);
                    intent.putExtra("LONGITUDE", longitude);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void addLocation() {
        Intent intent = new Intent(AddressActivity.this, SchoolActivity.class);
        intent.putExtra("SCHOOL_ID", SCHOOL_ID);
        intent.putExtra("SCHOOL_STATUS", "edit");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
