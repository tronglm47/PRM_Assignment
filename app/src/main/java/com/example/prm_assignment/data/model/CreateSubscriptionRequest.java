package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateSubscriptionRequest {
    @SerializedName("vehicleId")
    private String vehicleId;

    @SerializedName("package_id")
    private String packageId;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("status")
    private String status; // "ACTIVE", "PENDING", "EXPIRED"

    public CreateSubscriptionRequest(String vehicleId, String packageId, String startDate, String endDate, String status) {
        this.vehicleId = vehicleId;
        this.packageId = packageId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
