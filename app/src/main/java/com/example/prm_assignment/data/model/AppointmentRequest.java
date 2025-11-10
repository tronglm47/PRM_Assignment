package com.example.prm_assignment.data.model;

public class AppointmentRequest {
    private String staffId;
    private String customer_id;
    private String vehicle_id;
    private String center_id;
    private String slot_id;
    private String startTime;
    private String endTime;
    private String status;

    public AppointmentRequest(String staffId, String customer_id, String vehicle_id,
                              String center_id, String slot_id, String startTime, String endTime, String status) {
        this.staffId = staffId;
        this.customer_id = customer_id;
        this.vehicle_id = vehicle_id;
        this.center_id = center_id;
        this.slot_id = slot_id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public AppointmentRequest(String customer_id, String vehicle_id, String center_id, String slot_id, String startTime, String endTime) {
        this.customer_id = customer_id;
        this.vehicle_id = vehicle_id;
        this.center_id = center_id;
        this.slot_id = slot_id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "pending";
    }
}
