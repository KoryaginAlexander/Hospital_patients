package com.example.hospital_patients;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Import ImageView
import android.widget.ImageView;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<Hospital> hospitalList;
    private OnHospitalActionListener actionListener; // Declare the listener

    // Modify constructor to accept listener
    public HospitalAdapter(List<Hospital> hospitalList, OnHospitalActionListener actionListener) {
        this.hospitalList = hospitalList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.hospitalNameTextView.setText(hospital.getName());
        holder.hospitalAddressTextView.setText(hospital.getAddress());
        holder.hospitalPhoneTextView.setText(hospital.getPhone());

        holder.itemView.setOnClickListener(v -> {
            // Handle hospital item click
            Intent intent = new Intent(v.getContext(), PatientListActivity.class);
            intent.putExtra("hospital_id", hospital.getId());
            intent.putExtra("hospital_name", hospital.getName());
            v.getContext().startActivity(intent);
        });

        // Set click listener for edit button
        holder.imageViewEditHospital.setOnClickListener(v -> {
            // Use the listener to handle edit action
            if (actionListener != null) {
                actionListener.onEditClick(hospital);
            }
        });

        // Set click listener for delete button
        holder.imageViewDeleteHospital.setOnClickListener(v -> {
            // Use the listener to handle delete action
            if (actionListener != null) {
                actionListener.onDeleteClick(hospital);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    // Method to update the list of hospitals
    public void updateList(List<Hospital> newHospitalList) {
        this.hospitalList = newHospitalList;
        notifyDataSetChanged();
    }

    // Define the listener interface
    public interface OnHospitalActionListener {
        void onEditClick(Hospital hospital);
        void onDeleteClick(Hospital hospital);
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView hospitalNameTextView;
        TextView hospitalAddressTextView;
        TextView hospitalPhoneTextView;
        ImageView imageViewEditHospital; // Added ImageView for edit
        ImageView imageViewDeleteHospital; // Added ImageView for delete

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalNameTextView = itemView.findViewById(R.id.textViewHospitalName);
            hospitalAddressTextView = itemView.findViewById(R.id.textViewHospitalAddress);
            hospitalPhoneTextView = itemView.findViewById(R.id.textViewHospitalPhone);
            imageViewEditHospital = itemView.findViewById(R.id.imageViewEditHospital); // Find edit ImageView
            imageViewDeleteHospital = itemView.findViewById(R.id.imageViewDeleteHospital); // Find delete ImageView
        }
    }
} 