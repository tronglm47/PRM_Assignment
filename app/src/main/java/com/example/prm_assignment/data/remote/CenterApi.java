package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.CentersResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CenterApi {
    @GET("centers")
    Call<CentersResponse> getCenters(
            @Header("Authorization") String bearerToken,
            @Query("name") String name,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );
}
