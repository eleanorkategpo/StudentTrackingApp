package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SchoolAdminActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private String school_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_admin);
        setupUIViews();

        getSchool();
    }

    private void setupUIViews() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void getSchool() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("user_id")
                .equalTo(firebaseAuth.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        User user = snap.getValue(User.class);
                        school_id = user.getSchoolId();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_school_admin,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.addSchool:
                intent = new Intent(SchoolAdminActivity.this, SchoolActivity.class);
                intent.putExtra("SCHOOL_STATUS", "add");
                intent.putExtra("SCHOOL_ID", "");
                startActivity(intent);
                return true;
            case R.id.editDetails:
                intent = new Intent(SchoolAdminActivity.this, SchoolActivity.class);
                intent.putExtra("SCHOOL_STATUS", "edit");
                intent.putExtra("SCHOOL_ID", school_id);
                startActivity(intent);
                return true;
            case R.id.addUsers:
                startActivity(new Intent(SchoolAdminActivity.this, RegisterActivity.class));
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

}
