package com.example.hospital_patients;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ImageView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.view.View;
import android.widget.AdapterView;
import java.util.Collections;
import java.util.Comparator;

public class PatientListActivity extends AppCompatActivity implements PatientAdapter.OnPatientActionListener {

    private RecyclerView recyclerViewPatients;
    private PatientAdapter patientAdapter;
    private ApiService apiService;
    private int hospitalId;
    private List<Patient> patientList;

    // New UI elements
    private ImageView imageViewBack;
    private TextView textViewTitle;
    private ImageView imageViewRefresh;
    private Spinner spinnerSortBy;
    private ImageView fabAddPatient;

    // TODO: Use proper URL (from config/constants)
    private static final String BASE_URL = "http://10.0.2.2:5000"; // Use 10.0.2.2 for Android emulator localhost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        // Get hospitalId and hospitalName from Intent
        if (getIntent().hasExtra("hospital_id")) {
            hospitalId = getIntent().getIntExtra("hospital_id", -1);
            // Get hospital name from Intent
            String hospitalName = getIntent().getStringExtra("hospital_name");

            textViewTitle = findViewById(R.id.textViewTitle);
            if (hospitalName != null && !hospitalName.isEmpty()) {
                textViewTitle.setText("Пациенты больницы " + hospitalName); // Set title with hospital name
            } else {
                textViewTitle.setText("Пациенты больницы"); // Default title if name is not available
            }

            if (hospitalId == -1) {
                finish(); // Close activity if no valid hospitalId
                return;
            }
        } else {
            finish(); // Close activity if no hospitalId
            return;
        }

        // Initialize UI elements
        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewRefresh = findViewById(R.id.imageViewRefresh);
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        fabAddPatient = (ImageView) findViewById(R.id.fabAddPatient);

        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(this));

        // Set up listeners for top bar icons
        imageViewBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity (HospitalListActivity)
        });

        imageViewRefresh.setOnClickListener(v -> {
            // Implement refresh logic
            fetchPatients(hospitalId); // Refresh the list
        });

        // Configure Spinner for sorting
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options_patient, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(adapter);

        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = parentView.getItemAtPosition(position).toString();
                if (patientList != null) {
                    if (selectedOption.equals("По фамилии")) {
                        Collections.sort(patientList, Comparator.comparing(Patient::getSurname));
                    } else if (selectedOption.equals("По дате поступления")) {
                        Collections.sort(patientList, Comparator.comparing(Patient::getAdmission_date));
                    }
                    if (patientAdapter != null) {
                         patientAdapter.updateList(patientList);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Set up listener for FAB
        fabAddPatient.setOnClickListener(v -> {
            // Implement add patient logic (start new activity)
            Intent intent = new Intent(PatientListActivity.this, AddEditPatientActivity.class);
            intent.putExtra("hospital_id", hospitalId); // Pass hospital ID for new patient
            startActivity(intent);
        });

        setupRetrofit();
        fetchPatients(hospitalId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from AddEditPatientActivity
        fetchPatients(hospitalId);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void fetchPatients(int hospitalId) {
        // TODO: Get actual token after login
        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<List<Patient>> call = apiService.getPatients(authToken, hospitalId);
        call.enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(Call<List<Patient>> call, Response<List<Patient>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patientList = response.body();
                    // Pass 'this' as the listener to the adapter
                    patientAdapter = new PatientAdapter(patientList, PatientListActivity.this);
                    recyclerViewPatients.setAdapter(patientAdapter);
                } else {
                    // Handle API errors (e.g., 401 Unauthorized, 404 Not Found)
                    String errorMessage = "Ошибка получения пациентов: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("PatientListActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("PatientListActivity", "Error fetching patients: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Patient>> call, Throwable t) {
                Log.e("PatientListActivity", "Network error", t);
            }
        });
    }

    @Override
    public void onEditClick(Patient patient) {
        // Handle edit action: start AddEditPatientActivity with patient data
        Intent intent = new Intent(PatientListActivity.this, AddEditPatientActivity.class);
        intent.putExtra("patient_id", patient.getId());
        intent.putExtra("hospital_id", patient.getHospitalId());
        intent.putExtra("patient_name", patient.getName());
        intent.putExtra("patient_surname", patient.getSurname());
        intent.putExtra("patient_patronymic", patient.getPatronymic());
        intent.putExtra("patient_age", patient.getAge());
        intent.putExtra("patient_diagnosis", patient.getDiagnosis());
        intent.putExtra("patient_address", patient.getAddress());
        intent.putExtra("patient_phone", patient.getPhone());
        intent.putExtra("patient_admission_date", patient.getAdmission_date());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Patient patient) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Удалить пациента")
                .setMessage("Вы уверены, что хотите удалить " + patient.getSurname() + " " + patient.getName() + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // User confirmed, proceed with deletion
                    deletePatient(patient.getId());
                })
                .setNegativeButton("Нет", null) // Do nothing on cancel
                .show();
    }

    private void deletePatient(int patientId) {
        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<Void> call = apiService.deletePatient(authToken, patientId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("PatientListActivity", "Patient deleted successfully: " + patientId);
                    fetchPatients(hospitalId); // Refresh the list after deletion
                } else {
                    String errorMessage = "Error deleting patient: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("PatientListActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("PatientListActivity", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PatientListActivity", "Network error deleting patient", t);
            }
        });
    }
}