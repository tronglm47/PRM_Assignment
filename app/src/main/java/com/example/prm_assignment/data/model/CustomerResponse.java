package com.example.prm_assignment.data.model;

public class CustomerResponse {
    private boolean success;
    private CustomerData data;

    public boolean isSuccess() { return success; }
    public CustomerData getData() { return data; }

    public static class CustomerData {
        private String _id;
        private String user_id;
        private String customerName;
        private String address;

        public String getId() { return _id; }
        public String getUserId() { return user_id; }
        public String getCustomerName() { return customerName; }
        public String getAddress() { return address; }
    }
}
