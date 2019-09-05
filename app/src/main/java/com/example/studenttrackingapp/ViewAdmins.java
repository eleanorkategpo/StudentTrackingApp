package com.example.studenttrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
}
