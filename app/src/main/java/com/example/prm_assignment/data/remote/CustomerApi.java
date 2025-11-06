package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.CustomerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface CustomerApi {
    @GET("customers/user/{userId}")
    Call<CustomerResponse> getCustomerByUserId(
            @Path("userId") String userId,
            @Header("Authorization") String authHeader
    );
}

