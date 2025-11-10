package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class SingleSubscriptionResponse {
    private boolean success;
    private String message;
    
    @SerializedName("data")
    private VehicleSubscriptionResponse.VehicleSubscription data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VehicleSubscriptionResponse.VehicleSubscription getData() {
        return data;
    }

    public void setData(VehicleSubscriptionResponse.VehicleSubscription data) {
        this.data = data;
    }
}
