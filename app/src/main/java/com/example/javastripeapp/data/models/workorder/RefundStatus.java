package com.example.javastripeapp.data.models.workorder;

public enum RefundStatus {
    PROCESSED("processed"),
    QUEUED("queued"),
    FAILED("failed");

    private final String value;

    RefundStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RefundStatus fromValue(String value) {
        for (RefundStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return FAILED;
    }
}
