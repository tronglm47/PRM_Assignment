package com.example.prm_assignment.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.AppointmentRequest;
import com.example.prm_assignment.data.model.BaseResponse;
import com.example.prm_assignment.data.model.CenterModel;
import com.example.prm_assignment.data.model.CentersResponse;
import com.example.prm_assignment.data.model.CustomerResponse;
import com.example.prm_assignment.data.remote.AppointmentApi;
import com.example.prm_assignment.data.remote.AppointmentRetrofitClient;
import com.example.prm_assignment.data.remote.CenterApi;
import com.example.prm_assignment.data.remote.CenterRetrofitClient;
import com.example.prm_assignment.data.remote.CustomerApi;
import com.example.prm_assignment.data.remote.CustomerRetrofitClient;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {

    private static final String ARG_VEHICLE_ID = "vehicle_id";
    private String vehicleId;

    // UI
    private LinearLayout centerContainer;
    private TextView tvDate, tvTime;
    private Button btnConfirm;
    private ProgressBar progressBar;

    private String selectedCenterId;
    private String selectedDate, selectedTime;

    private AppointmentApi appointmentApi;
    private CenterApi centerApi;
    private CustomerApi customerApi;
    private TokenHelper tokenHelper;

    public static BookingFragment newInstance(String vehicleId) {
        BookingFragment fragment = new BookingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VEHICLE_ID, vehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleId = getArguments().getString(ARG_VEHICLE_ID);
        }
        appointmentApi = AppointmentRetrofitClient.getInstance().getAppointmentApi();
        centerApi = CenterRetrofitClient.getInstance().getCenterApi();
        customerApi = CustomerRetrofitClient.getInstance().getCustomerApi();
        tokenHelper = new TokenHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Mapping UI
        centerContainer = view.findViewById(R.id.centerContainer);
        tvDate = view.findViewById(R.id.tvDate);
        tvTime = view.findViewById(R.id.tvTime);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        progressBar = view.findViewById(R.id.progressBar);

        // Load service centers
        loadCenters();

        // Select date
        tvDate.setOnClickListener(v -> showDatePicker());

        // Select time
        tvTime.setOnClickListener(v -> showTimePicker());

        // Confirm booking
        btnConfirm.setOnClickListener(v -> createAppointment());

        return view;
    }

    // ================== Load service centers ==================
    private void loadCenters() {
        progressBar.setVisibility(View.VISIBLE);
        tokenHelper.getTokenAndExecute(token -> {
            centerApi.getCenters("Bearer " + token, null, 1, 10)
                    .enqueue(new Callback<CentersResponse>() {
                        @Override
                        public void onResponse(Call<CentersResponse> call, Response<CentersResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                CentersResponse.CentersData data = response.body().getData();
                                if (data != null && data.getCenters() != null) {
                                    setupCenterButtons(data.getCenters());
                                } else {
                                    Toast.makeText(getContext(), "Không có trung tâm nào khả dụng", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Không tải được danh sách trung tâm", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CentersResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ================== Setup dynamic center buttons ==================
    private void setupCenterButtons(List<CenterModel> centers) {
        centerContainer.removeAllViews();

        for (CenterModel c : centers) {
            Button btn = new Button(requireContext());
            btn.setText(c.getName());
            btn.setAllCaps(false);
            btn.setTextColor(getResources().getColor(android.R.color.black));
            btn.setBackgroundResource(R.drawable.bg_button_unselected);
            btn.setPadding(16, 16, 16, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                selectedCenterId = c.getId();
                highlightSelectedButton(btn);
            });

            centerContainer.addView(btn);
        }
    }

    // ================== Highlight selected center ==================
    private void highlightSelectedButton(Button selected) {
        for (int i = 0; i < centerContainer.getChildCount(); i++) {
            View child = centerContainer.getChildAt(i);
            if (child instanceof Button) {
                child.setBackgroundResource(child == selected
                        ? R.drawable.bg_button_selected
                        : R.drawable.bg_button_unselected);
            }
        }
    }

    // ================== Date picker ==================
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    tvDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // ================== Time picker ==================
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                (view, hour, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    tvTime.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    // ================== Create Appointment ==================
    private void createAppointment() {
        if (selectedCenterId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn trung tâm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày và giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnConfirm.setEnabled(false);

        // Tạo thời gian ISO
        String startTime = selectedDate + "T" + selectedTime + ":00.000Z";
        String[] parts = selectedTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int endHour = (hour + 2) % 24;
        String endTime = String.format(Locale.getDefault(), "%sT%02d:%02d:00.000Z", selectedDate, endHour, minute);

        // Bước 1️⃣: Lấy userId từ /auth/profile
        tokenHelper.getUserIdFromProfile(userId -> {
            if (userId == null) {
                progressBar.setVisibility(View.GONE);
                btnConfirm.setEnabled(true);
                Toast.makeText(getContext(), "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }

            // Bước 2️⃣: Lấy customerId bằng userId
            tokenHelper.getTokenAndExecute(token -> {
                customerApi.getCustomerByUserId("Bearer " + token, userId)
                        .enqueue(new Callback<CustomerResponse>() {
                            @Override
                            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    String customerId = response.body().getData().getId();

                                    // Bước 3️⃣: Gửi request tạo appointment
                                    AppointmentRequest request = new AppointmentRequest(
                                            null,
                                            customerId,
                                            vehicleId,
                                            selectedCenterId,
                                            startTime,
                                            endTime,
                                            "pending"
                                    );

                                    appointmentApi.createAppointment("Bearer " + token, request)
                                            .enqueue(new Callback<BaseResponse>() {
                                                @Override
                                                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                                                    progressBar.setVisibility(View.GONE);
                                                    btnConfirm.setEnabled(true);
                                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                                        Toast.makeText(getContext(), "✅ Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                                                        requireActivity().onBackPressed();
                                                    } else {
                                                        Toast.makeText(getContext(), "❌ Không thể tạo lịch hẹn", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<BaseResponse> call, Throwable t) {
                                                    progressBar.setVisibility(View.GONE);
                                                    btnConfirm.setEnabled(true);
                                                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    btnConfirm.setEnabled(true);
                                    Toast.makeText(getContext(), "Không tìm thấy thông tin khách hàng", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                btnConfirm.setEnabled(true);
                                Toast.makeText(getContext(), "Lỗi khi lấy customerId: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
            return null;
        });
    }
}
