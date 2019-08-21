package com.example.studenttrackingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentAdapterViewHolder>{

    private Context mCtx;
    private List<User> studentList;
    private OnRequestListener mOnRequestListener;


    public StudentAdapter(Context mCtx, List<User> studentList, OnRequestListener onRequestListener) {
        this.mCtx = mCtx;
        this.studentList = studentList;
        this.mOnRequestListener = onRequestListener;
    }

    @NonNull
    @Override
    public StudentAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.layout_students_recycler_view, null);
        return new StudentAdapterViewHolder(view, mOnRequestListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapterViewHolder holder, int position) {
        User student = studentList.get(position);
        holder.studentID.setText("Student ID: " +  student.getUserId());
        holder.studentName.setText(student.getName());
        holder.schoolName.setText(getSchoolName(student.getSchoolId()));
        holder.yearAndSection.setText(student.getYear() + " - " + student.getSection());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class StudentAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView studentID, studentName, schoolName, yearAndSection;
        OnRequestListener onRequestListener;

        public StudentAdapterViewHolder(@NonNull View itemView, OnRequestListener onRequestListener) {
            super(itemView);
            studentID = itemView.findViewById(R.id.studentID);
            studentName = itemView.findViewById(R.id.studentName);
            schoolName = itemView.findViewById(R.id.schoolName);
            yearAndSection = itemView.findViewById(R.id.yearAndSection);
            this.onRequestListener = onRequestListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onRequestListener.onRequestClick(getAdapterPosition());
        }
    }

    public interface OnRequestListener {
        void onRequestClick(int position);
    }

    private String getSchoolName(String id) {
        final String[] name = new String[1];
        Query user = FirebaseDatabase.getInstance().getReference("Schools").child(id);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    School school = dataSnapshot.getValue(School.class);
                    name[0] = school.getSchoolName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return name[0];
    }
}
