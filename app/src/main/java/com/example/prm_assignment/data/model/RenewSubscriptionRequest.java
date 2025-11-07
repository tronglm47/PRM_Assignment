package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class RenewSubscriptionRequest {
    @SerializedName("newPackageId")
    private String newPackageId;

    public RenewSubscriptionRequest(String newPackageId) {
        this.newPackageId = newPackageId;
    }

    public String getNewPackageId() {
        return newPackageId;
    }

    public void setNewPackageId(String newPackageId) {
        this.newPackageId = newPackageId;
    }
}
