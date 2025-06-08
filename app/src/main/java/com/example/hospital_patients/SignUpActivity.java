package com.example.hospital_patients;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextRepeatPassword;
    private Button buttonSignUp, buttonGoToLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextRepeatPassword = findViewById(R.id.editTextRepeatPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000") // Замените на ваш адрес
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        buttonSignUp.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString();
            String repeatPassword = editTextRepeatPassword.getText().toString();

            if (!isValidUsername(username)) {
                editTextUsername.setError("Минимум 3 буквы, только латиница и цифры");
                return;
            }
            if (password.length() < 3) {
                editTextPassword.setError("Минимум 3 символа");
                return;
            }
            if (!password.equals(repeatPassword)) {
                editTextRepeatPassword.setError("Пароли не совпадают");
                return;
            }

            // Регистрация через API
            apiService.register(new UserRequest(username, password)).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Ошибка регистрации: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        });
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 3 && username.matches("[A-Za-z0-9_]+$");
    }
} 