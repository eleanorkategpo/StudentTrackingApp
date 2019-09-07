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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    private String USER_ID, SCHOOL_ID, YEAR, SECTION, CHILD_ID, NEW_CHILD_ID;

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

        School = (Spinner) findViewById(R.id.changeSchool);
        School.setEnabled(false);

        if (user_type == 2) {
            School.setVisibility(View.GONE);
        }

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

        getDetails();

        if (user_type == 2) {//parent
            Child = (Spinner)findViewById(R.id.myChild);
            Child.setEnabled(false);
        } else if (user_type == 3){ // student
            Year = (Spinner)findViewById(R.id.year);
            Year.setSelection(0);
            Year.setVisibility(View.VISIBLE);

            Section = (Spinner)findViewById(R.id.section);
            Section.setVisibility(View.VISIBLE);
        }
    }

    private void initLayout() {
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (!Password.getText().toString().equals("")) { //update password
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.updatePassword(Password.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditProfile.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EditProfile.this, "Something went wrong while updating the password.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
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

                                if (USER_TYPE == 3) { //student
                                    if (YEAR != null) {
                                        myProfile.setYear(YEAR);
                                    }
                                    if (SECTION != null) {
                                        myProfile.setSection(SECTION);
                                    }
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

    private boolean validate() {
        String fname = FName.getText().toString();
        String lname = LName.getText().toString();
        String birthday = Birthday.getText().toString().trim();
        String gender = (Female.isChecked()) ? "Female" : "Male";
        String email = Email.getText().toString();
        String password = Password.getText().toString();
        String address = Address.getText().toString();
        String phone_number = PhoneNumber.getText().toString();
        String email_regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

        if (email.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
            Toast.makeText(this, "Please input name and " +
                    "email of the user.", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() != 0 && password.length() < 6) {
            Toast.makeText(this, "Password should have 6 or more characters.", Toast.LENGTH_SHORT).show();
        }
        else if (!email.matches(email_regex)) {
            Toast.makeText(this, "Email address is not valid.", Toast.LENGTH_SHORT).show();
        }
        else {
            return true;
        }

        if (USER_TYPE == 2) { //parent
            if (NEW_CHILD_ID.isEmpty()) {
                Toast.makeText(this, "Please enter child.", Toast.LENGTH_SHORT).show();
            }
        }

        if (USER_TYPE == 3) { //student
            if (YEAR.isEmpty() || SECTION.isEmpty()) {
                Toast.makeText(this, "Year and section is required.", Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    private void getSchools(){
        progressDialog.setMessage("Loading data..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listOfSchools.clear();
        schoolList.clear();

        Query mySchool = FirebaseDatabase.getInstance().getReference("Schools").child(SCHOOL_ID);
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

    private void getChild(){
        progressDialog.setMessage("Loading all students from school..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query students = FirebaseDatabase.getInstance().getReference("Users").child(CHILD_ID);
        students.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User student = dataSnapshot.getValue(User.class);
                    if (!listOfStudents.contains(student.getLastName() + ", " + student.getFirstName() + " (" + student.getYear() + " - " + student.getSection() + ")")) {
                        childList.add(student);
                        listOfStudents.add(student.getLastName() + ", " + student.getFirstName() + " (" + student.getYear() + " - " + student.getSection() + ")");
                    }
                    setupSpinner("child");
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                }

                setupSpinner("year");
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

        for (int i = 0; i < listOfSection.size(); i++) {
            if (listOfSection.get(i).equals(SECTION)) {
                Section.setSelection(i);
            }
        }

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

                    SCHOOL_ID = user.getSchoolId();
                    CHILD_ID = user.getChildId();
                    YEAR = user.getYear();
                    SECTION = user.getSection();

                    FName.setText(user.getFirstName());
                    LName.setText(user.getLastName());
                    Birthday.setText(user.getBirthday());
                    Email.setText(user.getEmail());
                    Address.setText(user.getAddress());
                    PhoneNumber.setText(user.getPhoneNumber());
                    if (user.getGender().equals("Female")) {
                        Female.setChecked(true);
                    } else {
                        Male.setChecked(true);
                    }

                    if (USER_TYPE != 2) {
                        getSchools();
                    }

                    if (USER_TYPE == 2) {
                        getChild();
                    }

                    if (USER_TYPE == 3) {
                        getYears();
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

            for (int i = 0; i < listOfYear.size(); i++) {
                if (listOfYear.get(i).equals(YEAR)) {
                    Year.setSelection(i);
                }
            }

            Year.setVisibility(View.VISIBLE);
            Section.setVisibility(View.VISIBLE);
            Section.setEnabled(false);
        } else if (s.equals("child")) {
            adapterChild = new ArrayAdapter<String>(EditProfile.this, android.R.layout.simple_spinner_item, listOfStudents);
            adapterChild.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Child.setAdapter(adapterChild);
            Child.setOnItemSelectedListener(this);
            Child.setSelection(0);
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
                School school = schoolList.get(i);
                SCHOOL_ID = school.getSchoolId();
                break;
            case R.id.year:
                YEAR = listOfYear.get(i);
                getSections(YEAR);
                Section.setEnabled(true);
                break;
            case R.id.section:
                SECTION = listOfSection.get(i);
                break;
            case R.id.myChild:
                    User myChild = childList.get(i);
                    NEW_CHILD_ID = myChild.getUserId();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
