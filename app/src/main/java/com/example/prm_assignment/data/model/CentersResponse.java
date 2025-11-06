package com.example.prm_assignment.data.model;

import java.util.List;

public class CentersResponse {
    private boolean success;
    private CentersData data;

    public boolean isSuccess() { return success; }
    public CentersData getData() { return data; }

    public static class CentersData {
        private List<CenterModel> centers;
        private int total;
        private int page;
        private int limit;
        private int totalPages;

        public List<CenterModel> getCenters() { return centers; }
        public int getTotal() { return total; }
        public int getPage() { return page; }
        public int getLimit() { return limit; }
        public int getTotalPages() { return totalPages; }
    }
}
