package com.example.prm_assignment.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.prm_assignment.data.model.PaymentWebhookRequest;
import com.example.prm_assignment.data.model.PaymentWebhookResponse;
import com.example.prm_assignment.data.remote.PaymentApi;
import com.example.prm_assignment.data.remote.PaymentRetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private static final String TAG = "PaymentRepository";
    private final PaymentApi api;

    public PaymentRepository() {
        this.api = PaymentRetrofitClient.getInstance().getPaymentApi();
    }

    // Callback interface for webhook handling
    public interface WebhookCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Handle payment webhook notification
    public void handlePaymentWebhook(int orderCode, String status, WebhookCallback callback) {
        PaymentWebhookRequest request = new PaymentWebhookRequest(orderCode, status);

        Log.d(TAG, "Sending webhook notification - orderCode: " + orderCode + ", status: " + status);

        api.handlePaymentWebhook(request).enqueue(new Callback<PaymentWebhookResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentWebhookResponse> call,
                                 @NonNull Response<PaymentWebhookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Log.d(TAG, "Webhook processed successfully: " + response.body().getMessage());
                        callback.onSuccess(response.body().getMessage());
                    } else {
                        Log.e(TAG, "Webhook failed: " + response.body().getMessage());
                        callback.onError(response.body().getMessage());
                    }
                } else {
                    String error = "Failed to process webhook: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentWebhookResponse> call, @NonNull Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Webhook request failed", t);
                callback.onError(error);
            }
        });
    }
}

