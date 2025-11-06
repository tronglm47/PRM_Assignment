package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProfileResponse {
    private boolean success;
    private String message;
    private ProfileData data;

    public ProfileResponse() {
    }

    public ProfileResponse(boolean success, String message, ProfileData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

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

    public ProfileData getData() {
        return data;
    }

    public void setData(ProfileData data) {
        this.data = data;
    }

    public static class ProfileData {
        @SerializedName("_id")
        private String id;
        private UserId userId;
        private String customerName;
        private String address;
        private String dateOfBirth;
        private List<String> deviceTokens;
        private String createdAt;
        private String updatedAt;

        public ProfileData() {
        }

        public ProfileData(String id, UserId userId, String customerName, String address,
                          String dateOfBirth, List<String> deviceTokens, String createdAt, String updatedAt) {
            this.id = id;
            this.userId = userId;
            this.customerName = customerName;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
            this.deviceTokens = deviceTokens;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public UserId getUserId() {
            return userId;
        }

        public void setUserId(UserId userId) {
            this.userId = userId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public List<String> getDeviceTokens() {
            return deviceTokens;
        }

        public void setDeviceTokens(List<String> deviceTokens) {
            this.deviceTokens = deviceTokens;
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

    public static class UserId {
        @SerializedName("_id")
        private String id;
        private String phone;
        private String role;
        private String email;

        public UserId() {
        }

        public UserId(String id, String phone, String role, String email) {
            this.id = id;
            this.phone = phone;
            this.role = role;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

