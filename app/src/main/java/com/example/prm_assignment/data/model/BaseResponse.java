package com.example.prm_assignment.data.model;

/**
 * Model phản hồi chung từ server.
 * Dùng cho các API chỉ trả về trạng thái success/message (ví dụ: tạo Appointment).
 */
public class BaseResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
