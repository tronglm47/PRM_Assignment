package com.example.prm_assignment.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.prm_assignment.R;
import com.example.prm_assignment.data.model.VehicleModel;

import java.util.List;

public class VehicleImagePagerAdapter extends RecyclerView.Adapter<VehicleImagePagerAdapter.ImageViewHolder> {
    private List<VehicleModel> vehicles;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(VehicleModel vehicle, int position);
    }

    public VehicleImagePagerAdapter(List<VehicleModel> vehicles, OnImageClickListener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        VehicleModel vehicle = vehicles.get(position);
        holder.bind(vehicle, position);
    }

    @Override
    public int getItemCount() {
        return vehicles != null ? vehicles.size() : 0;
    }

    public void updateVehicles(List<VehicleModel> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivVehicleImage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        void bind(VehicleModel vehicle, int position) {
            progressBar.setVisibility(View.VISIBLE);

            String imageUrl = vehicle.getImage();

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            progressBar.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(vehicle, position);
                }
            });
        }
    }
}

