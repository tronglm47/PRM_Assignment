package com.example.prm_assignment.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_assignment.R;
import com.example.prm_assignment.ui.adapters.VehicleAdapter;
import com.example.prm_assignment.data.TokenHelper;
import com.example.prm_assignment.data.model.VehicleModel;
import com.example.prm_assignment.data.model.VehicleResponse;
import com.example.prm_assignment.data.remote.VehiclesApi;
import com.example.prm_assignment.data.remote.VehiclesRetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesFragment extends Fragment implements VehicleAdapter.OnVehicleClickListener {

    private static final String TAG = "VehiclesFragment";

    private RecyclerView vehiclesRecyclerView;
    private VehicleAdapter vehicleAdapter;
    private VehiclesApi vehiclesApi;
    private TokenHelper tokenHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);

        // Initialize RecyclerView
        vehiclesRecyclerView = view.findViewById(R.id.vehiclesRecyclerView);
        vehiclesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize API client
        vehiclesApi = VehiclesRetrofitClient.getInstance().getVehiclesApi();
        tokenHelper = new TokenHelper(requireContext());

        // Initialize adapter with empty list
        vehicleAdapter = new VehicleAdapter(new ArrayList<>(), this);
        vehiclesRecyclerView.setAdapter(vehicleAdapter);

        // Fetch vehicles from API
        fetchMyVehicles();

        return view;
    }

    @Override
    public void onVehicleClick(VehicleAdapter.Vehicle vehicle) {
        // Handle vehicle click - navigate to detail fragment
        if (vehicle.id != null && !vehicle.id.isEmpty()) {
            VehicleDetailFragment detailFragment = VehicleDetailFragment.newInstance(vehicle.id);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "Invalid vehicle ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBookServiceClick(VehicleAdapter.Vehicle vehicle) {
        if (vehicle.id != null && !vehicle.id.isEmpty()) {
            BookingFragment bookingFragment = BookingFragment.newInstance(vehicle.id);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, bookingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "Invalid vehicle ID", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchMyVehicles() {
        // Use TokenHelper to get token asynchronously
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
        vehiclesApi.getMyVehicles(authToken).enqueue(new Callback<VehicleResponse>() {
            @Override
            public void onResponse(Call<VehicleResponse> call, Response<VehicleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VehicleResponse vehicleResponse = response.body();
                    if (vehicleResponse.isSuccess() && vehicleResponse.getData() != null) {
                        List<VehicleAdapter.Vehicle> vehicleList = convertToAdapterVehicles(vehicleResponse.getData());
                        updateRecyclerView(vehicleList);
                    } else {
                        showError("No vehicles found");
                    }
                } else {
                    showError("Failed to fetch vehicles: " + response.code());
                    Log.e(TAG, "Error response: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<VehicleResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private List<VehicleAdapter.Vehicle> convertToAdapterVehicles(List<VehicleModel> vehicleModels) {
        List<VehicleAdapter.Vehicle> vehicleList = new ArrayList<>();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (VehicleModel model : vehicleModels) {
            String id = model.getId();
            String vehicleName = model.getVehicleName() != null ? model.getVehicleName() : model.getModel();
            String year = String.valueOf(model.getYear());
            String vin = model.getVin();
            String mileage = String.format(Locale.getDefault(), "%.0f km", model.getMileage());
            String price = currencyFormat.format(model.getPrice());
            String owner = model.getCustomerId() != null ? model.getCustomerId().getCustomerName() : "Unknown";
            String address = model.getCustomerId() != null ? model.getCustomerId().getAddress() : "Unknown";
            String imageUrl = model.getImage(); // Get image URL from API

            vehicleList.add(new VehicleAdapter.Vehicle(
                    id,
                    vehicleName,
                    year,
                    vin,
                    mileage,
                    price,
                    owner,
                    address,
                    imageUrl  // Use imageUrl instead of imageResId
            ));
        }

        return vehicleList;
    }

    private void updateRecyclerView(List<VehicleAdapter.Vehicle> vehicleList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                vehicleAdapter = new VehicleAdapter(vehicleList, this);
                vehiclesRecyclerView.setAdapter(vehicleAdapter);
            });
        }
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
