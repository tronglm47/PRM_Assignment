package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PaymentData data;

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

    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }

    public static class PaymentData {
        @SerializedName("_id")
        private String id;

        @SerializedName("subscription_id")
        private String subscriptionId;

        @SerializedName("customer_id")
        private String customerId;

        @SerializedName("amount")
        private double amount;

        @SerializedName("payment_type")
        private String paymentType;

        @SerializedName("status")
        private String status;

        @SerializedName("paymentUrl")
        private String paymentUrl;

        @SerializedName("returnUrl")
        private String returnUrl;

        @SerializedName("cancelUrl")
        private String cancelUrl;

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

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public void setPaymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public String getCancelUrl() {
            return cancelUrl;
        }

        public void setCancelUrl(String cancelUrl) {
            this.cancelUrl = cancelUrl;
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
}

