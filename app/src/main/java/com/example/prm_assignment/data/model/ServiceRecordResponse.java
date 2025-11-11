package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ServiceRecordResponse {
    private boolean success;
    private ServiceRecordsData data;

    public boolean isSuccess() {
        return success;
    }

    public ServiceRecordsData getData() {
        return data;
    }

    public static class ServiceRecordsData {
        private List<ServiceRecordData> records;
        private int total;
        private int page;
        private int limit;
        private int totalPages;

        public List<ServiceRecordData> getRecords() {
            return records;
        }

        public int getTotal() {
            return total;
        }

        public int getPage() {
            return page;
        }

        public int getLimit() {
            return limit;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    public static class ServiceRecordData {
        @SerializedName("_id")
        private String id;

        @SerializedName("appointment_id")
        private AppointmentInfo appointmentId;

        @SerializedName("technician_id")
        private TechnicianInfo technicianId;

        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        private String description;
        private String status;
        private String createdAt;
        private String updatedAt;

        public String getId() {
            return id;
        }

        public AppointmentInfo getAppointmentId() {
            return appointmentId;
        }

        public TechnicianInfo getTechnicianId() {
            return technicianId;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    public static class AppointmentInfo {
        @SerializedName("_id")
        private String id;

        public String getId() {
            return id;
        }
    }

    public static class TechnicianInfo {
        @SerializedName("_id")
        private String id;

        private String name;
        private String dateOfBirth;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }
}
