package com.example.prm_assignment.utils;

import android.graphics.Color;

import com.example.prm_assignment.data.model.VehicleSubscriptionResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SubscriptionManager {
    
    // Date formats
    public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    public static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    
    static {
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    // Subscription status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_PENDING = "PENDING";
    
    // Color thresholds for subscription urgency
    public static final int DAYS_CRITICAL = 7;      // Red zone
    public static final int DAYS_WARNING = 15;      // Orange zone
    
    /**
     * Calculate days remaining until subscription expires
     * @param endDate End date string in ISO format
     * @return Number of days remaining (negative if expired)
     */
    public static long calculateDaysRemaining(String endDate) {
        try {
            Date end = ISO_DATE_FORMAT.parse(endDate);
            Date now = new Date();
            
            if (end == null) return 0;
            
            long diffInMillis = end.getTime() - now.getTime();
            return TimeUnit.MILLISECONDS.toDays(diffInMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Calculate percentage of subscription time used
     * @param startDate Start date string in ISO format
     * @param endDate End date string in ISO format
     * @return Percentage (0-100)
     */
    public static int calculateProgressPercentage(String startDate, String endDate) {
        try {
            Date start = ISO_DATE_FORMAT.parse(startDate);
            Date end = ISO_DATE_FORMAT.parse(endDate);
            Date now = new Date();
            
            if (start == null || end == null) return 0;
            
            long totalDuration = end.getTime() - start.getTime();
            long elapsedDuration = now.getTime() - start.getTime();
            
            if (totalDuration <= 0) return 100;
            
            int percentage = (int) ((elapsedDuration * 100) / totalDuration);
            return Math.min(100, Math.max(0, percentage));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get color for progress bar based on days remaining
     * @param daysRemaining Number of days until expiration
     * @return Color int (green, orange, or red)
     */
    public static int getProgressBarColor(long daysRemaining) {
        if (daysRemaining <= DAYS_CRITICAL) {
            return Color.parseColor("#F44336"); // Red
        } else if (daysRemaining <= DAYS_WARNING) {
            return Color.parseColor("#FF9800"); // Orange
        } else {
            return Color.parseColor("#4CAF50"); // Green
        }
    }
    
    /**
     * Format ISO date string to display format (dd/MM/yyyy)
     * @param isoDate Date string in ISO format
     * @return Formatted date string
     */
    public static String formatDateForDisplay(String isoDate) {
        try {
            Date date = ISO_DATE_FORMAT.parse(isoDate);
            if (date == null) return "";
            return DISPLAY_DATE_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate;
        }
    }
    
    /**
     * Format date for API request (ISO format)
     * @param date Date object
     * @return ISO formatted date string
     */
    public static String formatDateForApi(Date date) {
        return ISO_DATE_FORMAT.format(date);
    }
    
    /**
     * Check if subscription is active
     * @param subscription VehicleSubscription object
     * @return true if subscription is active
     */
    public static boolean isSubscriptionActive(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        if (subscription == null) return false;
        
        String status = subscription.getStatus();
        long daysRemaining = calculateDaysRemaining(subscription.getEndDate());
        
        return STATUS_ACTIVE.equals(status) && daysRemaining >= 0;
    }
    
    /**
     * Check if subscription is expiring soon
     * @param subscription VehicleSubscription object
     * @param days Number of days threshold
     * @return true if subscription expires within specified days
     */
    public static boolean isExpiringSoon(VehicleSubscriptionResponse.VehicleSubscription subscription, int days) {
        if (subscription == null) return false;
        
        long daysRemaining = calculateDaysRemaining(subscription.getEndDate());
        return daysRemaining >= 0 && daysRemaining <= days && STATUS_ACTIVE.equals(subscription.getStatus());
    }
    
    /**
     * Check if subscription has expired
     * @param subscription VehicleSubscription object
     * @return true if subscription has expired
     */
    public static boolean isExpired(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        if (subscription == null) return false;
        
        long daysRemaining = calculateDaysRemaining(subscription.getEndDate());
        return daysRemaining < 0 || STATUS_EXPIRED.equals(subscription.getStatus());
    }
    
    /**
     * Get status display text
     * @param subscription VehicleSubscription object
     * @return Status text for display
     */
    public static String getStatusDisplayText(VehicleSubscriptionResponse.VehicleSubscription subscription) {
        if (subscription == null) return "Unknown";
        
        long daysRemaining = calculateDaysRemaining(subscription.getEndDate());
        
        if (isExpired(subscription)) {
            return "Expired";
        } else if (daysRemaining == 0) {
            return "Expires today";
        } else if (daysRemaining == 1) {
            return "1 day remaining";
        } else if (daysRemaining > 1) {
            return daysRemaining + " days remaining";
        } else {
            return "Expired " + Math.abs(daysRemaining) + " days ago";
        }
    }
    
    /**
     * Calculate new end date based on package duration
     * @param startDate Start date
     * @param durationDays Duration in days
     * @return End date
     */
    public static Date calculateEndDate(Date startDate, int durationDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, durationDays);
        return calendar.getTime();
    }
    
    /**
     * Calculate new end date string for API
     * @param startDateString Start date in ISO format
     * @param durationDays Duration in days
     * @return End date in ISO format
     */
    public static String calculateEndDateString(String startDateString, int durationDays) {
        try {
            Date startDate = ISO_DATE_FORMAT.parse(startDateString);
            if (startDate == null) return "";
            
            Date endDate = calculateEndDate(startDate, durationDays);
            return formatDateForApi(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Validate subscription dates
     * @param startDate Start date string
     * @param endDate End date string
     * @return true if dates are valid (end after start)
     */
    public static boolean validateDates(String startDate, String endDate) {
        try {
            Date start = ISO_DATE_FORMAT.parse(startDate);
            Date end = ISO_DATE_FORMAT.parse(endDate);
            
            if (start == null || end == null) return false;
            
            return end.after(start);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get urgency level text
     * @param daysRemaining Days until expiration
     * @return Urgency level text
     */
    public static String getUrgencyLevel(long daysRemaining) {
        if (daysRemaining <= 0) {
            return "EXPIRED";
        } else if (daysRemaining <= DAYS_CRITICAL) {
            return "CRITICAL";
        } else if (daysRemaining <= DAYS_WARNING) {
            return "WARNING";
        } else {
            return "NORMAL";
        }
    }
    
    /**
     * Format kilometers for display
     * @param kilometers Kilometers value
     * @return Formatted string
     */
    public static String formatKilometers(int kilometers) {
        if (kilometers >= 1000) {
            return String.format(Locale.US, "%,d km", kilometers);
        }
        return kilometers + " km";
    }
    
    /**
     * Format price for display
     * @param price Price value
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        return String.format(Locale.US, "$%.2f", price);
    }
}
