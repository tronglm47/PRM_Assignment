package com.example.prm_assignment.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.prm_assignment.data.model.CreateSubscriptionRequest;
import com.example.prm_assignment.data.model.DeleteSubscriptionResponse;
import com.example.prm_assignment.data.model.RenewSubscriptionRequest;
import com.example.prm_assignment.data.model.SingleSubscriptionResponse;
import com.example.prm_assignment.data.model.UpdateSubscriptionRequest;
import com.example.prm_assignment.data.model.UpdateSubscriptionStatusRequest;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import com.example.prm_assignment.data.remote.VehicleSubscriptionApi;
import com.example.prm_assignment.data.remote.VehicleSubscriptionRetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleSubscriptionRepository {
    private static final String TAG = "VehicleSubscriptionRepo";
    private final VehicleSubscriptionApi api;

    public VehicleSubscriptionRepository() {
        this.api = VehicleSubscriptionRetrofitClient.getInstance().getVehicleSubscriptionApi();
    }

    // Callback interfaces
    public interface SubscriptionListCallback {
        void onSuccess(VehicleSubscriptionResponse response);
        void onError(String error);
    }

    public interface SingleSubscriptionCallback {
        void onSuccess(VehicleSubscriptionResponse.VehicleSubscription subscription);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface UpdateExpiredCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Get all subscriptions
    public void getAllSubscriptions(String token, SubscriptionListCallback callback) {
        String authHeader = "Bearer " + token;
        api.getAllSubscriptions(authHeader).enqueue(new Callback<VehicleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSubscriptionResponse> call, 
                                 @NonNull Response<VehicleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch subscriptions: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching all subscriptions", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get subscription by ID
    public void getSubscriptionById(String token, String subscriptionId, SingleSubscriptionCallback callback) {
        String authHeader = "Bearer " + token;
        api.getSubscriptionById(authHeader, subscriptionId).enqueue(new Callback<SingleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call, 
                                 @NonNull Response<SingleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Subscription not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching subscription by ID", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get subscriptions by vehicle
    public void getSubscriptionsByVehicle(String token, String vehicleId, SubscriptionListCallback callback) {
        String authHeader = "Bearer " + token;
        api.getSubscriptionsByVehicle(authHeader, vehicleId).enqueue(new Callback<VehicleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSubscriptionResponse> call, 
                                 @NonNull Response<VehicleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch vehicle subscriptions: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching subscriptions by vehicle", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get subscriptions by customer
    public void getSubscriptionsByCustomer(String token, String customerId, SubscriptionListCallback callback) {
        String authHeader = "Bearer " + token;
        api.getCustomerSubscriptions(authHeader, customerId).enqueue(new Callback<VehicleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSubscriptionResponse> call, 
                                 @NonNull Response<VehicleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch customer subscriptions: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching subscriptions by customer", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Create new subscription
    public void createSubscription(String token, CreateSubscriptionRequest request, 
                                  SingleSubscriptionCallback callback) {
        String authHeader = "Bearer " + token;
        api.createSubscription(authHeader, request).enqueue(new Callback<SingleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call, 
                                 @NonNull Response<SingleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to create subscription: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error creating subscription", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update subscription
    public void updateSubscription(String token, String subscriptionId, 
                                  UpdateSubscriptionRequest request, 
                                  SingleSubscriptionCallback callback) {
        String authHeader = "Bearer " + token;
        api.updateSubscription(authHeader, subscriptionId, request)
           .enqueue(new Callback<SingleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call, 
                                 @NonNull Response<SingleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to update subscription: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error updating subscription", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update subscription status
    public void updateSubscriptionStatus(String token, String subscriptionId, String status,
                                        SingleSubscriptionCallback callback) {
        String authHeader = "Bearer " + token;

        // Use updateSubscription endpoint with only status field
        // because backend returns 404 for /status endpoint
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setStatus(status);

        api.updateSubscription(authHeader, subscriptionId, request)
           .enqueue(new Callback<SingleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call, 
                                 @NonNull Response<SingleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to update status: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error updating subscription status", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Delete subscription
    public void deleteSubscription(String token, String subscriptionId, DeleteCallback callback) {
        String authHeader = "Bearer " + token;
        api.deleteSubscription(authHeader, subscriptionId).enqueue(new Callback<DeleteSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteSubscriptionResponse> call, 
                                 @NonNull Response<DeleteSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onError("Failed to delete subscription: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error deleting subscription", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get expiring subscriptions
    public void getExpiringSubscriptions(String token, int days, SubscriptionListCallback callback) {
        String authHeader = "Bearer " + token;
        api.getExpiringSubscriptions(authHeader, days).enqueue(new Callback<VehicleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSubscriptionResponse> call, 
                                 @NonNull Response<VehicleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch expiring subscriptions: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching expiring subscriptions", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Renew subscription
    public void renewSubscription(String token, String subscriptionId, String newPackageId,
                                 SingleSubscriptionCallback callback) {
        String authHeader = "Bearer " + token;
        RenewSubscriptionRequest request = new RenewSubscriptionRequest(newPackageId);
        api.renewSubscription(authHeader, subscriptionId, request)
           .enqueue(new Callback<SingleSubscriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call, 
                                 @NonNull Response<SingleSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to renew subscription: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error renewing subscription", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update expired subscriptions (batch operation)
    public void updateExpiredSubscriptions(String token, UpdateExpiredCallback callback) {
        String authHeader = "Bearer " + token;
        api.updateExpiredSubscriptions(authHeader).enqueue(new Callback<VehicleSubscriptionApi.BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSubscriptionApi.BaseResponse> call, 
                                 @NonNull Response<VehicleSubscriptionApi.BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onError("Failed to update expired subscriptions: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSubscriptionApi.BaseResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error updating expired subscriptions", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
