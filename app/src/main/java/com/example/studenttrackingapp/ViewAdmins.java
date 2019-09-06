package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAdmins extends AppCompatActivity implements AdminAdapter.OnRequestListener{
    private RecyclerView AdminList;
    private AdminAdapter adminAdapter;
    private TextView noData;
    private String USER_ID, SCHOOL_ID;
    private List<User> adminList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_admins);

        setupUIViews();
        getUser();
    }

    private void setupUIViews() {
        firebaseAuth = FirebaseAuth.getInstance();
        USER_ID = firebaseAuth.getUid();
        AdminList = (RecyclerView)findViewById(R.id.adminList);
        AdminList.setHasFixedSize(true);
        AdminList.setLayoutManager(new LinearLayoutManager(this));
        noData = (TextView)findViewById(R.id.noData);
    }

    private void getUser() {
        Query currentUser = FirebaseDatabase.getInstance().getReference("Users").child(USER_ID);

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    SCHOOL_ID = user.getSchoolId();
                    getAdmins();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupRV() {
        progressDialog = new ProgressDialog(this);
        adminAdapter = new AdminAdapter(this, adminList, this );
        AdminList.setAdapter(adminAdapter);
        adminAdapter.notifyDataSetChanged();

        if (adminList.size() == 0) {
            noData.setVisibility(View.VISIBLE);
        } else {

            noData.setVisibility(View.INVISIBLE);
        }
    }

    private void getAdmins() {

        adminAdapter = null;
        adminList.clear();

        Query currentUser = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("schoolId")
                .equalTo(SCHOOL_ID);

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        User user = snap.getValue(User.class);

                        if (user.getUserType() == 1 && !user.getEmail().equals("admin@studenttracking.com")) {
                            adminList.add(user);
                        }
                    }
                    if (adminList.size() > 0) {
                        setupRV();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestClick(int position) {

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
                    Toast.makeText(ViewAdmins.this, "Account is superadmin...", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(ViewAdmins.this, EditProfile.class);
                    startActivity(intent);
                }
                break;
            case R.id.viewAdmins:
                startActivity( new Intent(ViewAdmins.this, ViewAdmins.class ));
                break;
            case R.id.viewStudents:
                startActivity(new Intent(ViewAdmins.this, SchoolAdminActivity.class));
                break;
            case R.id.addEditSchool:
                progressDialog.setMessage("Checking existing school");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Intent intent = new Intent(ViewAdmins.this, SchoolActivity.class);

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
                Intent i = new Intent(ViewAdmins.this, RegisterActivity.class);
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
                                Toast.makeText(ViewAdmins.this, "Logged out successfully...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ViewAdmins.this, LoginActivity.class));
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
