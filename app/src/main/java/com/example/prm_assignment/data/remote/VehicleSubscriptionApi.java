package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.CreateSubscriptionRequest;
import com.example.prm_assignment.data.model.DeleteSubscriptionResponse;
import com.example.prm_assignment.data.model.RenewSubscriptionRequest;
import com.example.prm_assignment.data.model.SingleSubscriptionResponse;
import com.example.prm_assignment.data.model.UpdateSubscriptionRequest;
import com.example.prm_assignment.data.model.UpdateSubscriptionStatusRequest;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VehicleSubscriptionApi {
    
    // GET /api/vehicle-subscriptions - Get all subscriptions
    @GET("vehicle-subscriptions")
    Call<VehicleSubscriptionResponse> getAllSubscriptions(
            @Header("Authorization") String authHeader
    );

    // GET /api/vehicle-subscriptions/:id - Get subscription by ID
    @GET("vehicle-subscriptions/{id}")
    Call<SingleSubscriptionResponse> getSubscriptionById(
            @Header("Authorization") String authHeader,
            @Path("id") String subscriptionId
    );

    // GET /api/vehicle-subscriptions/vehicle/:vehicleId - Get subscriptions by vehicle
    @GET("vehicle-subscriptions/vehicle/{vehicleId}")
    Call<VehicleSubscriptionResponse> getSubscriptionsByVehicle(
            @Header("Authorization") String authHeader,
            @Path("vehicleId") String vehicleId
    );

    // GET /api/vehicle-subscriptions/customer/:customerId - Get subscriptions by customer
    @GET("vehicle-subscriptions/customer/{customerId}")
    Call<VehicleSubscriptionResponse> getCustomerSubscriptions(
            @Header("Authorization") String authHeader,
            @Path("customerId") String customerId
    );

    // POST /api/vehicle-subscriptions - Create new subscription
    @POST("vehicle-subscriptions")
    Call<SingleSubscriptionResponse> createSubscription(
            @Header("Authorization") String authHeader,
            @Body CreateSubscriptionRequest request
    );

    // PATCH /api/vehicle-subscriptions/:id - Update subscription
    @PATCH("vehicle-subscriptions/{id}")
    Call<SingleSubscriptionResponse> updateSubscription(
            @Header("Authorization") String authHeader,
            @Path("id") String subscriptionId,
            @Body UpdateSubscriptionRequest request
    );

    // PATCH /api/vehicle-subscriptions/:id/status - Update subscription status
    @PATCH("vehicle-subscriptions/{id}/status")
    Call<SingleSubscriptionResponse> updateSubscriptionStatus(
            @Header("Authorization") String authHeader,
            @Path("id") String subscriptionId,
            @Body UpdateSubscriptionStatusRequest request
    );

    // DELETE /api/vehicle-subscriptions/:id - Delete subscription
    @DELETE("vehicle-subscriptions/{id}")
    Call<DeleteSubscriptionResponse> deleteSubscription(
            @Header("Authorization") String authHeader,
            @Path("id") String subscriptionId
    );

    // GET /api/vehicle-subscriptions/expiring/:days - Get expiring subscriptions
    @GET("vehicle-subscriptions/expiring/{days}")
    Call<VehicleSubscriptionResponse> getExpiringSubscriptions(
            @Header("Authorization") String authHeader,
            @Path("days") int days
    );

    // POST /api/vehicle-subscriptions/:id/renew - Renew subscription
    @POST("vehicle-subscriptions/{id}/renew")
    Call<SingleSubscriptionResponse> renewSubscription(
            @Header("Authorization") String authHeader,
            @Path("id") String subscriptionId,
            @Body RenewSubscriptionRequest request
    );

    // POST /api/vehicle-subscriptions/update-expired - Update expired subscriptions
    @POST("vehicle-subscriptions/update-expired")
    Call<BaseResponse> updateExpiredSubscriptions(
            @Header("Authorization") String authHeader
    );
    
    class BaseResponse {
        private boolean success;
        private String message;
        
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
}
