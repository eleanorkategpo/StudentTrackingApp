package com.example.studenttrackingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminAdapterViewHolder>{ //RecyclerView.Adapter<StudentAdapter.StudentAdapterViewHolder>

    private Context mCtx;
    private List<User> adminList;
    private OnRequestListener mOnRequestListener2;


    public AdminAdapter(Context mCtx, List<User> adminList, OnRequestListener onRequestListener) {
        this.mCtx = mCtx;
        this.adminList = adminList;
        this.mOnRequestListener2 = onRequestListener;
    }

    @NonNull
    @Override
    public AdminAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.layout_admin_recycler_view, null);
        return new AdminAdapterViewHolder(view, mOnRequestListener2);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAdapterViewHolder holder, int position) {
        User admin = adminList.get(position);
        holder.adminID.setText("Student ID: " + admin.getUserId());
        holder.adminName.setText(admin.getLastName() + ", " + admin.getFirstName());
        holder.adminEmail.setText(admin.getEmail());
        holder.adminPhone.setText(admin.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    class AdminAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView adminID, adminName, adminEmail, adminPhone;
        OnRequestListener onRequestListener;

        public AdminAdapterViewHolder(@NonNull View itemView, OnRequestListener onRequestListener) {
            super(itemView);
            adminID = itemView.findViewById(R.id.adminID);
            adminName = itemView.findViewById(R.id.adminName);
            adminEmail = itemView.findViewById(R.id.adminEmail);
            adminPhone = itemView.findViewById(R.id.adminPhone);
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
}
