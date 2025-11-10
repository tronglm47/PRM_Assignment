package com.example.prm_assignment.data.model;

import java.util.List;

public class SlotsResponse {
    private boolean success;
    private List<SlotModel> data;

    public boolean isSuccess() { return success; }
    public List<SlotModel> getData() { return data; }
}

