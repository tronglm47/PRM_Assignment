package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.SlotsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SlotApi {
    @GET("slots")
    Call<SlotsResponse> getSlots(
        @Header("Authorization") String token,
        @Query("center_id") String centerId,
        @Query("slot_date") String slotDate
    );
}

