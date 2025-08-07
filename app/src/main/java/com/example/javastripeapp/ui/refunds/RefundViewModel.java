package com.example.javastripeapp.ui.refunds;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.repository.StripeCustomerRepo;
import com.example.javastripeapp.data.repository.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

public class RefundViewModel extends ViewModel {
    WorkOrderRepo workOrderRepo = new WorkOrderRepo();
    StripeCustomerRepo customerRepo = new StripeCustomerRepo();
    private WorkOrder currentOrder;

    public Task<WorkOrder> fetchWorkOrder(String workOrderId) {
        return workOrderRepo.fetchWorkOrderById(workOrderId).continueWith(task -> {
            currentOrder = TaskUtils.getTaskResultOrThrow(task, "Failed to fetch work order");
            return currentOrder;
        });
    }

    Task<StripeCustomerRepo.RefundResult> requestRefundFromPlatform(String workOrderId) {
        return customerRepo.requestRefundFromPlatform(workOrderId);
    }

    public WorkOrder getCurrentOrder() {
        return currentOrder;
    }
}
