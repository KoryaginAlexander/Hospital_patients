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

    private int hospitalId = -1; 

    private ImageView imageViewBack;
    private TextView textViewTitle;

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


        Intent intent = getIntent();
        if (intent.hasExtra("hospital_id")) {
            hospitalId = intent.getIntExtra("hospital_id", -1);
            if (hospitalId != -1) {

                editTextHospitalName.setText(intent.getStringExtra("hospital_name"));
                editTextHospitalAddress.setText(intent.getStringExtra("hospital_address"));
                editTextHospitalPhone.setText(intent.getStringExtra("hospital_phone"));
                buttonSaveHospital.setText("Обновить");
                textViewTitle.setText("Изменить больницу"); 
            }
        } else {
            textViewTitle.setText("Добавить больницу");
        }

        buttonSaveHospital.setOnClickListener(v -> {
            saveHospital();
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void saveHospital() {
        String name = editTextHospitalName.getText().toString().trim();
        String address = editTextHospitalAddress.getText().toString().trim();
        String phone = editTextHospitalPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {

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


        Hospital hospitalToSave = new Hospital(hospitalId, name, address, phone);


        String authToken = "Bearer YOUR_JWT_TOKEN"; 

        Call<Hospital> call;
        if (hospitalId == -1) {

            call = apiService.createHospital(authToken, hospitalToSave);
        } else {
 
            call = apiService.updateHospital(authToken, hospitalId, hospitalToSave);
        }

        call.enqueue(new Callback<Hospital>() {
            @Override
            public void onResponse(Call<Hospital> call, Response<Hospital> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String mode = (hospitalId == -1) ? "created" : "updated";
                    Log.d("AddEditHospitalActivity", "Hospital " + mode + " successfully: " + response.body().getName());
                    finish(); 
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