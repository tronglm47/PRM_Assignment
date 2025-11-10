package com.example.prm_assignment.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_assignment.R;
import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;
import com.example.prm_assignment.utils.SubscriptionManager;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {
    
    private final Context context;
    private List<VehicleSubscriptionResponse.VehicleSubscription> subscriptions;
    private OnSubscriptionActionListener listener;

    public interface OnSubscriptionActionListener {
        void onRenewClick(VehicleSubscriptionResponse.VehicleSubscription subscription);
        void onEditClick(VehicleSubscriptionResponse.VehicleSubscription subscription);
        void onDeleteClick(VehicleSubscriptionResponse.VehicleSubscription subscription);
        void onViewDetailsClick(VehicleSubscriptionResponse.VehicleSubscription subscription);
    }

    public SubscriptionAdapter(Context context) {
        this.context = context;
        this.subscriptions = new ArrayList<>();
    }

    public void setSubscriptions(List<VehicleSubscriptionResponse.VehicleSubscription> subscriptions) {
        this.subscriptions = subscriptions != null ? subscriptions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnSubscriptionActionListener(OnSubscriptionActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        VehicleSubscriptionResponse.VehicleSubscription subscription = subscriptions.get(position);
        holder.bind(subscription);
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivVehicleImage;
        private final TextView tvVehicleName;
        private final TextView tvPackageName;
        private final TextView tvStatus;
        private final TextView tvDaysRemaining;
        private final TextView tvDateRange;
        private final TextView tvKilometers;
        private final TextView tvPrice;
        private final ProgressBar progressBar;
        private final Button btnRenew;
        private final Button btnEdit;
        private final Button btnDelete;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cvSubscriptionCard);
            ivVehicleImage = itemView.findViewById(R.id.ivVehicleImage);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDaysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvKilometers = itemView.findViewById(R.id.tvKilometers);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnRenew = itemView.findViewById(R.id.btnRenew);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(VehicleSubscriptionResponse.VehicleSubscription subscription) {
            // Vehicle info
            if (subscription.getVehicleInfo() != null) {
                String vehicleName = subscription.getVehicleInfo().getVehicleName();
                String model = subscription.getVehicleInfo().getModel();
                tvVehicleName.setText(vehicleName + " - " + model);
                
                // Load vehicle image
                String imageUrl = subscription.getVehicleInfo().getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_directions_car_24)
                            .error(R.drawable.ic_directions_car_24)
                            .into(ivVehicleImage);
                } else {
                    ivVehicleImage.setImageResource(R.drawable.ic_directions_car_24);
                }
            }

            // Package info
            if (subscription.getPackageInfo() != null) {
                tvPackageName.setText(subscription.getPackageInfo().getName());
                tvKilometers.setText(SubscriptionManager.formatKilometers(
                        subscription.getPackageInfo().getKmInterval()));
                tvPrice.setText(SubscriptionManager.formatPrice(
                        subscription.getPackageInfo().getPrice()));
            }

            // Status and days remaining
            long daysRemaining = SubscriptionManager.calculateDaysRemaining(subscription.getEndDate());
            tvStatus.setText(subscription.getStatus());
            tvDaysRemaining.setText(SubscriptionManager.getStatusDisplayText(subscription));

            // Set status color
            int statusColor = SubscriptionManager.getProgressBarColor(daysRemaining);
            tvStatus.setTextColor(statusColor);
            tvDaysRemaining.setTextColor(statusColor);

            // Date range
            String startDate = SubscriptionManager.formatDateForDisplay(subscription.getStartDate());
            String endDate = SubscriptionManager.formatDateForDisplay(subscription.getEndDate());
            tvDateRange.setText(startDate + " - " + endDate);

            // Progress bar
            int progress = SubscriptionManager.calculateProgressPercentage(
                    subscription.getStartDate(), subscription.getEndDate());
            progressBar.setProgress(progress);
            progressBar.getProgressDrawable().setColorFilter(statusColor, 
                    android.graphics.PorterDuff.Mode.SRC_IN);

            // Button visibility and actions
            boolean isActive = SubscriptionManager.isSubscriptionActive(subscription);
            boolean isExpired = SubscriptionManager.isExpired(subscription);

            btnRenew.setVisibility(isExpired || daysRemaining <= 30 ? View.VISIBLE : View.GONE);
            btnEdit.setVisibility(isActive ? View.VISIBLE : View.GONE);

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(subscription);
                }
            });

            btnRenew.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRenewClick(subscription);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(subscription);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(subscription);
                }
            });
        }
    }
}
