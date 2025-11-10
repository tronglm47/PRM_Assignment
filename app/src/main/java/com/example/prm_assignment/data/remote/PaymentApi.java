package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.PaymentRequest;
import com.example.prm_assignment.data.model.PaymentResponse;
import com.example.prm_assignment.data.model.PaymentWebhookRequest;
import com.example.prm_assignment.data.model.PaymentWebhookResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApi {
    @POST("payments")
    Call<PaymentResponse> createPayment(
            @Header("Authorization") String authHeader,
            @Body PaymentRequest request
    );

    @GET("payments/{paymentId}")
    Call<PaymentResponse> getPaymentStatus(
            @Header("Authorization") String authHeader,
            @Path("paymentId") String paymentId
    );

    // Webhook endpoint to handle PayOS payment notifications
    @POST("payments/webhook")
    Call<PaymentWebhookResponse> handlePaymentWebhook(
            @Body PaymentWebhookRequest request
    );
}

