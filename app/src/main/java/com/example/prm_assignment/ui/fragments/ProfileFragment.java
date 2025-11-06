package com.example.prm_assignment.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm_assignment.MainActivity;
import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.ProfileResponse;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;
import com.example.prm_assignment.ui.adapters.VehicleImagePagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private ViewPager2 vehicleImagePager;
    private LinearLayout indicatorContainer;
    private MaterialCardView vehicleInfoCard;
//    private ImageView vehicleAvatar;
    private TextView tvVehicleNameProfile;
    private TextView tvMileage;
    private TextView tvLastServiceDate;
    private TextView tvCustomerName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private LinearLayout phoneLayout;
    private LinearLayout addressLayout;
    private MaterialButton btnLogout;

    private List<VehicleModel> vehicles = new ArrayList<>();
    private VehicleImagePagerAdapter pagerAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int currentPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupViewPager();
        loadData();

        return view;
    }

    private void initViews(View view) {
        vehicleImagePager = view.findViewById(R.id.vehicleImagePager);
        indicatorContainer = view.findViewById(R.id.indicatorContainer);
        vehicleInfoCard = view.findViewById(R.id.vehicleInfoCard);
//        vehicleAvatar = view.findViewById(R.id.vehicleAvatar);
        tvVehicleNameProfile = view.findViewById(R.id.tvVehicleNameProfile);
        tvMileage = view.findViewById(R.id.tvMileage);
        tvLastServiceDate = view.findViewById(R.id.tvLastServiceDate);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        phoneLayout = view.findViewById(R.id.phoneLayout);
        addressLayout = view.findViewById(R.id.addressLayout);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupViewPager() {
        pagerAdapter = new VehicleImagePagerAdapter(vehicles, (vehicle, position) -> {
            // Handle image click if needed
        });
        vehicleImagePager.setAdapter(pagerAdapter);

        vehicleImagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateIndicators(position);
                updateVehicleInfo(position);
            }
        });
    }

    private void loadData() {
        loadProfile();
        loadVehicles();
    }

    private void loadProfile() {
        TokenHelper.loadProfileAsync(requireContext(), new TokenHelper.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse response) {
                if (response != null && response.getData() != null) {
                    updateProfileUI(response.getData());
                } else {
                    showError("Failed to load profile");
                }
            }

            @Override
            public void onError(String message) {
                if ("Token not found".equals(message)) {
                    showError("Token not found. Please login again.");
                    logout();
                } else {
                    showError("Error: " + message);
                    Log.e(TAG, "Profile load error: " + message);
                }
            }
        });
    }

    private void loadVehicles() {
        TokenHelper.getAccessTokenAsync(requireContext(), token -> {
            if (token == null) return;

            VehiclesRetrofitClient.getInstance().getVehiclesApi()
                    .getMyVehicles("Bearer " + token)
                    .enqueue(new Callback<VehicleResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<VehicleResponse> call, @NonNull Response<VehicleResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                vehicles.clear();
                                vehicles.addAll(response.body().getData());
                                pagerAdapter.updateVehicles(vehicles);

                                if (!vehicles.isEmpty()) {
                                    setupIndicators();
                                    updateVehicleInfo(0);
                                    startAutoScroll();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<VehicleResponse> call, @NonNull Throwable t) {
                            Log.e(TAG, "Vehicles load error", t);
                        }
                    });
        });
    }

    private void updateProfileUI(com.example.prm_assignment.data.model.ProfileData profile) {
        if (profile == null) return;

        tvCustomerName.setText(profile.getCustomerName() != null ? profile.getCustomerName() : "N/A");
        tvEmail.setText(profile.getUserId() != null && profile.getUserId().getEmail() != null
                ? profile.getUserId().getEmail() : "N/A");

        if (profile.getUserId() != null && profile.getUserId().getPhone() != null && !profile.getUserId().getPhone().isEmpty()) {
            phoneLayout.setVisibility(View.VISIBLE);
            tvPhone.setText(profile.getUserId().getPhone());
        } else {
            phoneLayout.setVisibility(View.GONE);
        }

        if (profile.getAddress() != null && !profile.getAddress().isEmpty()) {
            addressLayout.setVisibility(View.VISIBLE);
            tvAddress.setText(profile.getAddress());
        } else {
            addressLayout.setVisibility(View.GONE);
        }
    }

    private void updateVehicleInfo(int position) {
        if (vehicles.isEmpty() || position >= vehicles.size()) return;

        VehicleModel vehicle = vehicles.get(position);

        // Fade out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(vehicleInfoCard, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.start();

        handler.postDelayed(() -> {
            // Update info
            tvVehicleNameProfile.setText(vehicle.getVehicleName() != null ? vehicle.getVehicleName() : "N/A");
            tvMileage.setText(String.format(Locale.getDefault(), "%.0f km", vehicle.getMileage()));

            String lastServiceDate = vehicle.getLastServiceDate();
            if (lastServiceDate != null && !lastServiceDate.isEmpty()) {
                tvLastServiceDate.setText(formatDate(lastServiceDate));
            } else {
                tvLastServiceDate.setText("Chưa bảo dưỡng");
            }

            // Fade in animation
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(vehicleInfoCard, "alpha", 0f, 1f);
            fadeIn.setDuration(300);
            fadeIn.start();
        }, 300);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void setupIndicators() {
        indicatorContainer.removeAllViews();
        ImageView[] indicators = new ImageView[vehicles.size()];

        for (int i = 0; i < vehicles.size(); i++) {
            indicators[i] = new ImageView(requireContext());
            indicators[i].setImageDrawable(getResources().getDrawable(R.drawable.indicator_inactive, null));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicators[i].setLayoutParams(params);

            indicatorContainer.addView(indicators[i]);
        }

        if (indicators.length > 0) {
            indicators[0].setImageDrawable(getResources().getDrawable(R.drawable.indicator_active, null));
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            ImageView indicator = (ImageView) indicatorContainer.getChildAt(i);
            if (i == position) {
                indicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_active, null));
            } else {
                indicator.setImageDrawable(getResources().getDrawable(R.drawable.indicator_inactive, null));
            }
        }
    }

    private void startAutoScroll() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (vehicles.isEmpty()) return;

                currentPage = (currentPage + 1) % vehicles.size();
                vehicleImagePager.setCurrentItem(currentPage, true);

                handler.postDelayed(this, 3000); // Auto scroll every 3 seconds
            }
        }, 3000);
    }

    private void logout() {
        TokenHelper.clearTokensAsync(requireContext(), () -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}

