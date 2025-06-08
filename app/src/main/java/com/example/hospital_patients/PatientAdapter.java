package com.example.hospital_patients;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patientList;
    private OnPatientActionListener actionListener;

    public PatientAdapter(List<Patient> patientList, OnPatientActionListener actionListener) {
        this.patientList = patientList;
        this.actionListener = actionListener;
    }

    public void updateList(List<Patient> newList) {
        this.patientList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        // Bind data to the new layout elements
        String fullName = String.format("%s %s %s",
                patient.getSurname(), patient.getName(), patient.getPatronymic());
        holder.patientFullNameTextView.setText(fullName);

        holder.patientAgeTextView.setText("Возраст: " + patient.getAge());
        holder.patientDiagnosisTextView.setText("Диагноз: " + patient.getDiagnosis());
        holder.patientAddressTextView.setText("Адрес: " + patient.getAddress());
        holder.patientPhoneTextView.setText("Телефон: " + patient.getPhone());
        holder.patientAdmissionDateTextView.setText("Дата поступления: " + patient.getAdmission_date());

        // Set click listener for edit button
        holder.imageViewEditPatient.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClick(patient);
            }
        });

        // Set click listener for delete button
        holder.imageViewDeletePatient.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public interface OnPatientActionListener {
        void onEditClick(Patient patient);
        void onDeleteClick(Patient patient);
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientFullNameTextView;
        TextView patientAgeTextView;
        TextView patientDiagnosisTextView;
        TextView patientAddressTextView;
        TextView patientPhoneTextView;
        TextView patientAdmissionDateTextView;
        ImageView imageViewEditPatient;
        ImageView imageViewDeletePatient;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientFullNameTextView = itemView.findViewById(R.id.textViewPatientFullName);
            patientAgeTextView = itemView.findViewById(R.id.textViewPatientAge);
            patientDiagnosisTextView = itemView.findViewById(R.id.textViewPatientDiagnosis);
            patientAddressTextView = itemView.findViewById(R.id.textViewPatientAddress);
            patientPhoneTextView = itemView.findViewById(R.id.textViewPatientPhone);
            patientAdmissionDateTextView = itemView.findViewById(R.id.textViewPatientAdmissionDate);
            imageViewEditPatient = itemView.findViewById(R.id.imageViewEditPatient);
            imageViewDeletePatient = itemView.findViewById(R.id.imageViewDeletePatient);
        }
    }
} 