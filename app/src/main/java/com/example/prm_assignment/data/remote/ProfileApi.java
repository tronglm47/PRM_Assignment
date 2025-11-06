package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.ProfileResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ProfileApi {
    @GET("auth/profile")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token);
}

