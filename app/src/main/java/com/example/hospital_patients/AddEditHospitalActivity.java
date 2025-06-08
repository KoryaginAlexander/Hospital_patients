package com.example.hospital_patients;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddEditHospitalActivity extends AppCompatActivity {

    private EditText editTextHospitalName;
    private EditText editTextHospitalAddress;
    private EditText editTextHospitalPhone;
    private Button buttonSaveHospital;

    private ApiService apiService;

    private int hospitalId = -1; // -1 indicates add mode, positive value indicates edit mode

    private ImageView imageViewBack;
    private TextView textViewTitle;

    // TODO: Use proper URL (from config/constants)
    private static final String BASE_URL = "http://192.168.31.234:5000"; // Changed back to http from https

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_hospital);

        // Initialize UI elements
        editTextHospitalName = findViewById(R.id.editTextHospitalName);
        editTextHospitalAddress = findViewById(R.id.editTextHospitalAddress);
        editTextHospitalPhone = findViewById(R.id.editTextHospitalPhone);
        buttonSaveHospital = findViewById(R.id.buttonSaveHospital);

        imageViewBack = findViewById(R.id.imageViewBack);
        textViewTitle = findViewById(R.id.textViewTitle);

        imageViewBack.setOnClickListener(v -> finish());

        setupRetrofit();

        // Check if we are in edit mode
        Intent intent = getIntent();
        if (intent.hasExtra("hospital_id")) {
            hospitalId = intent.getIntExtra("hospital_id", -1);
            if (hospitalId != -1) {
                // Populate fields for editing
                editTextHospitalName.setText(intent.getStringExtra("hospital_name"));
                editTextHospitalAddress.setText(intent.getStringExtra("hospital_address"));
                editTextHospitalPhone.setText(intent.getStringExtra("hospital_phone"));
                buttonSaveHospital.setText("Обновить"); // Change button text for edit mode
                textViewTitle.setText("Изменить больницу"); // Set title for edit mode
            }
        } else {
            textViewTitle.setText("Добавить больницу"); // Set title for add mode
        }

        buttonSaveHospital.setOnClickListener(v -> {
            saveHospital();
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void saveHospital() {
        String name = editTextHospitalName.getText().toString().trim();
        String address = editTextHospitalAddress.getText().toString().trim();
        String phone = editTextHospitalPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            // Show a visual indicator to the user
            if (name.isEmpty()) {
                editTextHospitalName.setError("Название не может быть пустым");
            }
            if (address.isEmpty()) {
                editTextHospitalAddress.setError("Адрес не может быть пустым");
            }
            if (phone.isEmpty()) {
                editTextHospitalPhone.setError("Телефон не может быть пустым");
            }
            Log.e("AddEditHospitalActivity", "Validation failed: All fields must be filled.");
            return;
        }

        // Create a Hospital object (ID will be included for update, ignored for creation)
        Hospital hospitalToSave = new Hospital(hospitalId, name, address, phone);

        // TODO: Get actual token after login
        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<Hospital> call;
        if (hospitalId == -1) {
            // Add mode
            call = apiService.createHospital(authToken, hospitalToSave);
        } else {
            // Edit mode
            call = apiService.updateHospital(authToken, hospitalId, hospitalToSave);
        }

        call.enqueue(new Callback<Hospital>() {
            @Override
            public void onResponse(Call<Hospital> call, Response<Hospital> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String mode = (hospitalId == -1) ? "created" : "updated";
                    Log.d("AddEditHospitalActivity", "Hospital " + mode + " successfully: " + response.body().getName());
                    // TODO: Consider adding a result intent to indicate success and refresh the list
                    finish(); // Close the activity after successful operation
                } else {
                    String mode = (hospitalId == -1) ? "creating" : "updating";
                    String errorMessage = "Error " + mode + " hospital: " + response.code();
                     if (response.errorBody() != null) {
                        try {
                             errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("AddEditHospitalActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("AddEditHospitalActivity", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Hospital> call, Throwable t) {
                String mode = (hospitalId == -1) ? "creating" : "updating";
                Log.e("AddEditHospitalActivity", "Network error " + mode + " hospital", t);
            }
        });
    }
} 