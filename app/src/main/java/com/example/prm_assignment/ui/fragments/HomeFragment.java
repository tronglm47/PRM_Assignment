package com.example.prm_assignment.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.PackageModel;
import com.example.prm_assignment.data.model.PackagesResponse;
import com.example.prm_assignment.data.model.ProfileResponse;
import com.example.prm_assignment.data.remote.PackagesApi;
import com.example.prm_assignment.data.remote.PackagesRetrofitClient;
import com.example.prm_assignment.data.remote.ProfileApi;
import com.example.prm_assignment.data.remote.ProfileRetrofitClient;

import java.text.NumberFormat;
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
            Toast.makeText(getContext(), "Đặt gói: " + packageModel.getName(), Toast.LENGTH_SHORT).show();
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

        // Find views in the card (removed ivVehicleImage)
        TextView tvVehicleName = cardView.findViewById(R.id.tvVehicleName);
        TextView tvVehicleModel = cardView.findViewById(R.id.tvVehicleModel);
        TextView tvPackageName = cardView.findViewById(R.id.tvPackageName);
        TextView tvSubscriptionStatus = cardView.findViewById(R.id.tvSubscriptionStatus);
        TextView tvKmInterval = cardView.findViewById(R.id.tvKmInterval);
        TextView tvDateRange = cardView.findViewById(R.id.tvDateRange);
        ProgressBar progressBar = cardView.findViewById(R.id.progressBar);

        // Set vehicle data
        if (subscription.getVehicleInfo() != null) {
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
}

