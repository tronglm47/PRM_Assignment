package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.CustomerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface CustomerApi {
    // Gọi /api/customer/user/{userId}
    @GET("customers/user/{userId}")
    Call<CustomerResponse> getCustomerByUserId(
            @Header("Authorization") String token,
            @Path("userId") String userId
    );
}
