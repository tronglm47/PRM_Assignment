package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.PackagesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface PackagesApi {
    @GET("service-packages")
    Call<PackagesResponse> getPackages(@Header("Authorization") String token);
}

