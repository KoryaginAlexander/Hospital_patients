package com.example.hospital_patients;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

// Import necessary Retrofit annotations for POST, PUT, DELETE, Body, Path
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface ApiService {

    // TODO: Implement proper token handling (login/registration)
    String AUTH_TOKEN = "Bearer YOUR_JWT_TOKEN"; // Placeholder - Replace with actual token

    @GET("/hospitals")
    Call<List<Hospital>> getHospitals(@Header("Authorization") String token);

    @GET("/patients")
    Call<List<Patient>> getPatients(
            @Header("Authorization") String token,
            @Query("hospital_id") int hospitalId
    );

    // Endpoints for Hospitals
    @POST("/hospitals")
    Call<Hospital> createHospital(
            @Header("Authorization") String token,
            @Body Hospital hospital
    );

    @PUT("/hospitals/{hospital_id}")
    Call<Hospital> updateHospital(
            @Header("Authorization") String token,
            @Path("hospital_id") int hospitalId,
            @Body Hospital hospital
    );

    @DELETE("/hospitals/{hospital_id}")
    Call<Void> deleteHospital(
            @Header("Authorization") String token,
            @Path("hospital_id") int hospitalId
    );

    // Endpoints for Patients
    @POST("/patients")
    Call<Patient> createPatient(
            @Header("Authorization") String token,
            @Body Patient patient
    );

    @PUT("/patients/{patient_id}")
    Call<Patient> updatePatient(
            @Header("Authorization") String token,
            @Path("patient_id") int patientId,
            @Body Patient patient
    );

    @DELETE("/patients/{patient_id}")
    Call<Void> deletePatient(
            @Header("Authorization") String token,
            @Path("patient_id") int patientId
    );

    @POST("/register")
    Call<Void> register(@Body UserRequest user);

    @FormUrlEncoded
    @POST("/login")
    Call<LoginResponse> login(
        @Field("username") String username,
        @Field("password") String password
    );
}

class UserRequest {
    String username;
    String password;
    public UserRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class LoginResponse {
    public String token;
    public String username;
} 