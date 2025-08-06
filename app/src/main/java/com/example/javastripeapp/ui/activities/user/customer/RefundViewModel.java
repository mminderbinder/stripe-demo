package com.example.javastripeapp.ui.activities.user.customer;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.repos.WorkOrderRepo;
import com.google.android.gms.tasks.Task;

public class RefundViewModel extends ViewModel {
    WorkOrderRepo workOrderRepo = new WorkOrderRepo();

    public Task<WorkOrder> fetchWorkOrder(String workOrderId) {
        return workOrderRepo.fetchWorkOrderById(workOrderId);
    }
}
