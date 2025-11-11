package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateSubscriptionStatusRequest {
    @SerializedName("status")
    private String status;

    public UpdateSubscriptionStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
