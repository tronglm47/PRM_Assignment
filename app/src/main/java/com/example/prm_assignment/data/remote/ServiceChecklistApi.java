package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.ServiceChecklistResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ServiceChecklistApi {

    /**
     * Get service checklists by record ID
     * Endpoint: GET /api/service-checklists
     */
    @GET("service-checklists")
    Call<ServiceChecklistResponse> getServiceChecklists(
            @Header("Authorization") String token,
            @Query("record_id") String recordId
    );
}

