package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class StudentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude, longitude;
    private User user;
    private String schoolId;

    private EditText MyLocation;
    private Button SendLocation;
    private GoogleMap mMap;

    private FirebaseAuth firebaseAuth;

    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        setupUIViews();
        initLayout();
        getStrings();
    }

    private void setupUIViews() {
        MyLocation = (EditText)findViewById(R.id.currentAddress);
        SendLocation = (Button)findViewById(R.id.sendLocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    private void initLayout() {
        SendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocation();
            }
        });

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                requestLocation();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }

    private void getStrings() {
        getUserDetails();
    }

    private void requestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);

                return;
            }
            else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null && locationListener != null) {
                    String formattedAddress = getFormattedAddress(location.getLongitude(), location.getLatitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    MyLocation.setText(formattedAddress);
                }
                else {
                    Toast.makeText(StudentActivity.this, "Something went wrong while getting location. Check your GPS and try again.", Toast.LENGTH_LONG);
                }
            }
        }
    }

    private String getFormattedAddress(double longitude, double latitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(StudentActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0);
            // myCity = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void sendLocation() { //send push notifications to parents and school admins
        //get parent
        //get admins with school id


        //continue here
    }

    private String getUserDetails() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("user_id")
                .equalTo(firebaseAuth.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        user = snap.getValue(User.class);
                        schoolId = user.getSchoolId();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_school_admin,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.editProfile:
                intent = new Intent(StudentActivity.this, EditProfile.class);
                startActivity(intent);
                return true;
            case R.id.logoutItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseAuth.signOut();
                                Toast.makeText(StudentActivity.this, "Logged out successfully...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(StudentActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
