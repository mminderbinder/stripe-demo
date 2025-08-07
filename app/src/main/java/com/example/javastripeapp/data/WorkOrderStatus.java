package com.example.javastripeapp.data;

public enum WorkOrderStatus {
    JOB_REQUESTED, JOB_ACCEPTED, JOB_CANCELED, JOB_FULFILLED, REFUND_QUEUED, FULLY_REFUNDED, REFUND_FAILED;

    public static WorkOrderStatus fromString(String status) {
        return switch (status) {
            case "JOB_REQUESTED" -> JOB_REQUESTED;
            case "JOB_ACCEPTED" -> JOB_ACCEPTED;
            case "JOB_CANCELED_CUSTOMER" -> JOB_CANCELED;
            case "JOB_FULFILLED" -> JOB_FULFILLED;
            case "REFUND_QUEUED" -> REFUND_QUEUED;
            case "FULLY_REFUNDED" -> FULLY_REFUNDED;
            case "REFUND_FAILED" -> REFUND_FAILED;
            default -> null;
        };
    }
}
