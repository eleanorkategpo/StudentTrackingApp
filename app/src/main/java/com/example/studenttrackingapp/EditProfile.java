package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditProfile extends AppCompatActivity {
    private int USER_TYPE;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    //changeSchool, name, male, female, birthday, email, password, address, phonenumber, addBtn
    //child
    //year, section
    private Spinner Spinner, Child;
    private EditText Name, Birthday, Email, Password, Address, PhoneNumber, Year, Section;
    private Button AddBtn;

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
                    setupUIViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupUIViews() {
        //changeSchool, name, male, female, birthday, email, password, address, phonenumber, addBtn
        //child
        //year, section

        Spinner = (Spinner)findViewById(R.id.changeSchool);
        Child = (Spinner)findViewById(R.id.myChild);

        Name = (EditText)findViewById(R.id.name);
        Birthday = (EditText)findViewById(R.id.birthday);
        Email = (EditText)findViewById(R.id.email);
        Password = (EditText)findViewById(R.id.password);
        Year = (EditText)findViewById(R.id.year);
        Section = (EditText)findViewById(R.id.section);
        AddBtn = (Button)findViewById(R.id.addBtn);
    }
}
