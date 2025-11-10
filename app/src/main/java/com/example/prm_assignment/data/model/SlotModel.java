package com.example.prm_assignment.data.model;

public class SlotModel {
    private String _id;
    private String center_id;
    private String start_time;
    private String end_time;
    private String slot_date;
    private int capacity;
    private int booked_count;
    private String status;
    private String workshift_id;

    public String getId() { return _id; }
    public String getCenterId() { return center_id; }
    public String getStartTime() { return start_time; }
    public String getEndTime() { return end_time; }
    public String getSlotDate() { return slot_date; }
    public int getCapacity() { return capacity; }
    public int getBookedCount() { return booked_count; }
    public String getStatus() { return status; }
    public String getWorkshiftId() { return workshift_id; }

    public boolean isAvailable() {
        return "active".equals(status) && booked_count < capacity;
    }
}

