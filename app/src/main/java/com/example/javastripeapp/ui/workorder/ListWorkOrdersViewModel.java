package com.example.javastripeapp.ui.workorder;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.repository.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.stream.Collectors;

public class ListWorkOrdersViewModel extends ViewModel {
    private static final String TAG = "ListWorkOrdersViewModel";
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();

    public Task<List<WorkOrder>> retrieveRequestedWorkOrders() {
        return workOrderRepo.fetchAllWorkOrders().continueWith(task -> {
            List<WorkOrder> workOrders = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve work orders");

            return workOrders.stream()
                    .filter(wo -> wo.getWorkOrderStatus().equals("JOB_REQUESTED"))
                    .peek(wo -> Log.d(TAG, wo.getWorkOrderId()))
                    .collect(Collectors.toList());
        });
    }
}
