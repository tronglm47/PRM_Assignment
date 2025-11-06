package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.model.VehicleDetailResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VehiclesApi {
    @GET("vehicles/my-vehicles")
    Call<VehicleResponse> getMyVehicles(@Header("Authorization") String token);

    @GET("vehicles/{id}")
    Call<VehicleDetailResponse> getVehicleById(@Path("id") String vehicleId, @Header("Authorization") String token);
}

