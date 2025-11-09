package com.example.prm_assignment.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm_assignment.R;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.CreateSubscriptionRequest;
import com.example.prm_assignment.data.model.PackageModel;
import com.example.prm_assignment.data.model.PackagesResponse;
import com.example.prm_assignment.data.model.PaymentRequest;
import com.example.prm_assignment.data.model.PaymentResponse;
import com.example.prm_assignment.data.model.UpdateSubscriptionRequest;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import com.example.prm_assignment.data.remote.PackagesRetrofitClient;
import com.example.prm_assignment.data.remote.PaymentApi;
import com.example.prm_assignment.data.remote.PaymentRetrofitClient;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;
import com.example.prm_assignment.data.repository.VehicleSubscriptionRepository;
import com.example.prm_assignment.data.repository.PaymentRepository;
import com.example.prm_assignment.ui.adapters.SubscriptionAdapter;
import com.example.prm_assignment.utils.SubscriptionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class SubscriptionFragment extends Fragment implements SubscriptionAdapter.OnSubscriptionActionListener {
    private static final String TAG = "SubscriptionFragment";

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private VehicleSubscriptionRepository repository;
    private PaymentRepository paymentRepository;
    private ProgressBar loadingOverlay;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddSubscription;
    private PaymentApi paymentApi;

    private List<VehicleModel> vehiclesList = new ArrayList<>();
    private List<PackageModel> packagesList = new ArrayList<>();
    private List<VehicleSubscriptionResponse.VehicleSubscription> subscriptions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupRepository();
        loadInitialData();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvSubscriptions);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        fabAddSubscription = view.findViewById(R.id.fabAddSubscription);
        
        swipeRefreshLayout.setOnRefreshListener(this::loadSubscriptions);
        fabAddSubscription.setOnClickListener(v -> showCreateSubscriptionDialog());
    }

    private void setupRecyclerView() {
        adapter = new SubscriptionAdapter(requireContext());
        adapter.setOnSubscriptionActionListener(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupRepository() {
        repository = new VehicleSubscriptionRepository();
        paymentRepository = new PaymentRepository();
        paymentApi = PaymentRetrofitClient.getInstance().getPaymentApi();
    }

    private void loadInitialData() {
        showLoading(true);
        
        // Load vehicles and packages first, then subscriptions
        loadVehicles(() -> loadPackages(this::loadSubscriptions));
    }

    private void loadVehicles(Runnable onComplete) {
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    if (onComplete != null) onComplete.run();
                    return;
                }
                
                VehiclesRetrofitClient.getInstance().getVehiclesApi()
                        .getMyVehicles("Bearer " + token)
                        .enqueue(new Callback<VehicleResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<VehicleResponse> call, 
                                                 @NonNull Response<VehicleResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    vehiclesList = response.body().getData();
                                }
                                if (onComplete != null) onComplete.run();
                            }

                            @Override
                            public void onFailure(@NonNull Call<VehicleResponse> call, @NonNull Throwable t) {
                                if (onComplete != null) onComplete.run();
                            }
                        });
            }
        });
    }

    private void loadPackages(Runnable onComplete) {
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    if (onComplete != null) onComplete.run();
                    return;
                }
                
                PackagesRetrofitClient.getInstance().getPackagesApi()
                        .getPackages("Bearer " + token)
                        .enqueue(new Callback<PackagesResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<PackagesResponse> call, 
                                                 @NonNull Response<PackagesResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    packagesList = response.body().getData();
                                }
                                if (onComplete != null) onComplete.run();
                            }

                            @Override
                            public void onFailure(@NonNull Call<PackagesResponse> call, @NonNull Throwable t) {
                                if (onComplete != null) onComplete.run();
                            }
                        });
            }
        });
    }

    private void loadSubscriptions() {
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Authentication required");
                    return;
                }
                
                new TokenHelper(requireContext()).getCustomerIdFromProfile(
                        new TokenHelper.CustomerIdCallback() {
                    @Override
                    public void onCustomerIdRetrieved(String customerId) {
                        if (customerId == null || customerId.isEmpty()) {
                            showLoading(false);
                            swipeRefreshLayout.setRefreshing(false);
                            showError("Customer ID not found");
                            return;
                        }
                        
                        repository.getSubscriptionsByCustomer(token, customerId, 
                                new VehicleSubscriptionRepository.SubscriptionListCallback() {
                            @Override
                            public void onSuccess(VehicleSubscriptionResponse response) {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                
                                if (response.getData() != null && !response.getData().isEmpty()) {
                                    subscriptions = response.getData(); // Store in class field
                                    adapter.setSubscriptions(subscriptions);
                                    showEmptyState(false);
                                } else {
                                    subscriptions = new ArrayList<>(); // Clear the list
                                    adapter.setSubscriptions(subscriptions);
                                    showEmptyState(true);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                showLoading(false);
                                swipeRefreshLayout.setRefreshing(false);
                                showError(error);
                                showEmptyState(true);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onRenewClick(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        showRenewSubscriptionDialog(subscription);
    }

    @Override
    public void onEditClick(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        showEditSubscriptionDialog(subscription);
    }

    @Override
    public void onDeleteClick(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        showDeleteConfirmationDialog(subscription);
    }

    @Override
    public void onViewDetailsClick(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        showSubscriptionDetailsDialog(subscription);
    }

    private void showCreateSubscriptionDialog() {
        if (vehiclesList.isEmpty()) {
            showError("No vehicles available. Please add a vehicle first.");
            return;
        }
        if (packagesList.isEmpty()) {
            showError("No packages available.");
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_subscription, null);
        
        Spinner spVehicle = dialogView.findViewById(R.id.spVehicle);
        Spinner spPackage = dialogView.findViewById(R.id.spPackage);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectStartDate);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);

        // Setup vehicle spinner
        List<String> vehicleNames = new ArrayList<>();
        for (VehicleModel vehicle : vehiclesList) {
            vehicleNames.add(vehicle.getVehicleName() + " - " + vehicle.getModel());
        }
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vehicleNames);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicle.setAdapter(vehicleAdapter);

        // Setup package spinner
        List<String> packageNames = new ArrayList<>();
        for (PackageModel pkg : packagesList) {
            packageNames.add(pkg.getName() + " - " + SubscriptionManager.formatPrice(pkg.getPrice()));
        }
        ArrayAdapter<String> packageAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, packageNames);
        packageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPackage.setAdapter(packageAdapter);

        // Date picker
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        tvSelectedDate.setText(dateFormat.format(calendar.getTime()));

        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        tvSelectedDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Create Subscription")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    int vehiclePosition = spVehicle.getSelectedItemPosition();
                    int packagePosition = spPackage.getSelectedItemPosition();
                    
                    if (vehiclePosition >= 0 && packagePosition >= 0) {
                        String vehicleId = vehiclesList.get(vehiclePosition).getId();
                        PackageModel selectedPackage = packagesList.get(packagePosition);
                        
                        Date startDate = calendar.getTime();
                        Date endDate = SubscriptionManager.calculateEndDate(startDate, 
                                selectedPackage.getDuration());
                        
                        CreateSubscriptionRequest request = new CreateSubscriptionRequest(
                                vehicleId,
                                selectedPackage.getId(),
                                SubscriptionManager.formatDateForApi(startDate),
                                SubscriptionManager.formatDateForApi(endDate),
                                "PENDING"
                        );
                        
                        createSubscription(request, selectedPackage);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createSubscription(CreateSubscriptionRequest request, PackageModel packageModel) {
        showLoading(true);
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showLoading(false);
                    showError("Authentication required");
                    return;
                }
                
                repository.createSubscription(token, request, 
                        new VehicleSubscriptionRepository.SingleSubscriptionCallback() {
                    @Override
                    public void onSuccess(VehicleSubscriptionResponse.VehicleSubscription subscription) {
                        Log.d(TAG, "Subscription created successfully with ID: " + subscription.getId());

                        // Now create payment link
                        createPaymentLink(subscription.getId(), packageModel.getPrice(), token);
                        showSuccess("Subscription created successfully");
                        loadSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        showError(error);
                    }
                });
            }
        });
    }

    private void showRenewSubscriptionDialog(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_renew_subscription, null);
        
        Spinner spPackage = dialogView.findViewById(R.id.spPackage);
        TextView tvInfo = dialogView.findViewById(R.id.tvInfo);

        tvInfo.setText("Renewing subscription for: " + 
                subscription.getVehicleInfo().getVehicleName());

        // Setup package spinner
        List<String> packageNames = new ArrayList<>();
        for (PackageModel pkg : packagesList) {
            packageNames.add(pkg.getName() + " - " + SubscriptionManager.formatPrice(pkg.getPrice()));
        }
        ArrayAdapter<String> packageAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, packageNames);
        packageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPackage.setAdapter(packageAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Renew Subscription")
                .setView(dialogView)
                .setPositiveButton("Renew", (dialog, which) -> {
                    int position = spPackage.getSelectedItemPosition();
                    if (position >= 0) {
                        String newPackageId = packagesList.get(position).getId();
                        renewSubscription(subscription.getId(), newPackageId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renewSubscription(String subscriptionId, String newPackageId) {
        showLoading(true);
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showLoading(false);
                    showError("Authentication required");
                    return;
                }
                
                repository.renewSubscription(token, subscriptionId, newPackageId,
                        new VehicleSubscriptionRepository.SingleSubscriptionCallback() {
                    @Override
                    public void onSuccess(VehicleSubscriptionResponse.VehicleSubscription subscription) {
                        showLoading(false);
                        showSuccess("Subscription renewed successfully");
                        loadSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        showError(error);
                    }
                });
            }
        });
    }

    private void showEditSubscriptionDialog(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        // Create edit dialog with status update
        String[] statuses = {"ACTIVE", "PENDING", "EXPIRED"};
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Update Subscription Status")
                .setItems(statuses, (dialog, which) -> {
                    updateSubscriptionStatus(subscription.getId(), statuses[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSubscriptionStatus(String subscriptionId, String status) {
        showLoading(true);
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showLoading(false);
                    showError("Authentication required");
                    return;
                }
                
                repository.updateSubscriptionStatus(token, subscriptionId, status,
                        new VehicleSubscriptionRepository.SingleSubscriptionCallback() {
                    @Override
                    public void onSuccess(VehicleSubscriptionResponse.VehicleSubscription subscription) {
                        showLoading(false);
                        showSuccess("Subscription status updated");
                        loadSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        showError(error);
                    }
                });
            }
        });
    }

    private void updateSubscriptionStatusWithData(String subscriptionId, String newStatus) {
        Log.d(TAG, "Updating subscription status with full data for: " + subscriptionId);

        // Find the subscription in our current list to get all its data
        VehicleSubscriptionResponse.VehicleSubscription targetSubscription = null;
        for (VehicleSubscriptionResponse.VehicleSubscription sub : subscriptions) {
            if (sub.getId().equals(subscriptionId)) {
                targetSubscription = sub;
                break;
            }
        }

        if (targetSubscription == null) {
            Log.e(TAG, "Subscription not found in current list: " + subscriptionId);
            showError("Subscription not found");
            return;
        }

        final VehicleSubscriptionResponse.VehicleSubscription subscription = targetSubscription;

        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showError("Authentication required");
                    return;
                }

                // Create update request with ALL fields from the subscription
                UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
                request.setVehicleId(subscription.getVehicleInfo().getId());
                request.setPackageId(subscription.getPackageInfo().getId());
                request.setStartDate(subscription.getStartDate());
                request.setEndDate(subscription.getEndDate());
                request.setStatus(newStatus);  // Only change the status

                Log.d(TAG, "Sending update with: vehicleId=" + subscription.getVehicleInfo().getId() +
                          ", packageId=" + subscription.getPackageInfo().getId() + ", status=" + newStatus);

                repository.updateSubscription(token, subscriptionId, request,
                        new VehicleSubscriptionRepository.SingleSubscriptionCallback() {
                    @Override
                    public void onSuccess(VehicleSubscriptionResponse.VehicleSubscription updatedSubscription) {
                        Log.d(TAG, "Subscription status updated successfully to " + newStatus);
                        Toast.makeText(requireContext(),
                            "Đã kích hoạt gói thành công! ✅",
                            Toast.LENGTH_SHORT).show();
                        loadSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error updating subscription status: " + error);
                        Toast.makeText(requireContext(),
                            "Lỗi cập nhật trạng thái: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showDeleteConfirmationDialog(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Subscription")
                .setMessage("Are you sure you want to delete this subscription for " + 
                        subscription.getVehicleInfo().getVehicleName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteSubscription(subscription.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSubscription(String subscriptionId) {
        showLoading(true);
        TokenHelper.Companion.getAccessTokenAsync(requireContext(), new TokenHelper.TokenAsyncCallback() {
            @Override
            public void onResult(String token) {
                if (token == null || token.isEmpty()) {
                    showLoading(false);
                    showError("Authentication required");
                    return;
                }
                
                repository.deleteSubscription(token, subscriptionId,
                        new VehicleSubscriptionRepository.DeleteCallback() {
                    @Override
                    public void onSuccess(String message) {
                        showLoading(false);
                        showSuccess("Subscription deleted successfully");
                        loadSubscriptions();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        showError(error);
                    }
                });
            }
        });
    }

    private void showSubscriptionDetailsDialog(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        StringBuilder details = new StringBuilder();
        details.append("Vehicle: ").append(subscription.getVehicleInfo().getVehicleName()).append("\n\n");
        details.append("Model: ").append(subscription.getVehicleInfo().getModel()).append("\n\n");
        details.append("Package: ").append(subscription.getPackageInfo().getName()).append("\n\n");
        details.append("Price: ").append(SubscriptionManager.formatPrice(subscription.getPackageInfo().getPrice())).append("\n\n");
        details.append("Status: ").append(subscription.getStatus()).append("\n\n");
        details.append("Start Date: ").append(SubscriptionManager.formatDateForDisplay(subscription.getStartDate())).append("\n\n");
        details.append("End Date: ").append(SubscriptionManager.formatDateForDisplay(subscription.getEndDate())).append("\n\n");
        details.append(SubscriptionManager.getStatusDisplayText(subscription));

        new AlertDialog.Builder(requireContext())
                .setTitle("Subscription Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void createPaymentLink(String subscriptionId, double amount, String token) {
        Log.d(TAG, "Creating payment link for subscription: " + subscriptionId);

        // Get customer ID first
        new TokenHelper(requireContext()).getCustomerIdFromProfile(new TokenHelper.CustomerIdCallback() {
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
                                    showLoading(false);

                                    if (response.isSuccessful() && response.body() != null) {
                                        PaymentResponse paymentResponse = response.body();
                                        if (paymentResponse.isSuccess() && paymentResponse.getData() != null) {
                                            String paymentUrl = paymentResponse.getData().getPaymentUrl();
                                            String paymentId = paymentResponse.getData().getId();
                                            Log.d(TAG, "Payment link created: " + paymentUrl);
                                            Log.d(TAG, "Payment ID: " + paymentId);

                                            // Automatically open payment URL in browser
                                            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                                openPaymentInBrowser(paymentUrl, amount, subscriptionId, paymentId, token);
                                            } else {
                                                showError("Không thể lấy link thanh toán");
                                            }
                                        } else {
                                            showError("Lỗi tạo thanh toán: " + paymentResponse.getMessage());
                                        }
                                    } else {
                                        try {
                                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                            Log.e(TAG, "Error creating payment: " + errorBody);
                                            showError("Không thể tạo thanh toán: " + response.code());
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error reading error body", e);
                                            showError("Không thể tạo thanh toán");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                                    showLoading(false);
                                    Log.e(TAG, "Error creating payment link: " + t.getMessage(), t);
                                    showError("Lỗi kết nối thanh toán: " + t.getMessage());
                                }
                            });
                } else {
                    showLoading(false);
                    showError("Không thể lấy thông tin khách hàng");
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
            loadSubscriptions();
            Log.d(TAG, "Subscriptions reloaded after payment redirect");
        }, 2000);
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            requireContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showPaymentWebView(String paymentUrl, double amount, String subscriptionId, String paymentId, String token) {
        Log.d(TAG, "Opening payment in WebView dialog");

        // Create WebView dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());

        // Create WebView
        android.webkit.WebView webView = new android.webkit.WebView(requireContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Create dialog first so we can reference it in WebViewClient
        android.app.AlertDialog[] dialogHolder = new android.app.AlertDialog[1];

        // Set custom WebViewClient to monitor payment completion
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                Log.d(TAG, "WebView URL changed: " + url);

                // Check if payment is successful (PayOS redirects to returnUrl on success)
                if (url.contains("payment/success") || url.contains("status=PAID") ||
                    url.contains("code=00") || url.contains("success=true")) {
                    Log.d(TAG, "Payment successful! Auto-closing dialog and updating subscription");

                    // Extract orderCode from URL
                    int orderCode = extractOrderCodeFromUrl(url);

                    // Show success message
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                "Thanh toán thành công! Đang cập nhật trạng thái...",
                                Toast.LENGTH_LONG).show();

                            // Close dialog
                            if (dialogHolder[0] != null) {
                                dialogHolder[0].dismiss();
                            }

                            // Call webhook endpoint to update subscription status
                            if (orderCode > 0) {
                                handlePaymentWebhook(orderCode, "PAID");
                            } else {
                                // Fallback to direct status update if orderCode not found
                                Log.w(TAG, "OrderCode not found in URL, using direct update");
                                updateSubscriptionStatusWithData(subscriptionId, "ACTIVE");
                            }

                            // Refresh subscriptions after a short delay
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                loadSubscriptions();
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
                            Toast.makeText(requireContext(),
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
                // PayOS may update the page without redirecting
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
            loadSubscriptions();
        });

        android.app.AlertDialog dialog = builder.create();
        dialogHolder[0] = dialog;
        dialog.show();

        Log.d(TAG, "WebView dialog opened with URL: " + paymentUrl);
    }

    /**
     * Extract orderCode from PayOS success URL
     * URL format: https://example.com/payment/success?code=00&id=xxx&orderCode=123456
     */
    private int extractOrderCodeFromUrl(String url) {
        try {
            if (url.contains("orderCode=")) {
                String[] parts = url.split("orderCode=");
                if (parts.length > 1) {
                    String orderCodeStr = parts[1].split("&")[0];
                    return Integer.parseInt(orderCodeStr);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting orderCode from URL: " + url, e);
        }
        return -1;
    }

    /**
     * Handle payment webhook - sends orderCode and status to backend
     * Backend will update subscription status to ACTIVE automatically
     */
    private void handlePaymentWebhook(int orderCode, String status) {
        Log.d(TAG, "Handling payment webhook - orderCode: " + orderCode + ", status: " + status);

        paymentRepository.handlePaymentWebhook(orderCode, status,
            new PaymentRepository.WebhookCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Webhook processed successfully: " + message);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                "Đã kích hoạt gói thành công! ✅",
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Webhook processing failed: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                "Lỗi cập nhật trạng thái: " + error,
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
    }
}

