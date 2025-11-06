package com.example.prm_assignment.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
    private FrameLayout loadingOverlay;
    private TokenHelper tokenHelper;
    private PackagesApi packagesApi;
    private ProfileApi profileApi;

    // Track loading states
    private boolean profileLoaded = false;
    private boolean packagesLoaded = false;

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
        // Hide loading only when both profile and packages are loaded
        if (profileLoaded && packagesLoaded) {
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
                    if (profileResponse.getSuccess() && profileResponse.getData() != null) {
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
}

