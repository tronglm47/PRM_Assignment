package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehicleResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<VehicleModel> data;

    public boolean isSuccess() {
        return success;
    }

    public List<VehicleModel> getData() {
        return data;
    }
}

