package com.example.studenttrackingapp;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddAdminFragment extends Fragment {
    private EditText Name, Birthday, Email, Password, Address, PhoneNumber;
    private RadioButton Female, Male;
    private Button AddBtn;
    private String name, gender, birthday, email, password, address, phone_number, school_id;

    private ProgressDialog progressDialog;
    private DatabaseReference userTable;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register_admin, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupUIViews();
        initLayout();
    }
    private void setupUIViews() {
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

        progressDialog = new ProgressDialog(this.getContext());
    }

    private void initLayout() {
        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    addAdmin();
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
        school_id = "";
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
        /* else if (!checkValidDate(birthday,  "yyyy/MM/dd HH:mm:ss")) {
            Toast.makeText(this.getContext(), "Date of birth is not valid.", Toast.LENGTH_SHORT).show();
        }*/
        else {
            return true;
        }

        return false;
    }


    private void addAdmin() {
        //name, gender, birthday, email, password=temporarypass, address, phoneNumber, isActive=1, userType = 1;

        progressDialog.setMessage("Creating user...");
        progressDialog.show();

        userTable = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        String id = userTable.push().getKey();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser newUser = task.getResult().getUser();

                    User user = new User(newUser.getUid(), name, gender, birthday, email, "temporarypassword", address, phone_number, school_id, 1, 1);

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
}