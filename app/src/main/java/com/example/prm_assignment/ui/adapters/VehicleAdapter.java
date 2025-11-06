package com.example.prm_assignment.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_assignment.R;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;
    private OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
        void onBookServiceClick(Vehicle vehicle);
    }

    public static class Vehicle {
        public String id;
        public String model;
        public String year;
        public String vin;
        public String mileage;
        public String price;
        public String owner;
        public String address;
        public int imageResId;
        public String imageUrl;

        // Constructor with resource ID (for backward compatibility)
        public Vehicle(String id, String model, String year, String vin, String mileage,
                      String price, String owner, String address, int imageResId) {
            this.id = id;
            this.model = model;
            this.year = year;
            this.vin = vin;
            this.mileage = mileage;
            this.price = price;
            this.owner = owner;
            this.address = address;
            this.imageResId = imageResId;
            this.imageUrl = null;
        }

        // Constructor with image URL (for API data)
        public Vehicle(String id, String model, String year, String vin, String mileage,
                      String price, String owner, String address, String imageUrl) {
            this.id = id;
            this.model = model;
            this.year = year;
            this.vin = vin;
            this.mileage = mileage;
            this.price = price;
            this.owner = owner;
            this.address = address;
            this.imageUrl = imageUrl;
            this.imageResId = 0;
        }
    }

    public VehicleAdapter(List<Vehicle> vehicleList, OnVehicleClickListener listener) {
        this.vehicleList = vehicleList != null ? vehicleList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        holder.tvCarModel.setText(vehicle.model);
        holder.tvCarYear.setText(vehicle.year);

        // Load image using Glide
        if (vehicle.imageUrl != null && !vehicle.imageUrl.isEmpty()) {
            // Load from URL
            Glide.with(holder.itemView.getContext())
                    .load(vehicle.imageUrl)
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .centerCrop()
                    .into(holder.ivCarImage);
        } else if (vehicle.imageResId != 0) {
            // Load from resource
            holder.ivCarImage.setImageResource(vehicle.imageResId);
        } else {
            // Default placeholder
            holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        // Set click listener for the whole card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVehicleClick(vehicle);
            }
        });

        // Set click listener for book service button
        holder.btnBookService.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookServiceClick(vehicle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvCarModel;
        TextView tvCarYear;
        TextView tvServiceIndicator;
        CardView btnBookService;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvCarModel = itemView.findViewById(R.id.tvCarModel);
            tvCarYear = itemView.findViewById(R.id.tvCarYear);
            tvServiceIndicator = itemView.findViewById(R.id.tvServiceIndicator);
            btnBookService = itemView.findViewById(R.id.btnBookService);
        }
    }
}

