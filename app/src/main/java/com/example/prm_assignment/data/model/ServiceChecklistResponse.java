package com.example.prm_assignment.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ServiceChecklistResponse {
    private boolean success;
    private ChecklistData data;

    public boolean isSuccess() {
        return success;
    }

    public ChecklistData getData() {
        return data;
    }

    public static class ChecklistData {
        private List<ChecklistItem> checklists;
        private int total;
        private int page;
        private int limit;
        private int totalPages;

        public List<ChecklistItem> getChecklists() {
            return checklists;
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

    public static class ChecklistItem {
        @SerializedName("_id")
        private String id;

        private String name;
        private int order;
        private String createdAt;
        private String updatedAt;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getOrder() {
            return order;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}
