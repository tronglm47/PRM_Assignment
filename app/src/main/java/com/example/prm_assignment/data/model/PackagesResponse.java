package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PackagesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<PackageModel> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<PackageModel> getData() {
        return data;
    }

    public void setData(List<PackageModel> data) {
        this.data = data;
    }
}

