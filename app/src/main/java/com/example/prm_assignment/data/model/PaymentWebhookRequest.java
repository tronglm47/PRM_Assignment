package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentWebhookRequest {
    @SerializedName("order_code")
    private int orderCode;

    @SerializedName("status")
    private String status;

    public PaymentWebhookRequest() {
    }

    public PaymentWebhookRequest(int orderCode, String status) {
        this.orderCode = orderCode;
        this.status = status;
    }

    public int getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(int orderCode) {
        this.orderCode = orderCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

