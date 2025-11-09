package com.example.prm_assignment.ui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.CreateSubscriptionRequest;
import com.example.prm_assignment.data.model.PackageModel;
import com.example.prm_assignment.data.model.PackagesResponse;
import com.example.prm_assignment.data.model.PaymentRequest;
import com.example.prm_assignment.data.model.PaymentResponse;
import com.example.prm_assignment.data.model.ProfileResponse;
import com.example.prm_assignment.data.model.SingleSubscriptionResponse;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import com.example.prm_assignment.data.remote.PackagesApi;
import com.example.prm_assignment.data.remote.PackagesRetrofitClient;
import com.example.prm_assignment.data.remote.PaymentApi;
import com.example.prm_assignment.data.remote.PaymentRetrofitClient;
import com.example.prm_assignment.data.remote.ProfileApi;
import com.example.prm_assignment.data.remote.ProfileRetrofitClient;
import com.example.prm_assignment.data.remote.VehiclesApi;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;
import com.example.prm_assignment.data.remote.VehicleSubscriptionApi;
import com.example.prm_assignment.data.remote.VehicleSubscriptionRetrofitClient;
import com.example.prm_assignment.data.repository.VehicleSubscriptionRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private float dX, dY;
    private int lastAction;

    private TextView tvGreeting;
    private TextView tvCountry;
    private LinearLayout llPackagesContainer;
    private LinearLayout llVehicleSubscriptionsContainer;
    private FrameLayout loadingOverlay;
    private TokenHelper tokenHelper;
    private PackagesApi packagesApi;
    private ProfileApi profileApi;
    private VehiclesApi vehiclesApi;
    private VehicleSubscriptionApi vehicleSubscriptionApi;
    private PaymentApi paymentApi;
    private List<VehicleModel> vehiclesList = new ArrayList<>();

    // Track loading states
    private boolean profileLoaded = false;
    private boolean packagesLoaded = false;
    private boolean vehicleSubscriptionsLoaded = false;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize TokenHelper and API
        tokenHelper = new TokenHelper(requireContext());
        packagesApi = PackagesRetrofitClient.getInstance().getPackagesApi();
        profileApi = ProfileRetrofitClient.getInstance().getProfileApi();
        vehiclesApi = VehiclesRetrofitClient.getInstance().getVehiclesApi();
        vehicleSubscriptionApi = VehicleSubscriptionRetrofitClient.getInstance().getVehicleSubscriptionApi();
        paymentApi = PaymentRetrofitClient.getInstance().getPaymentApi();

        // Initialize views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvCountry = view.findViewById(R.id.tvCountry);
        llPackagesContainer = view.findViewById(R.id.llPackagesContainer);
        llVehicleSubscriptionsContainer = view.findViewById(R.id.llVehicleSubscriptionsContainer);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Check if views are found
        if (tvGreeting == null) {
            Log.e(TAG, "tvGreeting is null!");
        }
        if (tvCountry == null) {
            Log.e(TAG, "tvCountry is null!");
        }
        if (llPackagesContainer == null) {
            Log.e(TAG, "llPackagesContainer is null!");
        } else {
            Log.d(TAG, "llPackagesContainer found");
        }

        // Show loading at start
        showLoading();

        // Initialize chat bubble
        CardView chatBubble = view.findViewById(R.id.chatBubble);
        if (chatBubble == null) {
            Log.w(TAG, "chatBubble not found in layout");
        } else {
            chatBubble.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        // Scale down slightly when pressed for visual feedback
                        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate new position
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Get parent dimensions for boundary checking
                        ViewGroup parent = (ViewGroup) v.getParent();
                        if (parent != null) {
                            // Constrain X within parent bounds
                            float maxX = parent.getWidth() - v.getWidth();
                            newX = Math.max(0, Math.min(newX, maxX));

                            // Constrain Y within parent bounds
                            float maxY = parent.getHeight() - v.getHeight();
                            newY = Math.max(0, Math.min(newY, maxY));
                        }

                        // Apply smooth movement with animation
                        v.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start();

                        lastAction = MotionEvent.ACTION_MOVE;
                        break;

                    case MotionEvent.ACTION_UP:
                        // Scale back to normal
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();

                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            // This was a click, not a drag
                            v.performClick();
                            Toast.makeText(getContext(), "Chat đang được phát triển", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        return false;
                }
                return true;
            });
        }

        // Load data from API
        loadProfileData();
        loadPackagesData();
        loadVehicleSubscriptions();

        return view;
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading overlay shown");
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            Log.d(TAG, "Loading overlay hidden");
        }
    }

    private void checkAndHideLoading() {
        // Hide loading only when all data is loaded
        if (profileLoaded && packagesLoaded && vehicleSubscriptionsLoaded) {
            hideLoading();
            Log.d(TAG, "All data loaded, hiding loading overlay");
        }
    }

    private void addPackageCard(PackageModel packageModel) {
        if (llPackagesContainer == null) {
            Log.e(TAG, "llPackagesContainer is null, cannot add card");
            return;
        }

        // Inflate the package card layout
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_package, llPackagesContainer, false);

        // Find views in the card
        TextView tvPackageName = cardView.findViewById(R.id.tvPackageName);
        TextView tvPackagePrice = cardView.findViewById(R.id.tvPackagePrice);
        CardView btnBookNow = cardView.findViewById(R.id.btnBookNow);

        // Set data
        tvPackageName.setText(packageModel.getName());

        // Format price
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(packageModel.getPrice()) + " VND";
        tvPackagePrice.setText(formattedPrice);

        // Set click listener
        btnBookNow.setOnClickListener(v -> {
            showPaymentDialog(packageModel);
        });

        cardView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Selected: " + packageModel.getName(), Toast.LENGTH_SHORT).show();
        });

        // Add card to container
        llPackagesContainer.addView(cardView);
    }

    private void loadProfileData() {
        Log.d(TAG, "loadProfileData: Starting to load profile");
        profileLoaded = false;
        tokenHelper.getTokenAndExecute(token -> {
            Log.d(TAG, "Token retrieved: " + (token != null ? "Yes (length=" + token.length() + ")" : "No"));
            if (token != null && !token.isEmpty()) {
                fetchProfile("Bearer " + token);
            } else {
                Log.e(TAG, "No token found");
                if (tvGreeting != null) {
                    tvGreeting.setText("Xin chào, Khách");
                }
                // Mark as loaded even if failed
                profileLoaded = true;
                checkAndHideLoading();
            }
        });
    }

    private void fetchProfile(String authHeader) {
        Log.d(TAG, "fetchProfile: Making API call");
        profileApi.getProfile(authHeader).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                Log.d(TAG, "Profile API response received: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess() && profileResponse.getData() != null) {
                        String customerName = profileResponse.getData().getCustomerName();
                        String address = profileResponse.getData().getAddress();
                        Log.d(TAG, "Customer name from API: " + customerName);
                        Log.d(TAG, "Address from API: " + address);

                        if (tvGreeting != null) {
                            if (customerName != null && !customerName.isEmpty()) {
                                tvGreeting.setText("Xin chào, " + customerName);
                            } else {
                                tvGreeting.setText("Xin chào, Khách hàng");
                            }
                        }

                        if (tvCountry != null) {
                            if (address != null && !address.isEmpty()) {
                                tvCountry.setText(address);
                            } else {
                                tvCountry.setText("Việt Nam");
                            }
                        }
                    } else {
                        Log.w(TAG, "Profile response unsuccessful or data is null");
                        if (tvGreeting != null) {
                            tvGreeting.setText("Xin chào, Khách hàng");
                        }
                        if (tvCountry != null) {
                            tvCountry.setText("Việt Nam");
                        }
                    }
                } else {
                    Log.e(TAG, "Profile fetch failed: " + response.code());
                    if (tvGreeting != null) {
                        tvGreeting.setText("Xin chào, Khách hàng");
                    }
                    if (tvCountry != null) {
                        tvCountry.setText("Việt Nam");
                    }
                }

                // Mark profile as loaded
                profileLoaded = true;
                checkAndHideLoading();
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Profile fetch error: " + t.getMessage(), t);
                if (tvGreeting != null) {
                    tvGreeting.setText("Xin chào, Khách hàng");
                }
                if (tvCountry != null) {
                    tvCountry.setText("Việt Nam");
                }

                // Mark profile as loaded even on failure
                profileLoaded = true;
                checkAndHideLoading();
            }
        });
    }

    private void loadPackagesData() {
        Log.d(TAG, "loadPackagesData: Starting to load packages");
        packagesLoaded = false;
        tokenHelper.getTokenAndExecute(token -> {
            Log.d(TAG, "Token retrieved for packages: " + (token != null ? "Yes" : "No"));
            if (token != null && !token.isEmpty()) {
                Log.d(TAG, "Calling fetchPackages with token");
                fetchPackages("Bearer " + token);
            } else {
                Log.e(TAG, "No token found for packages");
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                // Mark as loaded even if failed
                packagesLoaded = true;
                checkAndHideLoading();
            }
        });
    }

    private void fetchPackages(String authHeader) {
        Log.d(TAG, "fetchPackages: Making API call");
        Log.d(TAG, "Auth header: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        Log.d(TAG, "Base URL: " + com.example.prm_assignment.BuildConfig.BASE_URL);

        packagesApi.getPackages(authHeader).enqueue(new Callback<PackagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PackagesResponse> call, @NonNull Response<PackagesResponse> response) {
                Log.d(TAG, "Packages API response received");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    PackagesResponse packagesResponse = response.body();
                    Log.d(TAG, "Response successful: " + packagesResponse.isSuccess());
                    if (packagesResponse.isSuccess() && packagesResponse.getData() != null) {
                        Log.d(TAG, "Loaded " + packagesResponse.getData().size() + " packages");

                        // Clear existing cards
                        if (llPackagesContainer != null) {
                            llPackagesContainer.removeAllViews();
                        }

                        // Add each package as a card
                        for (PackageModel packageModel : packagesResponse.getData()) {
                            addPackageCard(packageModel);
                        }

//                        Toast.makeText(getContext(), "Đã tải " + packagesResponse.getData().size() + " gói dịch vụ", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Response body empty or unsuccessful");
                    }
                } else {
                    Log.e(TAG, "Packages fetch failed: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Không thể tải gói dịch vụ: " + response.code(), Toast.LENGTH_SHORT).show();
                }

                // Mark packages as loaded
                packagesLoaded = true;
                checkAndHideLoading();
            }

            @Override
            public void onFailure(@NonNull Call<PackagesResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Packages fetch error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Mark packages as loaded even on failure
                packagesLoaded = true;
                checkAndHideLoading();
            }
        });
    }

    private void loadVehicleSubscriptions() {
        Log.d(TAG, "loadVehicleSubscriptions: Starting to load vehicle subscriptions");
        vehicleSubscriptionsLoaded = false;

        // First, get the customer ID from the profile (through userId -> customerId)
        tokenHelper.getCustomerIdFromProfile(new TokenHelper.CustomerIdCallback() {
            @Override
            public void onCustomerIdRetrieved(String customerId) {
                Log.d(TAG, "Retrieved customerId: " + customerId);
                if (customerId != null && !customerId.isEmpty()) {
                    // Get token and fetch vehicle subscriptions
                    tokenHelper.getTokenAndExecute(new TokenHelper.TokenCallback() {
                        @Override
                        public void onTokenRetrieved(String token) {
                            if (token != null && !token.isEmpty()) {
                                fetchVehicleSubscriptions("Bearer " + token, customerId);
                            } else {
                                Log.e(TAG, "No token found for vehicle subscriptions");
                                vehicleSubscriptionsLoaded = true;
                                checkAndHideLoading();
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "No customerId found, cannot load vehicle subscriptions");
                    vehicleSubscriptionsLoaded = true;
                    checkAndHideLoading();
                }
            }
        });
    }

    private void fetchVehicleSubscriptions(String authHeader, String customerId) {
        Log.d(TAG, "fetchVehicleSubscriptions: Making API call for customerId: " + customerId);

        com.example.prm_assignment.data.remote.VehicleSubscriptionRetrofitClient
                .getInstance()
                .getVehicleSubscriptionApi()
                .getCustomerSubscriptions(authHeader, customerId)
                .enqueue(new Callback<com.example.prm_assignment.data.model.VehicleSubscriptionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.prm_assignment.data.model.VehicleSubscriptionResponse> call,
                                           @NonNull Response<com.example.prm_assignment.data.model.VehicleSubscriptionResponse> response) {
                        Log.d(TAG, "Vehicle subscriptions API response received: " + response.code());
                        Log.d(TAG, "Request URL: " + call.request().url());

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.prm_assignment.data.model.VehicleSubscriptionResponse subscriptionResponse = response.body();
                            if (subscriptionResponse.isSuccess() && subscriptionResponse.getData() != null) {
                                Log.d(TAG, "Loaded " + subscriptionResponse.getData().size() + " vehicle subscriptions");

                                // Clear existing cards
                                if (llVehicleSubscriptionsContainer != null) {
                                    llVehicleSubscriptionsContainer.removeAllViews();
                                }

                                // Add each subscription as a card
                                for (com.example.prm_assignment.data.model.VehicleSubscriptionResponse.VehicleSubscription subscription : subscriptionResponse.getData()) {
                                    addVehicleSubscriptionCard(subscription);
                                }
                            } else {
                                Log.w(TAG, "Vehicle subscriptions response unsuccessful or data is null");
                            }
                        } else {
                            Log.e(TAG, "Vehicle subscriptions fetch failed: " + response.code());
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error body: " + errorBody);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                        }

                        vehicleSubscriptionsLoaded = true;
                        checkAndHideLoading();
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.prm_assignment.data.model.VehicleSubscriptionResponse> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Vehicle subscriptions fetch error: " + t.getMessage(), t);
                        Log.e(TAG, "Error type: " + t.getClass().getName());
                        if (t.getCause() != null) {
                            Log.e(TAG, "Cause: " + t.getCause().getMessage());
                        }

                        vehicleSubscriptionsLoaded = true;
                        checkAndHideLoading();

                        // Show user-friendly error message
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                    "Không thể tải thông tin gói xe. Vui lòng thử lại sau.",
                                    Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
    }

    private void addVehicleSubscriptionCard(com.example.prm_assignment.data.model.VehicleSubscriptionResponse.VehicleSubscription subscription) {
        if (llVehicleSubscriptionsContainer == null) {
            Log.e(TAG, "llVehicleSubscriptionsContainer is null, cannot add card");
            return;
        }

        // Inflate the vehicle subscription card layout
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_vehicle_subscription, llVehicleSubscriptionsContainer, false);

        // Find views in the card
        ImageView ivVehicleImage = cardView.findViewById(R.id.ivVehicleImage);
        TextView tvVehicleName = cardView.findViewById(R.id.tvVehicleName);
        TextView tvVehicleModel = cardView.findViewById(R.id.tvVehicleModel);
        TextView tvPackageName = cardView.findViewById(R.id.tvPackageName);
        TextView tvSubscriptionStatus = cardView.findViewById(R.id.tvSubscriptionStatus);
        TextView tvKmInterval = cardView.findViewById(R.id.tvKmInterval);
        TextView tvDateRange = cardView.findViewById(R.id.tvDateRange);
        ProgressBar progressBar = cardView.findViewById(R.id.progressBar);

        // Set vehicle data
        if (subscription.getVehicleInfo() != null) {
            // Load vehicle image using Glide
            if (subscription.getVehicleInfo().getImageUrl() != null && !subscription.getVehicleInfo().getImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(subscription.getVehicleInfo().getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_directions)
                        .error(android.R.drawable.ic_menu_directions)
                        .centerCrop()
                        .into(ivVehicleImage);
                // Remove tint when showing real image
                ivVehicleImage.setImageTintList(null);
            } else {
                // Use default icon with tint
                ivVehicleImage.setImageResource(android.R.drawable.ic_menu_directions);
                ivVehicleImage.setImageTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_green_dark, null)));
            }

            tvVehicleName.setText(subscription.getVehicleInfo().getVehicleName() != null
                    ? subscription.getVehicleInfo().getVehicleName() : "N/A");
            tvVehicleModel.setText("Model: " + (subscription.getVehicleInfo().getModel() != null
                    ? subscription.getVehicleInfo().getModel() : "N/A"));
        }

        // Set package data
        if (subscription.getPackageInfo() != null) {
            tvPackageName.setText(subscription.getPackageInfo().getName() != null
                    ? subscription.getPackageInfo().getName() : "N/A");
            tvKmInterval.setText(String.format(Locale.getDefault(), "%,d km",
                    subscription.getPackageInfo().getKmInterval()));
        }

        // Calculate days remaining
        if (subscription.getEndDate() != null) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date endDate = sdf.parse(subscription.getEndDate());
                java.util.Date now = new java.util.Date();

                if (endDate != null) {
                    long diffInMillis = endDate.getTime() - now.getTime();
                    long daysRemaining = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis);

                    if (daysRemaining > 0) {
                        tvSubscriptionStatus.setText(String.format(Locale.getDefault(), "Còn %d ngày", daysRemaining));

                        // Set progress and color based on days remaining
                        int totalDays = subscription.getPackageInfo() != null ? subscription.getPackageInfo().getDuration() : 30;
                        int progress = (int) ((daysRemaining * 100) / totalDays);
                        progressBar.setProgress(Math.min(100, Math.max(0, progress)));

                        // Change color based on urgency
                        if (daysRemaining <= 7) {
                            tvSubscriptionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                                    getResources().getColor(android.R.color.holo_red_dark, null)));
                        } else if (daysRemaining <= 15) {
                            tvSubscriptionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                                    getResources().getColor(android.R.color.holo_orange_dark, null)));
                        } else {
                            tvSubscriptionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                                    getResources().getColor(android.R.color.holo_green_dark, null)));
                        }
                    } else {
                        tvSubscriptionStatus.setText("Hết hạn");
                        tvSubscriptionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                        progressBar.setProgress(0);
                    }
                }
            } catch (java.text.ParseException e) {
                Log.e(TAG, "Error parsing end date", e);
                tvSubscriptionStatus.setText("N/A");
            }
        }

        // Format and display date range
        if (subscription.getStartDate() != null && subscription.getEndDate() != null) {
            try {
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                java.util.Date startDate = inputFormat.parse(subscription.getStartDate());
                java.util.Date endDate = inputFormat.parse(subscription.getEndDate());

                if (startDate != null && endDate != null) {
                    String dateRange = outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
                    tvDateRange.setText(dateRange);
                }
            } catch (java.text.ParseException e) {
                Log.e(TAG, "Error parsing dates", e);
                tvDateRange.setText("N/A");
            }
        }

        // Add click listener
        cardView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chi tiết: " + subscription.getVehicleInfo().getVehicleName(), Toast.LENGTH_SHORT).show();
        });

        // Add card to container
        llVehicleSubscriptionsContainer.addView(cardView);
    }

    private void showPaymentDialog(PackageModel packageModel) {
        // Inflate dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payment_package, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Initialize dialog views
        TextView tvPackageNameDialog = dialogView.findViewById(R.id.tvPackageNameDialog);
        TextView tvPackageDescription = dialogView.findViewById(R.id.tvPackageDescription);
        TextView tvPackageDuration = dialogView.findViewById(R.id.tvPackageDuration);
        TextView tvPackageKmInterval = dialogView.findViewById(R.id.tvPackageKmInterval);
        TextView tvPackagePriceDialog = dialogView.findViewById(R.id.tvPackagePriceDialog);
        TextView tvTotalPrice = dialogView.findViewById(R.id.tvTotalPrice);
        Spinner spinnerVehicle = dialogView.findViewById(R.id.spinnerVehicle);
        RadioGroup rgPaymentMethod = dialogView.findViewById(R.id.rgPaymentMethod);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirmPayment = dialogView.findViewById(R.id.btnConfirmPayment);
        FrameLayout loadingOverlayDialog = dialogView.findViewById(R.id.loadingOverlayDialog);

        // Set package data
        tvPackageNameDialog.setText(packageModel.getName());
        tvPackageDescription.setText(packageModel.getDescription() != null ? packageModel.getDescription() : "Gói dịch vụ bảo dưỡng xe");
        tvPackageDuration.setText(String.format(Locale.getDefault(), "%d ngày", packageModel.getDuration()));
        tvPackageKmInterval.setText(String.format(Locale.getDefault(), "%,d km", packageModel.getKmInterval()));

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(packageModel.getPrice()) + " VND";
        tvPackagePriceDialog.setText(formattedPrice);
        tvTotalPrice.setText(formattedPrice);

        // Load vehicles
        loadingOverlayDialog.setVisibility(View.VISIBLE);
        loadVehiclesForDialog(spinnerVehicle, loadingOverlayDialog);

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Confirm payment button
        btnConfirmPayment.setOnClickListener(v -> {
            if (vehiclesList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng thêm xe trước khi đăng ký gói", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPosition = spinnerVehicle.getSelectedItemPosition();
            if (selectedPosition < 0 || selectedPosition >= vehiclesList.size()) {
                Toast.makeText(getContext(), "Vui lòng chọn xe", Toast.LENGTH_SHORT).show();
                return;
            }

            VehicleModel selectedVehicle = vehiclesList.get(selectedPosition);
            String paymentMethod = getSelectedPaymentMethod(rgPaymentMethod);

            // Show loading
            loadingOverlayDialog.setVisibility(View.VISIBLE);
            btnConfirmPayment.setEnabled(false);

            // Create subscription
            createSubscription(packageModel, selectedVehicle, paymentMethod, dialog, loadingOverlayDialog, btnConfirmPayment);
        });

        dialog.show();
    }

    private void loadVehiclesForDialog(Spinner spinnerVehicle, FrameLayout loadingOverlay) {
        tokenHelper.getTokenAndExecute(token -> {
            if (token != null && !token.isEmpty()) {
                vehiclesApi.getMyVehicles("Bearer " + token).enqueue(new Callback<VehicleResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VehicleResponse> call, @NonNull Response<VehicleResponse> response) {
                        loadingOverlay.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            VehicleResponse vehicleResponse = response.body();
                            if (vehicleResponse.isSuccess() && vehicleResponse.getData() != null) {
                                vehiclesList.clear();
                                vehiclesList.addAll(vehicleResponse.getData());

                                // Create adapter for spinner
                                List<String> vehicleNames = new ArrayList<>();
                                for (VehicleModel vehicle : vehiclesList) {
                                    vehicleNames.add(vehicle.getVehicleName() + " - " + vehicle.getPlateNumber());
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        getContext(),
                                        android.R.layout.simple_spinner_item,
                                        vehicleNames
                                );
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerVehicle.setAdapter(adapter);
                            } else {
                                Toast.makeText(getContext(), "Không tìm thấy xe nào", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Không thể tải danh sách xe", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VehicleResponse> call, @NonNull Throwable t) {
                        loadingOverlay.setVisibility(View.GONE);
                        Log.e(TAG, "Error loading vehicles: " + t.getMessage(), t);
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedPaymentMethod(RadioGroup rgPaymentMethod) {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCash) {
            return "Tiền mặt";
        } else if (selectedId == R.id.rbMomo) {
            return "Ví MoMo";
        } else if (selectedId == R.id.rbBankTransfer) {
            return "Chuyển khoản ngân hàng";
        }
        return "Tiền mặt";
    }

    private void createSubscription(PackageModel packageModel, VehicleModel vehicle, String paymentMethod,
                                     AlertDialog dialog, FrameLayout loadingOverlay, Button btnConfirmPayment) {
        tokenHelper.getTokenAndExecute(token -> {
            if (token != null && !token.isEmpty()) {
                // Calculate dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                Date startDate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_YEAR, packageModel.getDuration());
                Date endDate = calendar.getTime();

                // Create request
                CreateSubscriptionRequest request = new CreateSubscriptionRequest();
                request.setVehicleId(vehicle.getId());
                request.setPackageId(packageModel.getId());
                request.setStartDate(sdf.format(startDate));
                request.setEndDate(sdf.format(endDate));

                Log.d(TAG, "Creating subscription: vehicleId=" + vehicle.getId() + ", packageId=" + packageModel.getId());

                vehicleSubscriptionApi.createSubscription("Bearer " + token, request)
                        .enqueue(new Callback<SingleSubscriptionResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<SingleSubscriptionResponse> call,
                                                   @NonNull Response<SingleSubscriptionResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    SingleSubscriptionResponse subscriptionResponse = response.body();
                                    if (subscriptionResponse.isSuccess() && subscriptionResponse.getData() != null) {
                                        String subscriptionId = subscriptionResponse.getData().getId();
                                        Log.d(TAG, "Subscription created successfully with ID: " + subscriptionId);

                                        // Now create payment link
                                        createPaymentLink(subscriptionId, packageModel.getPrice(), token, dialog, loadingOverlay, btnConfirmPayment);
                                    } else {
                                        loadingOverlay.setVisibility(View.GONE);
                                        btnConfirmPayment.setEnabled(true);
                                        Toast.makeText(getContext(),
                                                "Lỗi: " + subscriptionResponse.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    loadingOverlay.setVisibility(View.GONE);
                                    btnConfirmPayment.setEnabled(true);
                                    try {
                                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                        Log.e(TAG, "Error creating subscription: " + errorBody);
                                        Toast.makeText(getContext(),
                                                "Không thể tạo gói: " + response.code(),
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error reading error body", e);
                                        Toast.makeText(getContext(),
                                                "Không thể tạo gói",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<SingleSubscriptionResponse> call, @NonNull Throwable t) {
                                loadingOverlay.setVisibility(View.GONE);
                                btnConfirmPayment.setEnabled(true);
                                Log.e(TAG, "Error creating subscription: " + t.getMessage(), t);
                                Toast.makeText(getContext(),
                                        "Lỗi kết nối: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                loadingOverlay.setVisibility(View.GONE);
                btnConfirmPayment.setEnabled(true);
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPaymentLink(String subscriptionId, double amount, String token,
                                   AlertDialog dialog, FrameLayout loadingOverlay, Button btnConfirmPayment) {
        Log.d(TAG, "Creating payment link for subscription: " + subscriptionId);

        // Get customer ID first
        tokenHelper.getCustomerIdFromProfile(new TokenHelper.CustomerIdCallback() {
            @Override
            public void onCustomerIdRetrieved(String customerId) {
                if (customerId != null && !customerId.isEmpty()) {
                    // Create payment request
                    PaymentRequest paymentRequest = new PaymentRequest();
                    paymentRequest.setSubscriptionId(subscriptionId);
                    paymentRequest.setCustomerId(customerId);
                    paymentRequest.setAmount(amount);
                    paymentRequest.setPaymentType("subscription");
                    paymentRequest.setReturnUrl("https://example.com/payment/success");
                    paymentRequest.setCancelUrl("https://example.com/payment/cancel");

                    Log.d(TAG, "Calling payment API");

                    paymentApi.createPayment("Bearer " + token, paymentRequest)
                            .enqueue(new Callback<PaymentResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<PaymentResponse> call,
                                                       @NonNull Response<PaymentResponse> response) {
                                    loadingOverlay.setVisibility(View.GONE);
                                    btnConfirmPayment.setEnabled(true);

                                    if (response.isSuccessful() && response.body() != null) {
                                        PaymentResponse paymentResponse = response.body();
                                        if (paymentResponse.isSuccess() && paymentResponse.getData() != null) {
                                            String paymentUrl = paymentResponse.getData().getPaymentUrl();
                                            String paymentId = paymentResponse.getData().getId();
                                            Log.d(TAG, "Payment link created: " + paymentUrl);
                                            Log.d(TAG, "Payment ID: " + paymentId);

                                            // Close the subscription dialog
                                            dialog.dismiss();

                                            // Automatically open payment URL in browser
                                            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                                openPaymentInBrowser(paymentUrl, amount, subscriptionId, paymentId, token);
                                            } else {
                                                Toast.makeText(getContext(),
                                                        "Không thể lấy link thanh toán",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(),
                                                    "Lỗi tạo thanh toán: " + paymentResponse.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        try {
                                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                            Log.e(TAG, "Error creating payment: " + errorBody);
                                            Toast.makeText(getContext(),
                                                    "Không thể tạo thanh toán: " + response.code(),
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error reading error body", e);
                                            Toast.makeText(getContext(),
                                                    "Không thể tạo thanh toán",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                                    loadingOverlay.setVisibility(View.GONE);
                                    btnConfirmPayment.setEnabled(true);
                                    Log.e(TAG, "Error creating payment link: " + t.getMessage(), t);
                                    Toast.makeText(getContext(),
                                            "Lỗi kết nối thanh toán: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    loadingOverlay.setVisibility(View.GONE);
                    btnConfirmPayment.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Không thể lấy thông tin khách hàng",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openPaymentInBrowser(String paymentUrl, double amount, String subscriptionId, String paymentId, String token) {
        Log.d(TAG, "Attempting to open payment gateway: " + paymentUrl);
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "Subscription ID: " + subscriptionId);
        Log.d(TAG, "Payment ID: " + paymentId);

        // FORCE WebView for now - to guarantee it works
        Log.d(TAG, "FORCING WebView dialog for testing");
        showPaymentWebView(paymentUrl, amount, subscriptionId, paymentId, token);

        // Reload subscriptions after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadVehicleSubscriptions();
            Log.d(TAG, "Subscriptions reloaded after payment redirect");
        }, 2000);
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showPaymentWebView(String paymentUrl, double amount, String subscriptionId, String paymentId, String token) {
        Log.d(TAG, "Opening payment in WebView dialog");

        // Create WebView dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Create WebView
        android.webkit.WebView webView = new android.webkit.WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Create dialog first so we can reference it in WebViewClient
        AlertDialog[] dialogHolder = new AlertDialog[1];

        // Set custom WebViewClient to monitor payment completion
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                Log.d(TAG, "WebView URL changed: " + url);

                // Check if payment is successful (PayOS redirects to returnUrl on success)
                if (url.contains("payment/success") || url.contains("status=PAID") ||
                    url.contains("code=00") || url.contains("success=true")) {
                    Log.d(TAG, "Payment successful! Auto-closing dialog and updating subscription");

                    // Show success message
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                "Thanh toán thành công! Đang cập nhật trạng thái...",
                                Toast.LENGTH_LONG).show();

                            // Close dialog
                            if (dialogHolder[0] != null) {
                                dialogHolder[0].dismiss();
                            }

                            // Update subscription status to COMPLETED
                            // Note: This will fail if subscription is not in vehicleSubscriptions list
                            // For HomeFragment, we just show success - status updates happen in SubscriptionFragment
                            Log.d(TAG, "Payment successful for subscription: " + subscriptionId);

                            // Refresh subscriptions after a short delay
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                loadVehicleSubscriptions();
                            }, 1500);
                        });
                    }
                    return true;
                }

                // Check if payment was cancelled
                if (url.contains("payment/cancel") || url.contains("status=CANCELLED")) {
                    Log.d(TAG, "Payment cancelled");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                "Thanh toán đã bị hủy",
                                Toast.LENGTH_SHORT).show();

                            // Close dialog
                            if (dialogHolder[0] != null) {
                                dialogHolder[0].dismiss();
                            }
                        });
                    }
                    return true;
                }

                // Continue loading in WebView
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished loading: " + url);

                // Check URL in page content for payment status
                view.evaluateJavascript(
                    "(function() { return window.location.href; })();",
                    value -> {
                        if (value != null) {
                            Log.d(TAG, "Current page URL: " + value);
                            // Check for success indicators in the URL
                            if (value.contains("success") || value.contains("PAID")) {
                                shouldOverrideUrlLoading(view, value);
                            }
                        }
                    }
                );
            }
        });

        // Load payment URL
        webView.loadUrl(paymentUrl);

        builder.setView(webView);
        builder.setTitle("Thanh toán PayOS");
        builder.setNegativeButton("Đóng", (dialog, which) -> {
            dialog.dismiss();
            loadVehicleSubscriptions();
        });

        AlertDialog dialog = builder.create();
        dialogHolder[0] = dialog;
        dialog.show();

        Log.d(TAG, "WebView dialog opened with URL: " + paymentUrl);
    }

    private void updateSubscriptionStatus(String subscriptionId, String status) {
        Log.d(TAG, "Updating subscription status to: " + status);

        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    Toast.makeText(getContext(), "Authentication required", Toast.LENGTH_SHORT).show();
                    return;
                }

                VehicleSubscriptionRepository repository = new VehicleSubscriptionRepository();
                repository.updateSubscriptionStatus(token, subscriptionId, status,
                        new VehicleSubscriptionRepository.SingleSubscriptionCallback() {
                    @Override
                    public void onSuccess(VehicleSubscriptionResponse.VehicleSubscription subscription) {
                        Log.d(TAG, "Subscription status updated successfully to " + status);
                        Toast.makeText(getContext(),
                            "Đã cập nhật trạng thái thành công! ✅",
                            Toast.LENGTH_SHORT).show();
                        loadVehicleSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error updating subscription status: " + error);
                        Toast.makeText(getContext(),
                            "Lỗi cập nhật trạng thái: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showPaymentLinkDialog(String paymentUrl, double amount) {
        Log.d(TAG, "Showing payment link dialog");

        // Inflate the payment link dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payment_link, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog paymentDialog = builder.create();

        // Initialize dialog views
        TextView tvSubscriptionAmount = dialogView.findViewById(R.id.tvSubscriptionAmount);
        Button btnProceedToPayment = dialogView.findViewById(R.id.btnProceedToPayment);
        Button btnPayLater = dialogView.findViewById(R.id.btnPayLater);

        // Format amount
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount) + " VND";
        tvSubscriptionAmount.setText("Số tiền: " + formattedAmount);

        // Proceed to Payment button
        btnProceedToPayment.setOnClickListener(v -> {
            Log.d(TAG, "User clicked 'Thanh toán ngay' button");
            try {
                Log.d(TAG, "Opening browser with URL: " + paymentUrl);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
                Log.d(TAG, "Browser opened successfully");

                Toast.makeText(getContext(),
                        "Đang mở trang thanh toán...",
                        Toast.LENGTH_SHORT).show();

                paymentDialog.dismiss();

                // Reload subscriptions after a delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadVehicleSubscriptions();
                }, 2000);

            } catch (Exception e) {
                Log.e(TAG, "Error opening browser: " + e.getMessage(), e);
                Toast.makeText(getContext(),
                        "Không thể mở trình duyệt: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Pay Later button
        btnPayLater.setOnClickListener(v -> {
            Log.d(TAG, "User clicked 'Thanh toán sau' button");
            Toast.makeText(getContext(),
                    "Bạn có thể thanh toán sau trong mục 'Lịch sử gói'",
                    Toast.LENGTH_LONG).show();
            paymentDialog.dismiss();

            // Reload subscriptions
            loadVehicleSubscriptions();
        });

        paymentDialog.show();
    }
}

