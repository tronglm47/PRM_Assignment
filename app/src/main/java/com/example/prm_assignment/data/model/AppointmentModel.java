package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentModel {
    @SerializedName("_id")
    private String id;

    @SerializedName("customer_id")
    private CustomerInfo customerId;

    @SerializedName("vehicle_id")
    private VehicleInfo vehicleId;

    @SerializedName("center_id")
    private CenterInfo centerId;

    @SerializedName("staffId")
    private String staffId;  // Changed from StaffInfo to String because API returns null or string ID

    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;
    private String updatedAt;

    public AppointmentModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomerInfo getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerInfo customerId) {
        this.customerId = customerId;
    }

    public VehicleInfo getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(VehicleInfo vehicleId) {
        this.vehicleId = vehicleId;
    }

    public CenterInfo getCenterId() {
        return centerId;
    }

    public void setCenterId(CenterInfo centerId) {
        this.centerId = centerId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public static class CustomerInfo {
        @SerializedName("_id")
        private String id;
        private String customerName;
        private String address;
        private String dateOfBirth;

        public String getId() {
            return id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getAddress() {
            return address;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }

    public static class VehicleInfo {
        @SerializedName("_id")
        private String id;
        private String vehicleName;
        private String model;
        private String plateNumber;
        private Integer mileage;

        public String getId() {
            return id;
        }

        public String getVehicleName() {
            return vehicleName;
        }

        public String getModel() {
            return model;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public Integer getMileage() {
            return mileage;
        }
    }

    public static class CenterInfo {
        @SerializedName("_id")
        private String id;
        private String name;
        private String address;
        private String phone;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getPhone() {
            return phone;
        }
    }
}
