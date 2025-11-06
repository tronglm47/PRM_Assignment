package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehicleSubscriptionResponse {
    private boolean success;
    private List<VehicleSubscription> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<VehicleSubscription> getData() {
        return data;
    }

    public void setData(List<VehicleSubscription> data) {
        this.data = data;
    }

    public static class VehicleSubscription {
        @SerializedName("_id")
        private String id;

        @SerializedName("vehicleId")
        private VehicleInfo vehicleInfo;

        @SerializedName("package_id")
        private PackageInfo packageInfo;

        @SerializedName("start_date")
        private String startDate;

        @SerializedName("end_date")
        private String endDate;

        private String status;
        private String createdAt;
        private String updatedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public VehicleInfo getVehicleInfo() {
            return vehicleInfo;
        }

        public void setVehicleInfo(VehicleInfo vehicleInfo) {
            this.vehicleInfo = vehicleInfo;
        }

        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        public void setPackageInfo(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
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

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class VehicleInfo {
        @SerializedName("_id")
        private String id;

        private String vehicleName;
        private String model;

        @SerializedName("VIN")
        private String vin;

        private String imageUrl;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVehicleName() {
            return vehicleName;
        }

        public void setVehicleName(String vehicleName) {
            this.vehicleName = vehicleName;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class PackageInfo {
        @SerializedName("_id")
        private String id;

        private String name;
        private String description;
        private double price;
        private int duration;

        @SerializedName("km_interval")
        private int kmInterval;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getKmInterval() {
            return kmInterval;
        }

        public void setKmInterval(int kmInterval) {
            this.kmInterval = kmInterval;
        }
    }
}

