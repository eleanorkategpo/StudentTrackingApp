package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class ParentActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private EditText myChild;
    private CheckBox inSchool, notSchool;
    private TextView currentAddress;
    private FloatingActionButton fab;
    private String CHILD_ID;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading maps..");
        progressDialog.show();

        setupUIViews();
        initLayout();
        getChild();
    }

    private void setupUIViews(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        myChild = (EditText) findViewById(R.id.myChild);
        myChild.setEnabled(false);
        inSchool = (CheckBox) findViewById(R.id.inSchool);
        inSchool.setClickable(false);
        notSchool = (CheckBox) findViewById(R.id.notSchool);
        notSchool.setClickable(false);
        currentAddress = (TextView) findViewById(R.id.childAddress);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initLayout() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChild();
            }
        });
    }

    private void getChild() {
        Query user = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());

        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User child = dataSnapshot.getValue(User.class);
                    CHILD_ID = child.getChildId();


                    //get location
                    Query childLoc = FirebaseDatabase.getInstance().getReference("StudentLocations").child(CHILD_ID);

                    childLoc.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                StudentLocation sloc = dataSnapshot.getValue(StudentLocation.class);

                                if (sloc.isInSchool()) {
                                    inSchool.setChecked(true);
                                    notSchool.setChecked(false);
                                } else {
                                    notSchool.setChecked(true);
                                    inSchool.setChecked(false);
                                }

                                getFormattedAddress(sloc.getLongitude(), sloc.getLatitude());

                                LatLng location = new LatLng(sloc.getLatitude(), sloc.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(location).title("Location of my child"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //end

                    //Get child details
                    Query parentChild = FirebaseDatabase.getInstance().getReference("Users").child(CHILD_ID);
                    parentChild.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User studentDetails = dataSnapshot.getValue(User.class);
                                myChild.setText(studentDetails.getLastName() + ", " + studentDetails.getFirstName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            {

                            }
                        }
                    });
                    //end
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFormattedAddress(double longitude, double latitude){
        String address = "";
        Geocoder geocoder = new Geocoder(ParentActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            // myCity = addresses.get(0).getLocality();
            currentAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_parent,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.editProfile:
                intent = new Intent(ParentActivity.this, EditProfile.class);
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
                                Toast.makeText(ParentActivity.this, "Logged out successfully...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ParentActivity.this, LoginActivity.class));
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
        progressDialog.dismiss();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout and stop monitoring on your child's location?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firebaseAuth.signOut();
                        Toast.makeText(ParentActivity.this, "Logged out successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ParentActivity.this, LoginActivity.class));
                        finish();
                        //ParentActivity.super.onBackPressed();
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
