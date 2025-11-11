package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.data.model.AppointmentRequest;
import com.example.prm_assignment.data.model.AppointmentsResponse;
import com.example.prm_assignment.data.model.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AppointmentApi {

    /**
     * 🟢 Tạo mới một cuộc hẹn bảo dưỡng (appointment)
     * Endpoint: POST /api/appointments
     */
    @POST("appointments")
    Call<BaseResponse> createAppointment(
            @Header("Authorization") String token,
            @Body AppointmentRequest request
    );

    /**
     * 🟢 Lấy danh sách các cuộc hẹn với populate để lấy thông tin chi tiết
     * Endpoint: GET /api/appointments?populate=vehicle_id,center_id
     */
    @GET("appointments")
    Call<AppointmentsResponse> getAppointments(
            @Header("Authorization") String token,
            @Query("customer_id") String customerId,
            @Query("populate") String populate
    );
}
