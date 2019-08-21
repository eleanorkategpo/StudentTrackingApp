package com.example.studenttrackingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddParentFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private EditText Name, Birthday, Email, Password, Address, PhoneNumber, Child;
    private RadioButton Female, Male;
    private Button AddBtn;
    private Spinner School;
    private String name, gender, birthday, email, password, address, phone_number, SCHOOL_ID, child;

    private ProgressDialog progressDialog;
    private DatabaseReference userTable;
    private FirebaseAuth firebaseAuth;

    private ArrayList<School> schoolList = new ArrayList<>();
    private ArrayList<String> listOfSchools = new ArrayList<>();
    private ArrayList<String> listOfYear = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register_parent, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupUIViews();
        initLayout();
        SCHOOL_ID = getSchool();
    }

    private void setupUIViews(){
        AddBtn = (Button)getView().findViewById(R.id.addBtn);
        Name = (EditText)getView().findViewById(R.id.name);
        Female = (RadioButton)getView().findViewById(R.id.female);
        Male = (RadioButton)getView().findViewById(R.id.male);
        Birthday = (EditText)getView().findViewById(R.id.birthday);
        Email = (EditText)getView().findViewById(R.id.email);
        Password = (EditText)getView().findViewById(R.id.password);
        Password.setText("temporarypassword");
        Password.setEnabled(false);
        PhoneNumber = (EditText)getView().findViewById(R.id.phoneNumber);
        Address = (EditText)getView().findViewById(R.id.address);
        Child = (EditText)getView().findViewById(R.id.myChild);
        progressDialog = new ProgressDialog(this.getContext());

        School = (Spinner)getView().findViewById(R.id.changeSchool);
        School.setEnabled(false);
        listOfSchools.add("-- Select school --");
        School.setSelection(0);
    }

    private void initLayout(){
        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    addStudent();
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

    private void getStrings() {
        name = Name.getText().toString();
        birthday = Birthday.getText().toString().trim();
        gender = (Female.isChecked()) ? "Female" : "Male";
        email = Email.getText().toString();
        password = Password.getText().toString();
        address = Address.getText().toString();
        phone_number = PhoneNumber.getText().toString();
        SCHOOL_ID = "";
        child = Child.getText().toString();
    }

    private boolean validate() {
        getStrings();
        String email_regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

        if (email.isEmpty() || name.isEmpty()) {
            Toast.makeText(this.getContext(), "Please input name and " +
                    "email of the user.", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(this.getContext(), "Password should have 6 or more characters.", Toast.LENGTH_SHORT).show();
        }
        else if (!email.matches(email_regex)) {
            Toast.makeText(this.getContext(), "Email address is not valid.", Toast.LENGTH_SHORT).show();
        }
        else if (child.isEmpty()) {
            Toast.makeText(this.getContext(), "Please enter child.", Toast.LENGTH_SHORT).show();
        }
        /* else if (!checkValidDate(birthday,  "yyyy/MM/dd HH:mm:ss")) {
            Toast.makeText(this.getContext(), "Date of birth is not valid.", Toast.LENGTH_SHORT).show();
        }*/
        else {
            return true;
        }

        return false;
    }

    private void addStudent() {
        //name, gender, birthday, email, password=temporarypass, address, phoneNumber, year, section, childId, isActive=1, userType = 2;

        getStrings();
        progressDialog.setMessage("Creating user...");
        progressDialog.show();

        userTable = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser newUser = task.getResult().getUser();

                    User user = new User(newUser.getUid(), name, gender, birthday, email, address, phone_number, SCHOOL_ID, null, null, "", true, 2, false, false);

                    userTable.child(newUser.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                progressDialog.dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private String getSchool(){
        progressDialog.setMessage("Loading data..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String[] school_id = new String[1];

        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    school_id[0] = user.getSchoolId();

                    if (user.isSuperAdmin()){
                        populateSchools();
                        School.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return school_id[0];
    }

    private void populateSchools() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Schools")
                .orderByChild("schoolName");

        currentUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                School school = dataSnapshot.getValue(School.class);
                schoolList.add(school);
                listOfSchools.add(school.getSchoolName());

                if (dataSnapshot.getKey().equals(SCHOOL_ID)){
                    int size = schoolList.size();
                    School.setSelection(size);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listOfSchools);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        School.setAdapter(adapter);
        School.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i != 0) { // not --select type--
            School school = schoolList.get(i - 1);
            SCHOOL_ID = school.getSchoolId();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
