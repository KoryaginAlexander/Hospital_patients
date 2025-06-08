package com.example.hospital_patients;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.view.View; // Import View
import android.widget.ImageView;
import android.widget.AdapterView;
import java.util.Collections;
import java.util.Comparator;

public class HospitalListActivity extends AppCompatActivity implements HospitalAdapter.OnHospitalActionListener {

    private RecyclerView recyclerViewHospitals;
    private HospitalAdapter hospitalAdapter;
    private ApiService apiService;
    private BottomNavigationView bottomNavigationView;
    private ImageView fabAddHospital; // Changed type from FloatingActionButton to ImageView
    private ImageView imageViewRefresh; 
    private Spinner spinnerSortBy; // Объявлена переменная для Spinner сортировки
    private TextView textViewTitle; // Объявлена переменная для TextView заголовка
    private List<Hospital> hospitalList; // Объявлена переменная для списка больниц

    // TODO: Use proper URL (from config/constants)
    private static final String BASE_URL = "http://212.192.31.136:5000"; // Use 10.0.2.2 for Android emulator localhost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);

        recyclerViewHospitals = findViewById(R.id.recyclerViewHospitals);
        recyclerViewHospitals.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_hospitals);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0); // Disable activity transition animation
                    return true;
                } else if (item.getItemId() == R.id.navigation_hospitals) {
                    // Already on this screen
                    return true;
                }
                return false;
            }
        });

        // Find Floating Action Button and set listener
        fabAddHospital = findViewById(R.id.fabAddHospital);
        fabAddHospital.setOnClickListener(v -> {
            // Implement add hospital logic (start new activity)
            Intent intent = new Intent(HospitalListActivity.this, AddEditHospitalActivity.class);
            startActivity(intent);
        });

        // Find refresh button and set listener
        imageViewRefresh = findViewById(R.id.imageViewRefresh);
        imageViewRefresh.setOnClickListener(v -> {
            fetchHospitals(); // Call fetchHospitals to refresh the list
        });

        // Initialize Spinner for sorting
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options_hospital, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(adapter);

        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = parentView.getItemAtPosition(position).toString();
                if (hospitalList != null) {
                    if (selectedOption.equals("По названию (А-Я)")) {
                        Collections.sort(hospitalList, Comparator.comparing(Hospital::getName));
                    } else if (selectedOption.equals("По названию (Я-А)")) {
                        Collections.sort(hospitalList, Collections.reverseOrder(Comparator.comparing(Hospital::getName)));
                    }
                    if (hospitalAdapter != null) {
                         hospitalAdapter.updateList(hospitalList);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        setupRetrofit();
        fetchHospitals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from AddEditHospitalActivity
        fetchHospitals();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void fetchHospitals() {
        // TODO: Get actual token after login
        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<List<Hospital>> call = apiService.getHospitals(authToken);
        call.enqueue(new Callback<List<Hospital>>() {
            @Override
            public void onResponse(Call<List<Hospital>> call, Response<List<Hospital>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hospitalList = response.body(); // Сохранить список больниц
                    // Pass 'this' as the listener to the adapter
                    hospitalAdapter = new HospitalAdapter(hospitalList, HospitalListActivity.this);
                    recyclerViewHospitals.setAdapter(hospitalAdapter);

                    // Sort the list initially based on the default spinner selection
                    String selectedOption = spinnerSortBy.getSelectedItem().toString();
                    if (selectedOption.equals("По названию (А-Я)")) {
                        Collections.sort(hospitalList, Comparator.comparing(Hospital::getName));
                    } else if (selectedOption.equals("По названию (Я-А)")) {
                        Collections.sort(hospitalList, Collections.reverseOrder(Comparator.comparing(Hospital::getName)));
                    }
                    hospitalAdapter.updateList(hospitalList);

                } else {
                    // Handle API errors (e.g., 401 Unauthorized, 404 Not Found)
                    String errorMessage = "Ошибка получения больниц: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                             errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("HospitalListActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("HospitalListActivity", "Error fetching hospitals: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Hospital>> call, Throwable t) {
                // Removed Toast message
                Log.e("HospitalListActivity", "Network error", t);
            }
        });
    }

    @Override
    public void onEditClick(Hospital hospital) {
        // Handle edit action: start AddEditHospitalActivity with hospital data
        Intent intent = new Intent(HospitalListActivity.this, AddEditHospitalActivity.class);
        intent.putExtra("hospital_id", hospital.getId());
        intent.putExtra("hospital_name", hospital.getName());
        intent.putExtra("hospital_address", hospital.getAddress());
        intent.putExtra("hospital_phone", hospital.getPhone());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Hospital hospital) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Удалить больницу")
                .setMessage("Вы уверены, что хотите удалить " + hospital.getName() + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // User confirmed, proceed with deletion
                    deleteHospital(hospital.getId());
                })
                .setNegativeButton("Нет", null) // Do nothing on cancel
                .show();
    }

    private void deleteHospital(int hospitalId) {
        String authToken = "Bearer YOUR_JWT_TOKEN"; // Using placeholder - Replace with actual token

        Call<Void> call = apiService.deleteHospital(authToken, hospitalId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("HospitalListActivity", "Hospital deleted successfully: " + hospitalId);
                    fetchHospitals(); // Refresh the list after deletion
                } else {
                    String errorMessage = "Error deleting hospital: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("HospitalListActivity", "Error parsing error body", e);
                        }
                    }
                    Log.e("HospitalListActivity", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("HospitalListActivity", "Network error deleting hospital", t);
            }
        });
    }

    // Optional: Override onBackPressed to control back button behavior if needed
    // @Override
    // public void onBackPressed() {
    //     // Do nothing or handle appropriately
    // }
} 