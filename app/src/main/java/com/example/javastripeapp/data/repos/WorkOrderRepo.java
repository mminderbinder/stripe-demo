package com.example.javastripeapp.data.repos;

import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.google.android.gms.tasks.Task;

import java.util.List;

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

    public Task<List<WorkOrder>> fetchAllWorkOrders() {
        return workOrderRef.getAllObjects();
    }
}
