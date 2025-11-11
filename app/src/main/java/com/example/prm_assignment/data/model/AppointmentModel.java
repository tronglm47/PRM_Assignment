package com.example.prm_assignment.data.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public class AppointmentModel {
    @SerializedName("_id")
    private String id;

    @SerializedName("customer_id")
    @JsonAdapter(CustomerInfoDeserializer.class)
    private CustomerInfo customerId;

    @SerializedName("vehicle_id")
    @JsonAdapter(VehicleInfoDeserializer.class)
    private VehicleInfo vehicleId;

    @SerializedName("center_id")
    @JsonAdapter(CenterInfoDeserializer.class)
    private CenterInfo centerId;

    @SerializedName("slot_id")
    @JsonAdapter(SlotInfoDeserializer.class)
    private SlotInfo slotId;

    @SerializedName("staffId")
    private Object staffIdRaw;

    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;
    private String updatedAt;

    public AppointmentModel() {
    }

    public String getId() {
        return id;
    }

    public CustomerInfo getCustomerId() {
        return customerId;
    }

    public VehicleInfo getVehicleId() {
        return vehicleId;
    }

    public CenterInfo getCenterId() {
        return centerId;
    }

    public SlotInfo getSlotId() {
        return slotId;
    }

    public String getStaffId() {
        if (staffIdRaw == null) return null;
        if (staffIdRaw instanceof String) return (String) staffIdRaw;
        return null;
    }

    public String getStartTime() {
        if (slotId != null && slotId.getSlotDate() != null && slotId.getStartTime() != null) {
            String date = slotId.getSlotDate().substring(0, 10);
            return date + "T" + slotId.getStartTime() + ":00.000Z";
        }
        return startTime;
    }

    public String getEndTime() {
        if (slotId != null && slotId.getSlotDate() != null && slotId.getEndTime() != null) {
            String date = slotId.getSlotDate().substring(0, 10);
            return date + "T" + slotId.getEndTime() + ":00.000Z";
        }
        return endTime;
    }

    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Inner classes for nested objects
    public static class CustomerInfo {
        @SerializedName("_id")
        public String id;
        public String customerName;
        public String address;
        public String dateOfBirth;

        public String getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getAddress() { return address; }
        public String getDateOfBirth() { return dateOfBirth; }
    }

    public static class VehicleInfo {
        @SerializedName("_id")
        public String id;
        public String vehicleName;
        public String model;
        public String plateNumber;
        public Integer mileage;

        public String getId() { return id; }
        public String getVehicleName() { return vehicleName; }
        public String getModel() { return model; }
        public String getPlateNumber() { return plateNumber; }
        public Integer getMileage() { return mileage; }
    }

    public static class CenterInfo {
        @SerializedName("_id")
        public String id;
        public String name;
        public String address;
        public String phone;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
    }

    public static class SlotInfo {
        @SerializedName("_id")
        public String id;
        @SerializedName("center_id")
        public String centerId;
        @SerializedName("start_time")
        public String startTime;
        @SerializedName("end_time")
        public String endTime;
        @SerializedName("slot_date")
        public String slotDate;
        public int capacity;
        @SerializedName("booked_count")
        public int bookedCount;
        public String status;

        public String getId() { return id; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getSlotDate() { return slotDate; }
    }

    // Custom deserializers to handle both string and object
    static class CustomerInfoDeserializer implements JsonDeserializer<CustomerInfo> {
        @Override
        public CustomerInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                CustomerInfo info = new CustomerInfo();
                info.id = json.getAsString();
                return info;
            } else if (json.isJsonObject()) {
                return context.deserialize(json, CustomerInfo.class);
            }
            return null;
        }
    }

    static class VehicleInfoDeserializer implements JsonDeserializer<VehicleInfo> {
        @Override
        public VehicleInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                VehicleInfo info = new VehicleInfo();
                info.id = json.getAsString();
                return info;
            } else if (json.isJsonObject()) {
                return context.deserialize(json, VehicleInfo.class);
            }
            return null;
        }
    }

    static class CenterInfoDeserializer implements JsonDeserializer<CenterInfo> {
        @Override
        public CenterInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                CenterInfo info = new CenterInfo();
                info.id = json.getAsString();
                return info;
            } else if (json.isJsonObject()) {
                return context.deserialize(json, CenterInfo.class);
            }
            return null;
        }
    }

    static class SlotInfoDeserializer implements JsonDeserializer<SlotInfo> {
        @Override
        public SlotInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                SlotInfo info = new SlotInfo();
                info.id = json.getAsString();
                return info;
            } else if (json.isJsonObject()) {
                return context.deserialize(json, SlotInfo.class);
            }
            return null;
        }
    }
}
