package com.example.prm_assignment.data.model;

import java.util.List;

public class AppointmentsResponse {
    private boolean success;
    private String message;
    private AppointmentsData data;

    public AppointmentsResponse() {
    }

    public AppointmentsResponse(boolean success, String message, AppointmentsData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AppointmentsData getData() {
        return data;
    }

    public void setData(AppointmentsData data) {
        this.data = data;
    }

    public static class AppointmentsData {
        private List<AppointmentModel> appointments;
        private int total;
        private int page;
        private int limit;
        private int totalPages;

        public AppointmentsData() {
        }

        public List<AppointmentModel> getAppointments() {
            return appointments;
        }

        public void setAppointments(List<AppointmentModel> appointments) {
            this.appointments = appointments;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}
