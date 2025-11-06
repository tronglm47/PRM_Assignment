package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VehicleSubscriptionApi {
    @GET("vehicle-subscriptions/customer/{customerId}")
    Call<VehicleSubscriptionResponse> getCustomerSubscriptions(
            @Path("customerId") String customerId,
            @Header("Authorization") String authHeader
    );
}

