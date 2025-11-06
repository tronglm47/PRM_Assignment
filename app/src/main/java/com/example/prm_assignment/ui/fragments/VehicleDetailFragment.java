package com.example.prm_assignment.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.VehicleDetailResponse;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.remote.VehiclesApi;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleDetailFragment extends Fragment {

    private static final String TAG = "VehicleDetailFragment";
    private static final String ARG_VEHICLE_ID = "vehicle_id";

    private String vehicleId;
    private VehiclesApi vehiclesApi;
    private TokenHelper tokenHelper;

    // UI Components
    private ImageView ivVehicleImage;
    private ProgressBar imageProgressBar;
    private TextView tvVehicleName;
    private TextView tvModel;
    private TextView tvYear;
    private TextView tvVin;
    private TextView tvPlateNumber;
    private TextView tvMileage;
    private TextView tvPrice;
    private TextView tvLastServiceDate;
    private TextView tvLastAlertMileage;
    private TextView tvOwnerName;
    private TextView tvOwnerAddress;
    private CardView btnBookService;
    private ProgressBar progressBar;
    private TextView tvError;
    private ImageButton btnBack;

    public static VehicleDetailFragment newInstance(String vehicleId) {
        VehicleDetailFragment fragment = new VehicleDetailFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle_detail, container, false);

        // Initialize views
        initViews(view);

        // Initialize API client
        vehiclesApi = VehiclesRetrofitClient.getInstance().getVehiclesApi();
        tokenHelper = new TokenHelper(requireContext());

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnBookService.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Book Service feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Fetch vehicle details
        if (vehicleId != null && !vehicleId.isEmpty()) {
            fetchVehicleDetails();
        } else {
            showError("Invalid vehicle ID");
        }

        return view;
    }

    private void initViews(View view) {
        ivVehicleImage = view.findViewById(R.id.ivVehicleImage);
        imageProgressBar = view.findViewById(R.id.imageProgressBar);
        tvVehicleName = view.findViewById(R.id.tvVehicleName);
        tvModel = view.findViewById(R.id.tvModel);
        tvYear = view.findViewById(R.id.tvYear);
        tvVin = view.findViewById(R.id.tvVin);
        tvPlateNumber = view.findViewById(R.id.tvPlateNumber);
        tvMileage = view.findViewById(R.id.tvMileage);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvLastServiceDate = view.findViewById(R.id.tvLastServiceDate);
        tvLastAlertMileage = view.findViewById(R.id.tvLastAlertMileage);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvOwnerAddress = view.findViewById(R.id.tvOwnerAddress);
        btnBookService = view.findViewById(R.id.btnBookService);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void fetchVehicleDetails() {
        showLoading(true);

        tokenHelper.getTokenAndExecute(new TokenHelper.TokenCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                if (token != null && !token.isEmpty()) {
                    makeApiCall("Bearer " + token);
                } else {
                    showError("Please login first");
                }
            }
        });
    }

    private void makeApiCall(String authToken) {
        vehiclesApi.getVehicleById(vehicleId, authToken).enqueue(new Callback<VehicleDetailResponse>() {
            @Override
            public void onResponse(Call<VehicleDetailResponse> call, Response<VehicleDetailResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    VehicleDetailResponse vehicleDetailResponse = response.body();
                    if (vehicleDetailResponse.isSuccess() && vehicleDetailResponse.getData() != null) {
                        displayVehicleDetails(vehicleDetailResponse.getData());
                    } else {
                        showError("Vehicle not found");
                    }
                } else {
                    showError("Failed to fetch vehicle details: " + response.code());
                    Log.e(TAG, "Error response: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<VehicleDetailResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private void displayVehicleDetails(VehicleModel vehicle) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            // Vehicle Name
            String vehicleName = vehicle.getVehicleName() != null ? vehicle.getVehicleName() : vehicle.getModel();
            tvVehicleName.setText(vehicleName);

            // Model
            tvModel.setText(vehicle.getModel());

            // Year
            tvYear.setText(String.valueOf(vehicle.getYear()));

            // VIN
            tvVin.setText(vehicle.getVin());

            // Plate Number
            tvPlateNumber.setText(vehicle.getPlateNumber());

            // Mileage
            String mileage = String.format(Locale.getDefault(), "%.0f km", vehicle.getMileage());
            tvMileage.setText(mileage);

            // Price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String price = currencyFormat.format(vehicle.getPrice());
            tvPrice.setText(price);

            // Last Service Date
            String formattedDate = formatDate(vehicle.getLastServiceDate());
            tvLastServiceDate.setText(formattedDate);

            // Last Alert Mileage
            String lastAlertMileage = String.format(Locale.getDefault(), "%.0f km", vehicle.getLastAlertMileage());
            tvLastAlertMileage.setText(lastAlertMileage);

            // Owner Information
            if (vehicle.getCustomerId() != null) {
                tvOwnerName.setText(vehicle.getCustomerId().getCustomerName());
                tvOwnerAddress.setText(vehicle.getCustomerId().getAddress());
            } else {
                tvOwnerName.setText("Unknown");
                tvOwnerAddress.setText("N/A");
            }

            // Load Vehicle Image
            if (vehicle.getImage() != null && !vehicle.getImage().isEmpty()) {
                loadImage(vehicle.getImage());
            }

            // Hide error message
            tvError.setVisibility(View.GONE);
        });
    }

    private void loadImage(String imageUrl) {
        imageProgressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        imageProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        imageProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivVehicleImage);
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return dateString;
        }
    }

    private void showLoading(boolean isLoading) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                tvError.setVisibility(View.GONE);
            }
        });
    }

    private void showError(String message) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}

