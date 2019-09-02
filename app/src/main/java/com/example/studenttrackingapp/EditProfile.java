package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private int USER_TYPE;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String USER_ID, SCHOOL_ID, year, section, NEW_CHILD_ID;

    private Spinner School, Child;
    private EditText FName, LName, Birthday, Email, Password, Address, PhoneNumber;
    private RadioButton Male, Female;
    private Spinner Year, Section;
    private Button SaveBtn;
    private ProgressDialog progressDialog;

    private ArrayList<School> schoolList = new ArrayList<>();
    private ArrayList<User> childList  = new ArrayList<>();
    private ArrayList<YearSection> yearSections = new ArrayList<>();
    private ArrayList<String> listOfSchools = new ArrayList<>();
    private ArrayList<String> listOfStudents  = new ArrayList<>();
    private ArrayList<String> listOfYear = new ArrayList<>();
    private ArrayList<String> listOfSection = new ArrayList<>();
    private ArrayAdapter<String> adapter, adapterChild, adapterYear, adapterSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserType();
    }

    private void getUserType() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    USER_TYPE = user.getUserType();

                    if ( USER_TYPE == 1) { //admin
                        setContentView(R.layout.activity_register_admin);
                    }
                    else if (USER_TYPE == 2) { //parent
                        setContentView(R.layout.activity_register_parent);
                    }
                    else { //student
                        setContentView(R.layout.activity_register_student);
                    }
                    setupUIViews(USER_TYPE);
                    initLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupUIViews(int user_type) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        USER_ID = firebaseUser.getUid();

        progressDialog = new ProgressDialog(this);

        School = (Spinner)findViewById(R.id.changeSchool);
        School.setEnabled(false);
        if (!listOfSchools.contains("-- Select school --")) {
            listOfSchools.add("-- Select school --");
        }
        School.setSelection(0);
        SCHOOL_ID = getSchools();

        FName = (EditText)findViewById(R.id.fname);
        LName = (EditText)findViewById(R.id.lname);
        Female = (RadioButton)findViewById(R.id.female);
        Male = (RadioButton)findViewById(R.id.male);
        Birthday = (EditText)findViewById(R.id.birthday);
        Email = (EditText)findViewById(R.id.email);
        Email.setEnabled(false);
        Password = (EditText)findViewById(R.id.password);
        Password.setHint("Input to change password");
        Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Address = (EditText)findViewById(R.id.address);
        PhoneNumber = (EditText)findViewById(R.id.phoneNumber);

        SaveBtn = (Button)findViewById(R.id.addBtn);
        SaveBtn.setText("Save");

        if (user_type == 2) {//parent
            Child = (Spinner)findViewById(R.id.myChild);
            if (!listOfStudents.contains("--Select child--")) {
                listOfStudents.add("--Select child--");
            }
            Child.setSelection(0);
            getChild();

        } else if (user_type == 3){ // student
            Year = (Spinner)findViewById(R.id.year);
            if (!listOfYear.contains("-- Select year --")) {
                listOfYear.add("-- Select year --");
            }
            Year.setSelection(0);
            Year.setVisibility(View.INVISIBLE);

            Section = (Spinner)findViewById(R.id.section);
            if (!listOfSection.contains("-- Select section --")) {
                listOfSection.add("-- Select section --");
            }
            Section.setSelection(0);
            Section.setVisibility(View.INVISIBLE);

            getYears();
        }

        getDetails();
    }

    private void initLayout() {
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Password.getText() != null) { //update password
                    //continue here
                }

                Query profile = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);
                profile.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User myProfile = dataSnapshot.getValue(User.class);
                            myProfile.setFirstName(FName.getText().toString().trim());
                            myProfile.setLastName(LName.getText().toString().trim());
                            myProfile.setGender((Female.isChecked()) ? "Female" : "Male");
                            myProfile.setBirthday(Birthday.getText().toString().trim());
                            myProfile.setAddress(Address.getText().toString().trim());
                            myProfile.setPhoneNumber(PhoneNumber.getText().toString().trim());

                            if (USER_TYPE == 2) { //parent
                                myProfile.setChildId(NEW_CHILD_ID);
                            } else if (USER_TYPE == 3) { //student
                                myProfile.setYear(year);
                                myProfile.setSection(section);
                            }

                            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);
                            dR.setValue(myProfile);

                            Toast.makeText(EditProfile.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            EditProfile.super.onBackPressed();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        Female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Male.setChecked(false);
            }
        });

        Male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Female.setChecked(false);
            }
        });
    }

    private String getSchools(){
        progressDialog.setMessage("Loading data..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listOfSchools.clear();
        schoolList.clear();

        final String[] school_id = new String[1];
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    school_id[0] = user.getSchoolId();

                    Query mySchool = FirebaseDatabase.getInstance().getReference("Schools").child(user.getSchoolId());
                    mySchool.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot ds) {
                            if (ds.exists()) {
                                School sc = ds.getValue(School.class);
                                schoolList.add(sc);
                                listOfSchools.add(sc.getSchoolName());
                                setupSpinner("school");
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return school_id[0];
    }

    private void getChild(){
        progressDialog.setMessage("Loading all students from school..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query students = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("userType")
                .equalTo(3);
        students.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        User student = snap.getValue(User.class);
                        if (student.getSchoolId().equals(SCHOOL_ID) && !listOfStudents.contains(student.getLastName() + ", " + student.getFirstName() + " (" + student.getYear() + " - " + student.getSection() + ")")) {
                            childList.add(student);
                            listOfStudents.add(student.getLastName() + ", " + student.getFirstName() + " (" + student.getYear() + " - " + student.getSection() + ")");
                        }
                    }
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapterChild = new ArrayAdapter<String>(EditProfile.this, android.R.layout.simple_spinner_item, listOfStudents);
        adapterChild.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Child.setAdapter(adapterChild);
        Child.setOnItemSelectedListener(this);
    }

    private void getYears() {
        Query years = FirebaseDatabase.getInstance().getReference("YearSection")
                .orderByChild("schoolId")
                .equalTo(SCHOOL_ID);

        years.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        YearSection ys = snap.getValue(YearSection.class);

                        if (ys.getSectionId().isEmpty() && !listOfYear.contains(ys.getYearDesc())) {
                            listOfYear.add(ys.getYearDesc());
                        }

                        yearSections.add(ys);
                    }
                    setupSpinner("year");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSections(String id) {
        progressDialog.setMessage("Loading sections...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listOfSection.clear();

        for (YearSection a: yearSections){
            if (a.getYearDesc().equals(id) && !(a.getSectionId().isEmpty())) {
                listOfSection.add(a.getSectionDesc());
            }
        }

        adapterSection = new ArrayAdapter<String>(EditProfile.this, android.R.layout.simple_spinner_item, listOfSection);
        adapterSection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Section.setAdapter(adapterSection);
        Section.setOnItemSelectedListener(this);
        Section.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void getDetails() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    FName.setText(user.getFirstName());
                    LName.setText(user.getLastName());
                    Birthday.setText(user.getBirthday());
                    Email.setText(user.getEmail());
                    //password
                    Address.setText(user.getAddress());
                    PhoneNumber.setText(user.getPhoneNumber());
                    if (user.getGender().equals("Female")) {
                        Female.setChecked(true);
                    } else {
                        Male.setChecked(true);
                    }

                    progressDialog.dismiss();


                } else {
                    Toast.makeText(EditProfile.this, "User not found", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupSpinner(String s) {
        if (s.equals("school")) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOfSchools);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            School.setAdapter(adapter);
            School.setOnItemSelectedListener(this);
            School.setSelection(schoolList.size()-1);
        } else if (s.equals("year")) {
            adapterYear = new ArrayAdapter<String>(EditProfile.this, android.R.layout.simple_spinner_item, listOfYear);
            adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Year.setAdapter(adapterYear);
            Year.setOnItemSelectedListener(this);

            Year.setVisibility(View.VISIBLE);
            Section.setVisibility(View.VISIBLE);
            Section.setEnabled(false);
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditProfile.super.onBackPressed();
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int id = adapterView.getId();
        switch (id)
        {
            case R.id.changeSchool:
                if (i != 0) { // not --select type--
                    School school = schoolList.get(i - 1);
                    SCHOOL_ID = school.getSchoolId();
                } else {
                    SCHOOL_ID = "";
                }
                break;
            case R.id.year:
                if (i != 0) {
                    year = listOfYear.get(i);
                    getSections(year);
                    Section.setEnabled(true);
                } else {
                    year = "";
                }
                break;
            case R.id.section:
                    if (i != 0) {
                        section = listOfSection.get(i);
                    } else {
                    section = "";
                }
                break;
            case R.id.myChild:
                if (i != 0) {
                    User myChild = childList.get(i - 1);
                    NEW_CHILD_ID =  myChild.getUserId();
                } else {
                    NEW_CHILD_ID = "";
                }
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
