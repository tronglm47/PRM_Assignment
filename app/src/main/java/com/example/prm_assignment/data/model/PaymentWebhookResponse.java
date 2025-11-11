package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentWebhookResponse {
    private boolean success;
    private String message;

    public PaymentWebhookResponse() {
    }

    public PaymentWebhookResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
}

