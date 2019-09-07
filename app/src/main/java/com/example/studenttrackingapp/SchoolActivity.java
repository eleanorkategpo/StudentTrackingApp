package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class SchoolActivity extends AppCompatActivity {
    private String SCHOOL_STATUS, SCHOOL_ID, ADDRESS, LATIDUDE, LONGITUDE;
    private int USER_TYPE;
    private EditText ID, Name, Address, PhoneNumber, Latitude, Longitude;
    private String id, name, address, phonenumber, latitude, longitude;
    private Button AddEditAddress, Save;
    private DatabaseReference schoolsTable, usersTable;
    private ProgressDialog progressDialog;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school);

        SCHOOL_STATUS = getIntent().getStringExtra("SCHOOL_STATUS");
        SCHOOL_ID = getIntent().getStringExtra("SCHOOL_ID");

        setupUIViews();
        initLayout();
    }

    private void setupUIViews(){

        schoolsTable = FirebaseDatabase.getInstance().getReference("Schools");
        usersTable = FirebaseDatabase.getInstance().getReference("Users");

        AddEditAddress = (Button)findViewById(R.id.addEditAddress);
        Save = (Button)findViewById(R.id.saveBtn);
        ID = (EditText)findViewById(R.id.schoolID);
        Name = (EditText)findViewById(R.id.schoolName);
        PhoneNumber = (EditText)findViewById(R.id.schoolPhoneNumber);
        Address = (EditText)findViewById(R.id.schoolAddress);
        Latitude = (EditText)findViewById(R.id.schoolLatitude);
        Longitude = (EditText)findViewById(R.id.schoolLongitude);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ID.setEnabled(false);
        if (SCHOOL_STATUS.equals("add")){
            ID.setText(schoolsTable.push().getKey());
        } else { //edit
            getStrings();
            ID.setText(SCHOOL_ID);
        }

        getUser();
    }

    private void initLayout(){
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                int type = (SCHOOL_STATUS.equals("add")) ? 0 : 2;
                saveDetails(type);
            }
        });

        AddEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                saveDetails(1);
            }
        });
    }

    private void getStrings() {
        progressDialog.setMessage("Loading data..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Query query = FirebaseDatabase.getInstance().getReference("Schools").child(SCHOOL_ID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    School school = dataSnapshot.getValue(School.class);
                    Name.setText(school.getSchoolName());
                    PhoneNumber.setText(school.getSchoolPhone());

                    LATIDUDE = getIntent().getStringExtra("LATITUDE");
                    if (!LATIDUDE.equals("0")) {
                        Latitude.setText(LATIDUDE);
                    } else {
                        Latitude.setText(school.getSchoolLat());
                    }

                    LONGITUDE = getIntent().getStringExtra("LONGITUDE");
                    if (!LONGITUDE.equals("0")) {
                        Longitude.setText(LONGITUDE);
                    } else {
                        Longitude.setText(school.getSchoolLong());
                    }

                    ADDRESS = getIntent().getStringExtra("ADDRESS");
                    if (!ADDRESS.equals("0")) {
                        Address.setText(ADDRESS);
                    } else {
                        Address.setText(school.getSchoolAddress());
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUser() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    USER_TYPE = user.getUserType();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveDetails(int type) { //type = 0: save all, 1: add edit address, 2: edit
        id = ID.getText().toString().trim();
        name = Name.getText().toString().trim();
        address = Address.getText().toString().trim();
        phonenumber = PhoneNumber.getText().toString().trim();
        latitude = Latitude.getText().toString().trim();
        longitude = Longitude.getText().toString().trim();

        if (type == 0 && (name.isEmpty() || address.isEmpty() || phonenumber.isEmpty() || latitude.isEmpty() || longitude.isEmpty())) {
            Toast.makeText(SchoolActivity.this, "Please enter all school details.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
        else if (type == 1 && (name.isEmpty() || phonenumber.isEmpty())) {
            Toast.makeText(SchoolActivity.this, "Please enter name and phone number of school before proceeding.", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
        else {
            if (type == 0) { //all
                schoolsTable = FirebaseDatabase.getInstance().getReference("Schools");
                String id = schoolsTable.push().getKey();

                //String schoolName, String schoolAddress, String schoolPhone, String schoolLat, String schoolLong
                School school = new School(id, name, address, phonenumber, latitude, longitude);

                schoolsTable.child(id).setValue(school).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Query request = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                            request.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (SCHOOL_STATUS.equals("add")) {
                                            user.setSchoolId(id);
                                        } else {
                                            user.setSchoolId(SCHOOL_ID);
                                        }

                                        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                        dR.setValue(user);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Toast.makeText(SchoolActivity.this, "School " + name + " is successfully added!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SchoolActivity.this, SchoolAdminActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SchoolActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } else if (type == 1) { //type 1: add edit address

                progressDialog.setMessage("Loading maps...");
                progressDialog.show();
                Intent intent = new Intent(SchoolActivity.this, AddressActivity.class);
                intent.putExtra("SCHOOL_ID", id);
                startActivity(intent);
                progressDialog.dismiss();

            } else if (type == 2) { //edit existing
                Query school = FirebaseDatabase.getInstance().getReference("Schools").child(SCHOOL_ID);

                school.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            School sc = dataSnapshot.getValue(School.class);
                            sc.setSchoolName(name);
                            sc.setSchoolLong(latitude);
                            sc.setSchoolLong(longitude);
                            sc.setSchoolAddress(address);
                            sc.setSchoolPhone(phonenumber);

                            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Schools").child(SCHOOL_ID);
                            dR.setValue(sc);
                            SchoolActivity.super.onBackPressed();

                            Toast.makeText(SchoolActivity.this, "School details updated successfully.", Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SchoolActivity.super.onBackPressed();
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
