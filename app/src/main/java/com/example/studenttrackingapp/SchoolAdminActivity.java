package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchoolAdminActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, StudentAdapter.OnRequestListener {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;
    private RecyclerView StudentList;
    private Spinner allSchools;
    private TextView noData;

    private String SCHOOL_ID;
    private int USER_TYPE;
    private ArrayList<User> studentList = new ArrayList<>();
    private ArrayList<School> schoolList = new ArrayList<>();
    private ArrayList<String> listOfSchools = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_admin);
        setupUIViews();
        initLayout();
        getSchoolOfUser();
    }

    private void setupUIViews() {
        noData = (TextView)findViewById(R.id.noData);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(this);

        allSchools = (Spinner)findViewById(R.id.changeSchool);
        allSchools.setEnabled(false);

        if (!listOfSchools.contains("-- Select school --")) {
            listOfSchools.add("-- Select school --");
            schoolList.add(null);
        }

        StudentList = (RecyclerView)findViewById(R.id.studentList);
        StudentList.setHasFixedSize(true);
        StudentList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initLayout() {
    }

    private void getSchoolOfUser() {
        progressDialog.setMessage("Loading data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    SCHOOL_ID = user.getSchoolId();
                    USER_TYPE = user.getUserType();

                    getAllSchools();

                    if (user.isSuperAdmin()){
                        allSchools.setEnabled(true);
                    }
                } else {
                    Toast.makeText(SchoolAdminActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SchoolAdminActivity.this, LoginActivity.class));
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getAllSchools() {
        final int[] size = new int[1];
        Query currentSchool = FirebaseDatabase.getInstance().getReference("Schools")
                .orderByChild("schoolName");

        currentSchool.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        School school = snap.getValue(School.class);
                        if (!listOfSchools.contains(school.getSchoolName())) {
                            schoolList.add(school);
                            listOfSchools.add(school.getSchoolName());
                        }
                        if (snap.getKey().equals(SCHOOL_ID)){
                            size[0] = schoolList.size();
                            //allSchools.setSelection(size[0]);
                        }
                    }
                    setupSpinner(size[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllStudentsBySchool(){
        //get students by school
        progressDialog.setMessage("Your data is loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        studentAdapter = null;
        studentList.clear();

        Query currentUser = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("schoolId")
                .equalTo(SCHOOL_ID);

        currentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        User user = snap.getValue(User.class);
                        if (user.getUserType() == 3 && !studentList.contains(user)) {
                            studentList.add(user);
                        }
                    }
                    setupRV();
                } else {
                    noData.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupRV() {
        studentAdapter = new StudentAdapter(this, studentList, this );
        StudentList.setAdapter(studentAdapter);
        studentAdapter.notifyDataSetChanged();

        if (studentList.size() == 0) {
            noData.setVisibility(View.VISIBLE);
        } else {
            noData.setVisibility(View.GONE);
        }

    }

    private void setupSpinner(int size) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOfSchools);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allSchools.setAdapter(adapter);
        allSchools.setOnItemSelectedListener(this);
        allSchools.setSelection(size);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_school_admin,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editProfile:
                if (firebaseUser.getEmail().equals("admin@studenttracking.com")) {
                    Toast.makeText(SchoolAdminActivity.this, "Account is superadmin...", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SchoolAdminActivity.this, EditProfile.class);
                    startActivity(intent);
                }
                break;
            case R.id.viewAdmins:
                startActivity( new Intent(SchoolAdminActivity.this, ViewAdmins.class ));
                break;
            case R.id.viewStudents:
                startActivity(new Intent(SchoolAdminActivity.this, SchoolAdminActivity.class));
                break;
            case R.id.addEditSchool:
                progressDialog.setMessage("Checking existing school");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Intent intent = new Intent(SchoolAdminActivity.this, SchoolActivity.class);

                if (SCHOOL_ID.isEmpty()) {
                    intent.putExtra("SCHOOL_STATUS", "add");
                    intent.putExtra("SCHOOL_ID", "0");
                } else {
                    intent.putExtra("SCHOOL_STATUS", "edit");
                    intent.putExtra("SCHOOL_ID", SCHOOL_ID);
                }

                intent.putExtra("ADDRESS", "0");
                intent.putExtra("LATITUDE", "0");
                intent.putExtra("LONGITUDE", "0");
                startActivity(intent);
                progressDialog.dismiss();

                return true;
            case R.id.addUsers:
                Intent i = new Intent(SchoolAdminActivity.this, RegisterActivity.class);
                startActivity(i);
                return true;
            case R.id.logoutItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseAuth.signOut();
                                Toast.makeText(SchoolAdminActivity.this, "Logged out successfully...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SchoolAdminActivity.this, LoginActivity.class));
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        /*if(i >= 0 && schoolList.size() > 0) {
            if (i == 0 && listOfSchools.get(0).equals("-- Select school --")) {
               studentList.clear();
               SCHOOL_ID = "";
            }

            School school = schoolList.get(i);
            SCHOOL_ID = school.getSchoolId();
            getAllStudentsBySchool();
        } else {
            studentList.clear();
            SCHOOL_ID = "";
            noData.setVisibility(View.VISIBLE);
        }*/
        if (i > 0){
            if (schoolList.size() > 0) {
                School school = schoolList.get(i);
                SCHOOL_ID = school.getSchoolId();
                getAllStudentsBySchool();
            }
        } else {
            studentList.clear();
            SCHOOL_ID = "";
            noData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onRequestClick(int position) {
       /*FirstAidRequest request = requestList.get(position);
        Intent intent = new Intent(RecipientRequests.this, RecipientSuccessActivity.class);
        intent.putExtra("REQUEST_ID", request.getId());
        startActivity(intent);*/
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        User u = studentList.get(position);
        Intent intent = new Intent(SchoolAdminActivity.this, ParentActivity.class);
        intent.putExtra("USER_ID", u.getUserId());
        intent.putExtra("SCHOOL_ADMIN", "true");
        startActivity(intent);
        progressDialog.dismiss();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firebaseAuth.signOut();
                        SchoolAdminActivity.super.onBackPressed();
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
