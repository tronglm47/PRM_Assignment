package com.example.prm_assignment.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.AppointmentModel;
import com.example.prm_assignment.data.model.AppointmentsResponse;
import com.example.prm_assignment.data.model.CenterModel;
import com.example.prm_assignment.data.model.CustomerResponse;
import com.example.prm_assignment.data.model.ProfileResponse;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.remote.AppointmentApi;
import com.example.prm_assignment.data.remote.AppointmentRetrofitClient;
import com.example.prm_assignment.data.remote.CenterApi;
import com.example.prm_assignment.data.remote.CenterRetrofitClient;
import com.example.prm_assignment.data.remote.CustomerApi;
import com.example.prm_assignment.data.remote.CustomerRetrofitClient;
import com.example.prm_assignment.data.remote.ProfileApi;
import com.example.prm_assignment.data.remote.ProfileRetrofitClient;
import com.example.prm_assignment.data.remote.VehiclesApi;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordsFragment extends Fragment {
    private static final String TAG = "RecordsFragment";

    private LinearLayout llAppointmentsContainer;
    private LinearLayout emptyState;
    private FrameLayout loadingOverlay;
    private TokenHelper tokenHelper;
    private AppointmentApi appointmentApi;
    private ProfileApi profileApi;
    private CustomerApi customerApi;
    private VehiclesApi vehiclesApi;
    private CenterApi centerApi;
    private String customerId;

    // Cache for vehicles and centers
    private List<VehicleModel> myVehicles = new ArrayList<>();
    private List<CenterModel> allCenters = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        // Initialize helpers and APIs
        tokenHelper = new TokenHelper(requireContext());
        appointmentApi = AppointmentRetrofitClient.getInstance().getAppointmentApi();
        profileApi = ProfileRetrofitClient.getInstance().getProfileApi();
        customerApi = CustomerRetrofitClient.getInstance().getCustomerApi();
        vehiclesApi = VehiclesRetrofitClient.getInstance().getVehiclesApi();
        centerApi = CenterRetrofitClient.getInstance().getCenterApi();

        // Initialize views
        llAppointmentsContainer = view.findViewById(R.id.llAppointmentsContainer);
        emptyState = view.findViewById(R.id.emptyState);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Show loading and fetch data
        showLoading();
        loadProfileAndAppointments();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        Log.d(TAG, "onResume - Refreshing appointments");
        showLoading();
        loadProfileAndAppointments();
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    private void loadProfileAndAppointments() {
        tokenHelper.getTokenAndExecute(token -> {
            if (token != null && !token.isEmpty()) {
                fetchProfile("Bearer " + token);
            } else {
                Log.e(TAG, "No token found");
                hideLoading();
                showEmptyState();
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProfile(String authHeader) {
        profileApi.getProfile(authHeader).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess() && profileResponse.getData() != null) {
                        ProfileResponse.UserId userId = profileResponse.getData().getUserId();
                        if (userId != null && userId.getId() != null) {
                            String userIdStr = userId.getId();
                            Log.d(TAG, "User ID from profile: " + userIdStr);
                            // Fetch customer_id using userId
                            fetchCustomer(authHeader, userIdStr);
                        } else {
                            Log.e(TAG, "User ID is null in profile");
                            hideLoading();
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "Profile data is null");
                        hideLoading();
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Profile fetch failed: " + response.code());
                    hideLoading();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Profile fetch error: " + t.getMessage(), t);
                hideLoading();
                showEmptyState();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCustomer(String authHeader, String userId) {
        Log.d(TAG, "Fetching customer for userId: " + userId);
        customerApi.getCustomerByUserId(authHeader, userId).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(@NonNull Call<CustomerResponse> call, @NonNull Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse customerResponse = response.body();
                    if (customerResponse.isSuccess() && customerResponse.getData() != null) {
                        customerId = customerResponse.getData().getId();
                        Log.d(TAG, "Customer ID: " + customerId);

                        // Fetch vehicles and centers first, then fetch appointments
                        fetchVehiclesAndCenters(authHeader, customerId);
                    } else {
                        Log.e(TAG, "Customer data is null");
                        hideLoading();
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Customer fetch failed: " + response.code());
                    hideLoading();
                    showEmptyState();
                    Toast.makeText(getContext(), "Không thể lấy thông tin khách hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CustomerResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Customer fetch error: " + t.getMessage(), t);
                hideLoading();
                showEmptyState();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVehiclesAndCenters(String authHeader, String customerId) {
        // Fetch vehicles
        vehiclesApi.getMyVehicles(authHeader).enqueue(new Callback<com.example.prm_assignment.data.model.VehicleResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.prm_assignment.data.model.VehicleResponse> call,
                                 @NonNull Response<com.example.prm_assignment.data.model.VehicleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    myVehicles = response.body().getData();
                    Log.d(TAG, "✅ Loaded " + (myVehicles != null ? myVehicles.size() : 0) + " vehicles");
                }

                // Fetch centers
                centerApi.getCenters(authHeader, null, 1, 100).enqueue(new Callback<com.example.prm_assignment.data.model.CentersResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.prm_assignment.data.model.CentersResponse> call2,
                                         @NonNull Response<com.example.prm_assignment.data.model.CentersResponse> response2) {
                        if (response2.isSuccessful() && response2.body() != null && response2.body().isSuccess()) {
                            allCenters = response2.body().getData().getCenters();
                            Log.d(TAG, "✅ Loaded " + (allCenters != null ? allCenters.size() : 0) + " centers");
                        }

                        // Now fetch appointments with cached data
                        fetchAppointments(authHeader, customerId);
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.prm_assignment.data.model.CentersResponse> call2, @NonNull Throwable t) {
                        Log.e(TAG, "Centers fetch error: " + t.getMessage());
                        // Continue anyway
                        fetchAppointments(authHeader, customerId);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<com.example.prm_assignment.data.model.VehicleResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Vehicles fetch error: " + t.getMessage());
                // Continue anyway
                fetchAppointments(authHeader, customerId);
            }
        });
    }

    private void fetchAppointments(String authHeader, String customerId) {
        Log.d(TAG, "Fetching appointments for customer: " + customerId);
        Log.d(TAG, "Auth header: " + authHeader);

        // Add populate parameter to get detailed vehicle and center info
        String populate = "vehicle_id,center_id";

        appointmentApi.getAppointments(authHeader, customerId, populate).enqueue(new Callback<AppointmentsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppointmentsResponse> call, @NonNull Response<AppointmentsResponse> response) {
                hideLoading();
                Log.d(TAG, "Appointments response code: " + response.code());
                Log.d(TAG, "Request URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    AppointmentsResponse appointmentsResponse = response.body();
                    Log.d(TAG, "Response success: " + appointmentsResponse.isSuccess());
                    Log.d(TAG, "Response data: " + (appointmentsResponse.getData() != null ? "not null" : "null"));

                    if (appointmentsResponse.isSuccess() && appointmentsResponse.getData() != null) {
                        // Get appointments from nested data.appointments
                        List<AppointmentModel> appointments = appointmentsResponse.getData().getAppointments();
                        Log.d(TAG, "Appointments list: " + (appointments != null ? "not null" : "null"));

                        if (appointments != null && !appointments.isEmpty()) {
                            Log.d(TAG, "✅ Loaded " + appointments.size() + " appointments");
                            for (int i = 0; i < appointments.size(); i++) {
                                AppointmentModel apt = appointments.get(i);
                                Log.d(TAG, "Appointment " + (i+1) + ": ID=" + apt.getId() +
                                      ", Status=" + apt.getStatus() +
                                      ", Date=" + apt.getStartTime());
                                // Log vehicle and center info
                                if (apt.getVehicleId() != null) {
                                    Log.d(TAG, "  Vehicle: " + apt.getVehicleId().getVehicleName() +
                                          " - " + apt.getVehicleId().getPlateNumber());
                                }
                                if (apt.getCenterId() != null) {
                                    Log.d(TAG, "  Center: " + apt.getCenterId().getName());
                                }
                            }
                            hideEmptyState();
                            displayAppointments(appointments);
                        } else {
                            Log.w(TAG, "❌ No appointments found or list is empty");
                            showEmptyState();
                            Toast.makeText(getContext(), "Bạn chưa có lịch hẹn nào", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Appointments response unsuccessful or data is null");
                        if (appointmentsResponse.getMessage() != null) {
                            Log.w(TAG, "Message: " + appointmentsResponse.getMessage());
                        }
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Appointments fetch failed: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    showEmptyState();
                    Toast.makeText(getContext(), "Không thể tải lịch hẹn: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppointmentsResponse> call, @NonNull Throwable t) {
                hideLoading();
                Log.e(TAG, "❌ Appointments fetch error: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                showEmptyState();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAppointments(java.util.List<AppointmentModel> appointments) {
        if (llAppointmentsContainer == null) return;

        llAppointmentsContainer.removeAllViews();

        for (AppointmentModel appointment : appointments) {
            addAppointmentCard(appointment);
        }
    }

    private void addAppointmentCard(AppointmentModel appointment) {
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_appointment, llAppointmentsContainer, false);

        TextView tvAppointmentDate = cardView.findViewById(R.id.tvAppointmentDate);
        TextView tvStatus = cardView.findViewById(R.id.tvStatus);
        TextView tvVehicleInfo = cardView.findViewById(R.id.tvVehicleInfo);
        TextView tvCenterInfo = cardView.findViewById(R.id.tvCenterInfo);
        TextView tvTimeInfo = cardView.findViewById(R.id.tvTimeInfo);
        LinearLayout llStaffInfo = cardView.findViewById(R.id.llStaffInfo);
        TextView tvStaffInfo = cardView.findViewById(R.id.tvStaffInfo);

        // Format and set date
        String formattedDate = formatDate(appointment.getStartTime());
        tvAppointmentDate.setText(formattedDate);

        // Set status with color
        String status = appointment.getStatus();
        tvStatus.setText(getStatusText(status));
        tvStatus.setBackgroundColor(getStatusColor(status));

        // Set vehicle info - use cached data if only ID is available
        String vehicleInfo = "Không rõ";
        if (appointment.getVehicleId() != null) {
            String vehicleName = appointment.getVehicleId().getVehicleName();
            String model = appointment.getVehicleId().getModel();
            String plateNumber = appointment.getVehicleId().getPlateNumber();
            String vehicleId = appointment.getVehicleId().getId();

            // Check if we have full vehicle data from API
            if (vehicleName != null || model != null) {
                vehicleInfo = (vehicleName != null ? vehicleName : model);
                if (plateNumber != null) {
                    vehicleInfo += " - " + plateNumber;
                }
            } else if (vehicleId != null && myVehicles != null) {
                // Look up from cached vehicles
                for (com.example.prm_assignment.data.model.VehicleModel v : myVehicles) {
                    if (vehicleId.equals(v.getId())) {
                        vehicleInfo = (v.getVehicleName() != null ? v.getVehicleName() : v.getModel());
                        if (v.getPlateNumber() != null) {
                            vehicleInfo += " - " + v.getPlateNumber();
                        }
                        Log.d(TAG, "✅ Found vehicle from cache: " + vehicleInfo);
                        break;
                    }
                }
            }
        }
        tvVehicleInfo.setText(vehicleInfo);

        // Set center info - use cached data if only ID is available
        String centerInfo = "Không rõ";
        if (appointment.getCenterId() != null) {
            String centerName = appointment.getCenterId().getName();
            String centerId = appointment.getCenterId().getId();

            // Check if we have full center data from API
            if (centerName != null && !centerName.isEmpty()) {
                centerInfo = centerName;
            } else if (centerId != null && allCenters != null) {
                // Look up from cached centers
                for (com.example.prm_assignment.data.model.CenterModel c : allCenters) {
                    if (centerId.equals(c.getId())) {
                        centerInfo = c.getName();
                        Log.d(TAG, "✅ Found center from cache: " + centerInfo);
                        break;
                    }
                }
            }
        }
        tvCenterInfo.setText(centerInfo);

        // Set time info
        String timeInfo = formatTime(appointment.getStartTime()) + " - " + formatTime(appointment.getEndTime());
        tvTimeInfo.setText(timeInfo);

        // Set staff info if available
        if (appointment.getStaffId() != null && !appointment.getStaffId().isEmpty()) {
            llStaffInfo.setVisibility(View.VISIBLE);
            tvStaffInfo.setText("Staff ID: " + appointment.getStaffId());
        } else {
            llStaffInfo.setVisibility(View.GONE);
        }

        // Add click listener to open detail activity
        final String finalVehicleInfo = vehicleInfo;
        final String finalCenterInfo = centerInfo;
        cardView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(),
                    com.example.prm_assignment.ui.activities.AppointmentDetailActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            intent.putExtra("date", formattedDate);
            intent.putExtra("time", timeInfo);
            intent.putExtra("vehicle", finalVehicleInfo);
            intent.putExtra("center", finalCenterInfo);
            intent.putExtra("status", status);
            startActivity(intent);
        });

        llAppointmentsContainer.addView(cardView);
    }

    private String formatDate(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);
            return date != null ? outputFormat.format(date) : dateTimeString;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
            return dateTimeString;
        }
    }

    private String formatTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);
            return date != null ? outputFormat.format(date) : "";
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing time: " + e.getMessage());
            return "";
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "N/A";
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#64748B");
        switch (status.toLowerCase()) {
            case "pending":
                return Color.parseColor("#F59E0B"); // Orange
            case "confirmed":
                return Color.parseColor("#3B82F6"); // Blue
            case "completed":
                return Color.parseColor("#10B981"); // Green
            case "cancelled":
                return Color.parseColor("#EF4444"); // Red
            default:
                return Color.parseColor("#64748B"); // Gray
        }
    }

    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (llAppointmentsContainer != null) {
            llAppointmentsContainer.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (llAppointmentsContainer != null) {
            llAppointmentsContainer.setVisibility(View.VISIBLE);
        }
    }
}
