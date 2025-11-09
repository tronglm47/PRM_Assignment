package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("subscription_id")
    private String subscriptionId;

    @SerializedName("customer_id")
    private String customerId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("payment_type")
    private String paymentType;

    @SerializedName("returnUrl")
    private String returnUrl;

    @SerializedName("cancelUrl")
    private String cancelUrl;

    public PaymentRequest() {
    }

    public PaymentRequest(String subscriptionId, String customerId, double amount, String paymentType, String returnUrl, String cancelUrl) {
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    // Getters and Setters
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
}

