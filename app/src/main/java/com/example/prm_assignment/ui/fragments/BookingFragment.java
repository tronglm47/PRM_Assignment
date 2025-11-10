package com.example.prm_assignment.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.AppointmentRequest;
import com.example.prm_assignment.data.model.BaseResponse;
import com.example.prm_assignment.data.model.CenterModel;
import com.example.prm_assignment.data.model.CentersResponse;
import com.example.prm_assignment.data.model.CustomerResponse;
import com.example.prm_assignment.data.model.SlotModel;
import com.example.prm_assignment.data.model.SlotsResponse;
import com.example.prm_assignment.data.remote.AppointmentApi;
import com.example.prm_assignment.data.remote.AppointmentRetrofitClient;
import com.example.prm_assignment.data.remote.CenterApi;
import com.example.prm_assignment.data.remote.CenterRetrofitClient;
import com.example.prm_assignment.data.remote.CustomerApi;
import com.example.prm_assignment.data.remote.CustomerRetrofitClient;
import com.example.prm_assignment.data.remote.SlotApi;
import com.example.prm_assignment.data.remote.SlotRetrofitClient;

import java.text.SimpleDateFormat;
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
    private LinearLayout centerContainer, slotContainer;
    private TextView tvDate, tvNoSlots;
    private Button btnConfirm;
    private ProgressBar progressBar;

    private String selectedCenterId;
    private String selectedDate;
    private String selectedSlotId;
    private SlotModel selectedSlot;

    private AppointmentApi appointmentApi;
    private CenterApi centerApi;
    private CustomerApi customerApi;
    private SlotApi slotApi;
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
        slotApi = SlotRetrofitClient.getInstance().getSlotApi();
        tokenHelper = new TokenHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Mapping UI
        centerContainer = view.findViewById(R.id.centerContainer);
        slotContainer = view.findViewById(R.id.slotContainer);
        tvDate = view.findViewById(R.id.tvDate);
        tvNoSlots = view.findViewById(R.id.tvNoSlots);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        progressBar = view.findViewById(R.id.progressBar);

        // Load service centers
        loadCenters();

        // Select date
        tvDate.setOnClickListener(v -> showDatePicker());

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

    // ================== Setup dynamic center buttons with images ==================
    private void setupCenterButtons(List<CenterModel> centers) {
        centerContainer.removeAllViews();

        for (CenterModel c : centers) {
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_center, centerContainer, false);

            ImageView ivCenterImage = itemView.findViewById(R.id.ivCenterImage);
            TextView tvCenterName = itemView.findViewById(R.id.tvCenterName);
            TextView tvCenterAddress = itemView.findViewById(R.id.tvCenterAddress);
            TextView tvCenterPhone = itemView.findViewById(R.id.tvCenterPhone);

            tvCenterName.setText(c.getName());
            tvCenterAddress.setText(c.getAddress());
            tvCenterPhone.setText(c.getPhone());

            // Load image using Glide
            if (c.getImage() != null && !c.getImage().isEmpty()) {
                Glide.with(requireContext())
                        .load(c.getImage())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(ivCenterImage);
            }

            itemView.setOnClickListener(v -> {
                selectedCenterId = c.getId();
                highlightSelectedCenter(itemView);

                // Load slots when center is selected
                if (selectedDate != null) {
                    loadSlots();
                }
            });

            centerContainer.addView(itemView);
        }
    }

    // ================== Highlight selected center ==================
    private void highlightSelectedCenter(View selected) {
        for (int i = 0; i < centerContainer.getChildCount(); i++) {
            View child = centerContainer.getChildAt(i);
            child.setBackgroundResource(child == selected
                    ? R.drawable.bg_button_selected
                    : R.drawable.bg_button_unselected);
        }
    }

    // ================== Date picker ==================
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 7);
        long maxDate = calendar.getTimeInMillis();

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, day);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(selectedCal.getTime());

                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDate.setText(displayFormat.format(selectedCal.getTime()));

                    // Load slots when date is selected
                    if (selectedCenterId != null) {
                        loadSlots();
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(today);
        dialog.getDatePicker().setMaxDate(maxDate);
        dialog.show();
    }

    // ================== Load slots ==================
    private void loadSlots() {
        if (selectedCenterId == null || selectedDate == null) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        slotContainer.removeAllViews();
        tvNoSlots.setVisibility(View.GONE);
        selectedSlotId = null;
        selectedSlot = null;

        tokenHelper.getTokenAndExecute(token -> {
            slotApi.getSlots("Bearer " + token, selectedCenterId, selectedDate)
                    .enqueue(new Callback<SlotsResponse>() {
                        @Override
                        public void onResponse(Call<SlotsResponse> call, Response<SlotsResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                List<SlotModel> slots = response.body().getData();
                                if (slots != null && !slots.isEmpty()) {
                                    setupSlotButtons(slots);
                                    tvNoSlots.setVisibility(View.GONE);
                                } else {
                                    tvNoSlots.setText("Không có khung giờ nào khả dụng cho ngày này");
                                    tvNoSlots.setVisibility(View.VISIBLE);
                                }
                            } else {
                                tvNoSlots.setText("Không thể tải danh sách khung giờ");
                                tvNoSlots.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<SlotsResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            tvNoSlots.setText("Lỗi mạng: " + t.getMessage());
                            tvNoSlots.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }

    // ================== Setup slot buttons ==================
    private void setupSlotButtons(List<SlotModel> slots) {
        slotContainer.removeAllViews();

        for (SlotModel slot : slots) {
            Button btn = new Button(requireContext());

            String slotText = slot.getStartTime() + " - " + slot.getEndTime();

            if (!slot.isAvailable()) {
                slotText += " (Đã đầy)";
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
            }

            btn.setText(slotText);
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
                selectedSlotId = slot.getId();
                selectedSlot = slot;
                highlightSelectedSlot(btn);
            });

            slotContainer.addView(btn);
        }
    }

    // ================== Highlight selected slot ==================
    private void highlightSelectedSlot(Button selected) {
        for (int i = 0; i < slotContainer.getChildCount(); i++) {
            View child = slotContainer.getChildAt(i);
            if (child instanceof Button && child.isEnabled()) {
                child.setBackgroundResource(child == selected
                        ? R.drawable.bg_button_selected
                        : R.drawable.bg_button_unselected);
            }
        }
    }

    // ================== Create Appointment ==================
    private void createAppointment() {
        if (selectedCenterId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn trung tâm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSlotId == null || selectedSlot == null) {
            Toast.makeText(getContext(), "Vui lòng chọn khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnConfirm.setEnabled(false);

        // Tạo thời gian ISO từ slot
        String startTime = selectedDate + "T" + selectedSlot.getStartTime() + ":00.000Z";
        String endTime = selectedDate + "T" + selectedSlot.getEndTime() + ":00.000Z";

        // Bước 1️⃣: Lấy userId từ /auth/profile
        tokenHelper.getUserIdFromProfile(userId -> {
            if (userId == null) {
                progressBar.setVisibility(View.GONE);
                btnConfirm.setEnabled(true);
                Toast.makeText(getContext(), "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Bước 2️⃣: Lấy customerId bằng userId
            tokenHelper.getTokenAndExecute(token -> {
                customerApi.getCustomerByUserId("Bearer " + token, userId)
                        .enqueue(new Callback<CustomerResponse>() {
                            @Override
                            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    String customerId = response.body().getData().getId();

                                    // Bước 3️⃣: Gửi request tạo appointment với slot_id
                                    AppointmentRequest request = new AppointmentRequest(
                                            null,
                                            customerId,
                                            vehicleId,
                                            selectedCenterId,
                                            selectedSlotId,
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
