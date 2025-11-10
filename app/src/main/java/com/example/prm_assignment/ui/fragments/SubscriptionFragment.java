package com.example.prm_assignment.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.example.prm_assignment.data.model.UpdateSubscriptionRequest;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import com.example.prm_assignment.data.remote.PackagesRetrofitClient;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;
import com.example.prm_assignment.data.repository.VehicleSubscriptionRepository;
import com.example.prm_assignment.ui.adapters.SubscriptionAdapter;
import com.example.prm_assignment.utils.SubscriptionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private VehicleSubscriptionRepository repository;
    private ProgressBar loadingOverlay;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddSubscription;
    
    private List<VehicleModel> vehiclesList = new ArrayList<>();
    private List<PackageModel> packagesList = new ArrayList<>();

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
                                    adapter.setSubscriptions(response.getData());
                                    showEmptyState(false);
                                } else {
                                    adapter.setSubscriptions(new ArrayList<>());
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
                        
                        createSubscription(request);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createSubscription(CreateSubscriptionRequest request) {
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
                        showLoading(false);
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
}

