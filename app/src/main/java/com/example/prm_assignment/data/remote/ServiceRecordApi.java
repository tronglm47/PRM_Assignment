package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.ServiceRecordResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ServiceRecordApi {

    /**
     * Get service record by appointment ID
     * Endpoint: GET /api/service-records
     */
    @GET("service-records")
    Call<ServiceRecordResponse> getServiceRecordByAppointment(
            @Header("Authorization") String token,
            @Query("appointment_id") String appointmentId
    );
}

