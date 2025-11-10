package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class VehicleModel {
    @SerializedName("_id")
    private String id;

    @SerializedName("vehicleName")
    private String vehicleName;

    @SerializedName("model")
    private String model;

    @SerializedName("year")
    private int year;

    @SerializedName("VIN")
    private String vin;

    @SerializedName("price")
    private double price;

    @SerializedName("mileage")
    private double mileage;

    @SerializedName("plateNumber")
    private String plateNumber;

    @SerializedName("last_service_date")
    private String lastServiceDate;

    @SerializedName("last_alert_mileage")
    private double lastAlertMileage;

    @SerializedName("image")
    private String image;

    @SerializedName("customerId")
    private Customer customerId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters and Setters
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getVin() {
        return vin;
    }

    public double getPrice() {
        return price;
    }

    public double getMileage() {
        return mileage;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getLastServiceDate() {
        return lastServiceDate;
    }

    public double getLastAlertMileage() {
        return lastAlertMileage;
    }

    public String getImage() {
        return image;
    }

    public Customer getCustomerId() {
        return customerId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Inner class for Customer
    public static class Customer {
        @SerializedName("_id")
        private String id;

        @SerializedName("customerName")
        private String customerName;

        @SerializedName("address")
        private String address;

        public String getId() {
            return id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getAddress() {
            return address;
        }
    }
}

