package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class VehicleDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private VehicleModel data;

    public boolean isSuccess() {
        return success;
    }

    public VehicleModel getData() {
        return data;
    }
}

