package com.example.javastripeapp.data.models.workorder;

public enum WorkOrderStatus {
    JOB_REQUESTED, JOB_ACCEPTED, JOB_CANCELED, JOB_FULFILLED;

    public static WorkOrderStatus fromString(String status) {
        return switch (status) {
            case "JOB_REQUESTED" -> JOB_REQUESTED;
            case "JOB_ACCEPTED" -> JOB_ACCEPTED;
            case "JOB_CANCELED" -> JOB_CANCELED;
            case "JOB_FULFILLED" -> JOB_FULFILLED;
            default -> null;
        };
    }
}
