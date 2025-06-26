package com.example.javastripeapp.data.models.work_order;

import com.example.javastripeapp.data.models.work_order.line_item.LineItem;
import com.google.firebase.database.ServerValue;

import java.util.Map;

public class WorkOrder {
    private String workOrderId;
    private String customerId;
    private String providerId;
    private Object createdAt;
    private Object updatedAt;
    private Map<String, LineItem> lineItemMap;
    private String workOrderStatus;
    private Double totalAmount;
    private String jobAddress;

    public WorkOrder() {

    }

    public WorkOrder(String workOrderId, String customerId, String providerId, Map<String, LineItem> lineItemMap, WorkOrderStatus workOrderStatus, Double totalAmount, String jobAddress) {
        this.workOrderId = workOrderId;
        this.customerId = customerId;
        this.providerId = providerId;
        this.lineItemMap = lineItemMap;
        this.createdAt = ServerValue.TIMESTAMP;
        this.updatedAt = ServerValue.TIMESTAMP;
        this.workOrderStatus = workOrderStatus.name();
        this.totalAmount = totalAmount;
        this.jobAddress = jobAddress;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, LineItem> getLineItemMap() {
        return lineItemMap;
    }

    public void setLineItemMap(Map<String, LineItem> lineItemMap) {
        this.lineItemMap = lineItemMap;
    }

    public String getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(String workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getJobAddress() {
        return jobAddress;
    }

    public void setJobAddress(String jobAddress) {
        this.jobAddress = jobAddress;
    }
}
