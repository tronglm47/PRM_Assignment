package com.example.prm_assignment.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.ServiceChecklistResponse;
import com.example.prm_assignment.data.model.ServiceRecordResponse;
import com.example.prm_assignment.data.remote.ServiceRecordRetrofitClient;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentDetail";

    private TextView tvAppointmentDate, tvAppointmentTime, tvVehicleInfo, tvCenterInfo, tvStatus;
    private TextView tvRecordStatus, tvTotalCost, tvRecordNotes, tvEmptyChecklist;
    private CardView cardServiceRecord, cardChecklist;
    private LinearLayout llChecklistContainer;
    private FrameLayout loadingOverlay;
    private ImageView btnBack;

    private TokenHelper tokenHelper;
    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        tokenHelper = new TokenHelper(this);

        // Get appointment data from intent
        appointmentId = getIntent().getStringExtra("appointment_id");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String vehicle = getIntent().getStringExtra("vehicle");
        String center = getIntent().getStringExtra("center");
        String status = getIntent().getStringExtra("status");

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);
        tvCenterInfo = findViewById(R.id.tvCenterInfo);
        tvStatus = findViewById(R.id.tvStatus);

        cardServiceRecord = findViewById(R.id.cardServiceRecord);
        cardChecklist = findViewById(R.id.cardChecklist);
        tvRecordStatus = findViewById(R.id.tvRecordStatus);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvRecordNotes = findViewById(R.id.tvRecordNotes);
        tvEmptyChecklist = findViewById(R.id.tvEmptyChecklist);

        llChecklistContainer = findViewById(R.id.llChecklistContainer);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Set appointment data
        tvAppointmentDate.setText(date);
        tvAppointmentTime.setText(time);
        tvVehicleInfo.setText(vehicle);
        tvCenterInfo.setText(center);
        tvStatus.setText(getStatusText(status));
        tvStatus.setBackgroundColor(getStatusColor(status));

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Load service record and checklist
        if (appointmentId != null && !appointmentId.isEmpty()) {
            loadServiceRecord();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
        }
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

    private void loadServiceRecord() {
        showLoading();
        tokenHelper.getTokenAndExecute(token -> {
            if (token != null && !token.isEmpty()) {
                fetchServiceRecord("Bearer " + token);
            } else {
                hideLoading();
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchServiceRecord(String authHeader) {
        Log.d(TAG, "Fetching service record for appointment: " + appointmentId);

        ServiceRecordRetrofitClient.getInstance()
                .getServiceRecordApi()
                .getServiceRecordByAppointment(authHeader, appointmentId)
                .enqueue(new Callback<ServiceRecordResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ServiceRecordResponse> call,
                                         @NonNull Response<ServiceRecordResponse> response) {
                        Log.d(TAG, "Service record response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            ServiceRecordResponse recordResponse = response.body();
                            if (recordResponse.isSuccess() && recordResponse.getData() != null
                                    && recordResponse.getData().getRecords() != null
                                    && !recordResponse.getData().getRecords().isEmpty()) {
                                // Get first record
                                ServiceRecordResponse.ServiceRecordData record =
                                        recordResponse.getData().getRecords().get(0);
                                Log.d(TAG, "Service record loaded successfully");
                                displayServiceRecord(record);

                                // Load checklist with record_id
                                String recordId = record.getId();
                                if (recordId != null && !recordId.isEmpty()) {
                                    fetchServiceChecklist(authHeader, recordId);
                                } else {
                                    hideLoading();
                                }
                            } else {
                                Log.w(TAG, "No service record found");
                                hideLoading();
                                // Don't show error, just hide the cards
                            }
                        } else {
                            Log.e(TAG, "Service record fetch failed: " + response.code());
                            hideLoading();
                            try {
                                if (response.errorBody() != null) {
                                    Log.e(TAG, "Error body: " + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServiceRecordResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Service record fetch error: " + t.getMessage(), t);
                        hideLoading();
                        Toast.makeText(AppointmentDetailActivity.this,
                                "Không thể tải hồ sơ bảo dưỡng",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchServiceChecklist(String authHeader, String recordId) {
        Log.d(TAG, "Fetching checklist for record: " + recordId);

        ServiceRecordRetrofitClient.getInstance()
                .getServiceChecklistApi()
                .getServiceChecklists(authHeader, recordId)
                .enqueue(new Callback<ServiceChecklistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ServiceChecklistResponse> call,
                                         @NonNull Response<ServiceChecklistResponse> response) {
                        hideLoading();
                        Log.d(TAG, "Checklist response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            ServiceChecklistResponse checklistResponse = response.body();
                            if (checklistResponse.isSuccess() && checklistResponse.getData() != null
                                    && checklistResponse.getData().getChecklists() != null) {
                                List<ServiceChecklistResponse.ChecklistItem> checklists =
                                        checklistResponse.getData().getChecklists();
                                Log.d(TAG, "Loaded " + checklists.size() + " checklist items");
                                displayChecklist(checklists);
                            } else {
                                Log.w(TAG, "No checklist items found");
                                showEmptyChecklist();
                            }
                        } else {
                            Log.e(TAG, "Checklist fetch failed: " + response.code());
                            showEmptyChecklist();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServiceChecklistResponse> call, @NonNull Throwable t) {
                        hideLoading();
                        Log.e(TAG, "Checklist fetch error: " + t.getMessage(), t);
                        showEmptyChecklist();
                    }
                });
    }

    private void displayServiceRecord(ServiceRecordResponse.ServiceRecordData record) {
        cardServiceRecord.setVisibility(View.VISIBLE);

        // Set status
        String status = record.getStatus();
        tvRecordStatus.setText(getRecordStatusText(status));

        // Set technician name if available
        String technicianName = "N/A";
        if (record.getTechnicianId() != null && record.getTechnicianId().getName() != null) {
            technicianName = record.getTechnicianId().getName();
        }

        // Format start and end time
        String timeRange = "N/A";
        if (record.getStartTime() != null && record.getEndTime() != null) {
            String startTime = formatTime(record.getStartTime());
            String endTime = formatTime(record.getEndTime());
            timeRange = startTime + " - " + endTime;
        }

        // Set total cost to 0 VND (API không có field này)
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedCost = "0 VND";
        tvTotalCost.setText(formattedCost);

        // Set notes/description
        String description = record.getDescription();
        if (description != null && !description.isEmpty()) {
            tvRecordNotes.setText(description);
            tvRecordNotes.setVisibility(View.VISIBLE);
        } else {
            tvRecordNotes.setText("Không có ghi chú");
            tvRecordNotes.setVisibility(View.VISIBLE);
        }
    }

    private void displayChecklist(List<ServiceChecklistResponse.ChecklistItem> items) {
        cardChecklist.setVisibility(View.VISIBLE);
        tvEmptyChecklist.setVisibility(View.GONE);
        llChecklistContainer.removeAllViews();

        for (ServiceChecklistResponse.ChecklistItem item : items) {
            addChecklistItem(item);
        }
    }

    private void addChecklistItem(ServiceChecklistResponse.ChecklistItem item) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_checklist, llChecklistContainer, false);

        CheckBox cbCompleted = itemView.findViewById(R.id.cbCompleted);
        TextView tvItemName = itemView.findViewById(R.id.tvItemName);
        TextView tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
        TextView tvItemNotes = itemView.findViewById(R.id.tvItemNotes);

        // API chỉ trả về name và order, không có status completed
        cbCompleted.setChecked(false); // Default unchecked
        cbCompleted.setVisibility(View.GONE); // Hide checkbox since we don't have completion status

        // Use 'name' field from API
        tvItemName.setText(item.getName());

        // Hide description since API doesn't provide it
        tvItemDescription.setVisibility(View.GONE);

        // Hide notes since API doesn't provide it
        tvItemNotes.setVisibility(View.GONE);

        llChecklistContainer.addView(itemView);
    }

    private void showEmptyChecklist() {
        cardChecklist.setVisibility(View.VISIBLE);
        tvEmptyChecklist.setVisibility(View.VISIBLE);
        llChecklistContainer.setVisibility(View.GONE);
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

    private String getRecordStatusText(String status) {
        if (status == null) return "N/A";
        switch (status.toLowerCase()) {
            case "in_progress":
                return "Đang thực hiện";
            case "completed":
                return "Đã hoàn thành";
            case "pending":
                return "Chờ xử lý";
            default:
                return status;
        }
    }

    private String formatTime(String time) {
        // Giả sử định dạng thời gian ban đầu là "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        String inputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        // Định dạng đầu ra mong muốn
        String outputFormat = "HH:mm";

        SimpleDateFormat sdfInput = new SimpleDateFormat(inputFormat, Locale.getDefault());
        SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormat, Locale.getDefault());
        try {
            Date date = sdfInput.parse(time);
            return sdfOutput.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // Trả về chuỗi gốc nếu có lỗi
        }
    }
}
