package com.example.javastripeapp.data.repository.firebase;

import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Map;

public class WorkOrderRepo {
    private final GenericReference<WorkOrder> workOrderRef;

    public WorkOrderRepo() {
        this.workOrderRef = new GenericReference<>("workOrders", WorkOrder.class);
    }

    public Task<Void> createWorkOrder(WorkOrder workOrder) {
        return workOrderRef.createNewObject(workOrder);
    }

    public Task<WorkOrder> fetchWorkOrderById(String workOrderId) {
        return workOrderRef.getObject(workOrderId);
    }

    public Task<List<WorkOrder>> fetchWorkOrdersByUser(User user) {
        String userId = user.getUserId();
        AccountType accountType = AccountType.fromString(user.getAccountType());

        if (accountType == null) {
            return TaskUtils.forIllegalStateException("Account type is null");
        }
        String fieldName = retrieveAccountTypeField(accountType);
        return workOrderRef.findByField(fieldName, userId);
    }

    public Task<List<WorkOrder>> fetchAllWorkOrders() {
        return workOrderRef.getAllObjects();
    }

    public Task<Void> updateWorkOrder(String workOrderId, Map<String, Object> updates) {
        return workOrderRef.updateObject(workOrderId, updates);
    }

    private String retrieveAccountTypeField(AccountType accountType) {
        return switch (accountType) {
            case PROVIDER -> "providerId";
            case CUSTOMER -> "customerId";
        };
    }

    public Task<Void> deleteWorkOrder(String workOrderId) {
        return workOrderRef.deleteObject(workOrderId);
    }
}
