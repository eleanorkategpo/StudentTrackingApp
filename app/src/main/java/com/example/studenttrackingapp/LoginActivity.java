package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText Username, Password;
    private Button Login;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        setContentView(R.layout.activity_login);
        setupUIViews();

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Username.getText().toString(), Password.getText().toString());
            }
        });
    }

    private void setupUIViews() {
        Username = (EditText)findViewById(R.id.emailET);
        Password = (EditText)findViewById(R.id.passwordET);
        Login = (Button)findViewById(R.id.loginBT);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        /*user = firebaseAuth.getCurrentUser();
        if (user != null) {
            finish();
            //add condition based on usertype
            startActivity(new Intent(LoginActivity.this, SchoolAdminActivity.class));
        }*/
    }

    private void validate(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT);
        }
        else {
            progressDialog.setMessage("Logging you in...");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        user = task.getResult().getUser();
                        getUserType(user.getUid());
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this,  e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                      progressDialog.dismiss();
                }
            });
        }
    }

    private void getUserType(String user_id) {
        //String user_id = user.getUid();
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(user_id);

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User loginUser = dataSnapshot.getValue(User.class);
                    if (loginUser.getUserType() == 1 ) { //admin
                        Intent intent = new Intent(LoginActivity.this, SchoolAdminActivity.class);
                        startActivity(intent);
                    } else if (loginUser.getUserType() == 2) { //parent
                        Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
                        intent.putExtra("USER_ID", user_id);
                        intent.putExtra("SCHOOL_ADMIN", "false");
                        startActivity(intent);
                    } else { //student
                        Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                        /*intent.putExtra("USER_ID", user_id);*/
                        intent.putExtra("SCHOOL_ADMIN", "false");
                        startActivity(intent);
                    }

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
