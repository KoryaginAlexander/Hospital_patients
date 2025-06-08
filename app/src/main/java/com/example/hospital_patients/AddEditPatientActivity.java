package com.example.hospital_patients;

import android.app.DatePickerDialog;
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
import java.util.Calendar;

public class AddEditPatientActivity extends AppCompatActivity {

    private EditText editTextPatientName;
    private EditText editTextPatientSurname;
    private EditText editTextPatientPatronymic;
    private EditText editTextPatientAge;
    private EditText editTextPatientDiagnosis;
    private EditText editTextPatientAddress;
    private EditText editTextPatientPhone;
    private EditText editTextPatientAdmissionDate;
    private Button buttonSavePatient;

    private ApiService apiService;

    private int patientId = -1; // -1 indicates add mode, positive value indicates edit mode
    private int hospitalId = -1; // Mandatory for patient creation/update

    private ImageView imageViewBack;
    private TextView textViewTitle;

    // TODO: Use proper URL (from config/constants)
    private static final String BASE_URL = "http://10.0.2.2:5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_patient);

        // Initialize UI elements
        editTextPatientName = findViewById(R.id.editTextPatientName);
        editTextPatientSurname = findViewById(R.id.editTextPatientSurname);
        editTextPatientPatronymic = findViewById(R.id.editTextPatientPatronymic);
        editTextPatientAge = findViewById(R.id.editTextPatientAge);
        editTextPatientDiagnosis = findViewById(R.id.editTextPatientDiagnosis);
        editTextPatientAddress = findViewById(R.id.editTextPatientAddress);
        editTextPatientPhone = findViewById(R.id.editTextPatientPhone);
        editTextPatientAdmissionDate = findViewById(R.id.id_editTextPatientAdmissionDate);
        buttonSavePatient = findViewById(R.id.buttonSavePatient);

        imageViewBack = findViewById(R.id.imageViewBack);
        textViewTitle = findViewById(R.id.textViewTitle);

        imageViewBack.setOnClickListener(v -> finish());

        setupRetrofit();

        // Get hospital ID from Intent (mandatory)
        Intent intent = getIntent();
        if (intent.hasExtra("hospital_id")) {
            hospitalId = intent.getIntExtra("hospital_id", -1);
            if (hospitalId == -1) {
                Log.e("AddEditPatientActivity", "Error: Hospital ID not provided.");
                finish(); // Close activity if no valid hospitalId
                return;
            }
        } else {
            Log.e("AddEditPatientActivity", "Error: Hospital ID not provided. Cannot proceed.");
            finish();
            return;
        }

        // Check if we are in edit mode
        if (intent.hasExtra("patient_id")) {
            patientId = intent.getIntExtra("patient_id", -1);
            if (patientId != -1) {
                // Populate fields for editing
                editTextPatientName.setText(intent.getStringExtra("patient_name"));
                editTextPatientSurname.setText(intent.getStringExtra("patient_surname"));
                editTextPatientPatronymic.setText(intent.getStringExtra("patient_patronymic"));
                editTextPatientAge.setText(String.valueOf(intent.getIntExtra("patient_age", 0)));
                editTextPatientDiagnosis.setText(intent.getStringExtra("patient_diagnosis"));
                editTextPatientAddress.setText(intent.getStringExtra("patient_address"));
                editTextPatientPhone.setText(intent.getStringExtra("patient_phone"));
                editTextPatientAdmissionDate.setText(intent.getStringExtra("patient_admission_date"));
                buttonSavePatient.setText("Обновить"); // Change button text for edit mode
                textViewTitle.setText("Изменить пациента"); // Set title for edit mode
            }
        } else {
            textViewTitle.setText("Добавить пациента"); // Set title for add mode
        }

        buttonSavePatient.setOnClickListener(v -> {
            savePatient();
        });

        // Set up DatePickerDialog for admission date
        editTextPatientAdmissionDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Note: monthOfYear is 0-indexed
                    String formattedDate = String.format("%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    editTextPatientAdmissionDate.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void savePatient() {
        String name = editTextPatientName.getText().toString().trim();
        String surname = editTextPatientSurname.getText().toString().trim();
        String patronymic = editTextPatientPatronymic.getText().toString().trim();
        String ageString = editTextPatientAge.getText().toString().trim();
        String diagnosis = editTextPatientDiagnosis.getText().toString().trim();
        String address = editTextPatientAddress.getText().toString().trim();
        String phone = editTextPatientPhone.getText().toString().trim();
        String admissionDate = editTextPatientAdmissionDate.getText().toString().trim();

        // Basic validation
        if (name.isEmpty() || surname.isEmpty() || ageString.isEmpty() || diagnosis.isEmpty() || address.isEmpty() || phone.isEmpty() || admissionDate.isEmpty()) {
            if (name.isEmpty()) editTextPatientName.setError("Имя не может быть пустым");
            if (surname.isEmpty()) editTextPatientSurname.setError("Фамилия не может быть пустой");
            if (ageString.isEmpty()) editTextPatientAge.setError("Возраст не может быть пустым");
            if (diagnosis.isEmpty()) editTextPatientDiagnosis.setError("Диагноз не может быть пустым");
            if (address.isEmpty()) editTextPatientAddress.setError("Адрес не может быть пустым");
            if (phone.isEmpty()) editTextPatientPhone.setError("Телефон не может быть пустым");
            if (admissionDate.isEmpty()) editTextPatientAdmissionDate.setError("Дата поступления не может быть пустой");
            Log.e("AddEditPatientActivity", "Validation failed: All mandatory fields must be filled.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageString);
        } catch (NumberFormatException e) {
            editTextPatientAge.setError("Неверный формат возраста");
            Log.e("AddEditPatientActivity", "Invalid age format", e);
            return;
        }

        Patient patientToSave = new Patient();
        if (patientId != -1) {
            patientToSave.setId(patientId);
        }
        patientToSave.setName(name);
        patientToSave.setSurname(surname);
        patientToSave.setPatronymic(patronymic);
        patientToSave.setAge(age);
        patientToSave.setDiagnosis(diagnosis);
        patientToSave.setAddress(address);
        patientToSave.setPhone(phone);
        patientToSave.setAdmission_date(admissionDate);
        patientToSave.setHospitalId(hospitalId);

        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<Patient> call;
        if (patientId == -1) {
            // Add mode
            call = apiService.createPatient(authToken, patientToSave);
        } else {
            // Edit mode
            call = apiService.updatePatient(authToken, patientId, patientToSave);
        }

        call.enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String mode = (patientId == -1) ? "created" : "updated";
                    Log.d("AddEditPatientActivity", "Patient " + mode + " successfully: " + response.body().getName());
                    finish();
                } else {
                    String mode = (patientId == -1) ? "creating" : "updating";
                    String errorMessage = "Error " + mode + " patient: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("AddEditPatientActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("AddEditPatientActivity", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                String mode = (patientId == -1) ? "creating" : "updating";
                Log.e("AddEditPatientActivity", "Network error " + mode + " patient", t);
            }
        });
    }
} 