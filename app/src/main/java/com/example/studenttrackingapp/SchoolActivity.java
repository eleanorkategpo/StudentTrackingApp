package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SchoolActivity extends AppCompatActivity {
    private String SCHOOL_STATUS, SCHOOL_ID;
    private EditText ID, Name, Address, PhoneNumber, Latitude, Longitude;
    private String id, name, address, phonenumber, latitude, longitude;
    private Button AddEditAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school);

        getStrings();
        setupUIViews();
        initLayout();
        getSchoolDetails();

    }

    private void setupUIViews(){
        AddEditAddress = (Button)findViewById(R.id.addEditAddress);
        ID = (EditText)findViewById(R.id.schoolID);
        Name = (EditText)findViewById(R.id.schoolName);
        Address = (EditText)findViewById(R.id.schoolAddress);
        Latitude = (EditText)findViewById(R.id.schoolLatitude);
        Longitude = (EditText)findViewById(R.id.schoolLongitude);

        ID.setEnabled(false);
        Address.setEnabled(false);
        Latitude.setEnabled(false);
        Longitude.setEnabled(false);
    }

    private void initLayout(){
        AddEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDetails();
            }
        });
    }

    private void getStrings() {
        SCHOOL_STATUS = getIntent().getStringExtra("SCHOOL_STATUS");
        SCHOOL_ID = getIntent().getStringExtra("SCHOOL_ID");

        name = Name.getText().toString().trim();
        address = Address.getText().toString().trim();
        latitude = Latitude.getText().toString().trim();
        longitude = Longitude.getText().toString().trim();
    }

    private void getSchoolDetails() {
        if (SCHOOL_STATUS.equals("add")) {

        }
        else { //edit
            ID.setText(SCHOOL_ID);

            Query currentUser = FirebaseDatabase.getInstance().getReference("Schools")
                    .orderByChild("school_id")
                    .equalTo(SCHOOL_ID);

            currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            School s = snap.getValue(School.class);

                            Name.setText(s.getSchoolName());
                            Address.setText(s.getSchoolAddress());
                            PhoneNumber.setText(s.getSchoolPhone());
                            Latitude.setText(s.getSchoolLat());
                            Longitude.setText(s.getSchoolLong());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });
        }
    }

    private void saveDetails() {
        if (SCHOOL_STATUS.equals("add")) { //save new

        }
        else { //update


        }
    }


}
