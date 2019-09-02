package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class StudentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean USER_LOGGED_IN = true;
    private double latitude, longitude;
    private User user;
    private String schoolId, USER_ID, isSchoolAdmin;
    private EditText MyLocation;
    private Button SendLocation;
    private CheckBox inSchool, notInSchool;
    private GoogleMap mMap;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ProgressDialog progressDialog;
    private Marker myMarker;
    private Circle radius;
    private boolean isInSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        setupUIViews();
        initLayout();
    }

    private void setupUIViews() {
        isInSchool = false;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading maps...");
        progressDialog.show();

        MyLocation = (EditText)findViewById(R.id.currentAddress);
        MyLocation.setEnabled(false);
        SendLocation = (Button)findViewById(R.id.sendLocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        inSchool = (CheckBox)findViewById(R.id.inSchool);
        inSchool.setClickable(false);
        notInSchool = (CheckBox)findViewById(R.id.notSchool);
        notInSchool.setClickable(false);

        //USER_ID = firebaseAuth.getUid();
        USER_ID = getIntent().getStringExtra("USER_ID");
        isSchoolAdmin = getIntent().getStringExtra("SCHOOL_ADMIN");

        if (isSchoolAdmin.equals("true")) {
            SendLocation.setVisibility(View.GONE);
        }
    }

    private void initLayout() {
        SendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocation(true);
            }
        });

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (USER_LOGGED_IN) {
                    requestLocation();
                }
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
                    MyLocation.setText(formattedAddress);

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    LatLng myLocation = new LatLng(latitude, longitude);
                    MarkerOptions myLocationMarker = new MarkerOptions().position(myLocation).title("My Current Location");

                    if (myMarker != null) {
                        myMarker.remove();
                    }

                    myMarker = mMap.addMarker(myLocationMarker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(19));

                    float[] distance = new float[2];

                    if (radius != null) {
                        Location.distanceBetween(myMarker.getPosition().latitude, myMarker.getPosition().longitude,
                                radius.getCenter().latitude, radius.getCenter().longitude, distance);

                        if( distance[0] > radius.getRadius()  ){
                            //Toast.makeText(getBaseContext(), "Not In School", Toast.LENGTH_SHORT).show();
                            notInSchool.setChecked(true);
                            inSchool.setChecked(false);

                            isInSchool = false;
                        } else {
                            //Toast.makeText(getBaseContext(), "In School", Toast.LENGTH_SHORT).show();
                            inSchool.setChecked(true);
                            notInSchool.setChecked(false);
                            isInSchool = true;
                        }
                    }

                    sendLocation(false);
                }
                else {
                    Toast.makeText(StudentActivity.this, "Something went wrong while getting location. Check your GPS and try again.", Toast.LENGTH_LONG);
                }
            }
        }
    }

    private void sendLocation(boolean isActual) { //true if send button was clicked
        if (isActual) {
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            final String[] emailList = {""};
            Query schoolAdmins = FirebaseDatabase.getInstance().getReference("Users")
                    .orderByChild("schoolId")
                    .equalTo(schoolId);

            schoolAdmins.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            User a = snap.getValue(User.class);
                            if (a.isSchoolAdmin && a.getEmail() != null) {
                                emailList[0] += a.getEmail() + ",";
                            }
                        }
                        Intent i = new Intent(StudentActivity.this, SendEmail.class );
                        i.putExtra("EMAIL_LIST", emailList[0]);
                        startActivity(i);
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Query parent = FirebaseDatabase.getInstance().getReference("Users")
                    .orderByChild("childId")
                    .equalTo(USER_ID);

            parent.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            User p = snap.getValue(User.class);
                            if (p.getEmail() != null) {
                                emailList[0] = p.getEmail() + ",";
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Query currentUser = FirebaseDatabase.getInstance().getReference("StudentLocations").child(USER_ID);
            currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        StudentLocation loc = dataSnapshot.getValue(StudentLocation.class);
                        loc.setLatitude(latitude);
                        loc.setLongitude(longitude);
                        loc.setInSchool(isInSchool);

                        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("StudentLocations").child(USER_ID);
                        dR.setValue(loc);
                    } else { //add new
                        StudentLocation sl = new StudentLocation(USER_ID, latitude, longitude, isInSchool);
                        FirebaseDatabase.getInstance().getReference("StudentLocations")
                                .child(USER_ID)
                                .setValue(sl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(StudentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getUserDetails() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    schoolId = user.getSchoolId();

                    Query userSchool = FirebaseDatabase.getInstance().getReference("Schools").child(schoolId);
                    userSchool.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                School userSchool = dataSnapshot.getValue(School.class);

                                LatLng myLocation = new LatLng(Double.parseDouble(userSchool.getSchoolLat()), Double.parseDouble(userSchool.getSchoolLong()));
                                MarkerOptions marker = new MarkerOptions().position(myLocation).title("My School");
                                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.school_marker));
                                mMap.addMarker(marker);
                                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                                radius = mMap.addCircle(new CircleOptions()
                                        .center(myLocation)
                                        .radius(80)
                                        .strokeColor(Color.parseColor("#227B1FA2"))
                                        .fillColor(Color.parseColor("#229C27B0")));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_student,menu);
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
                                USER_LOGGED_IN = false;
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
        requestLocation();
        getUserDetails();
        progressDialog.dismiss();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout and stop sending updates on your location?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firebaseAuth.signOut();
                        USER_LOGGED_IN = false;
                        StudentActivity.super.onBackPressed();
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
    }


}
