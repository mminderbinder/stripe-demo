package com.example.javastripeapp.ui.activities.workorder.view;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.data.repos.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

public class ViewWorkOrderViewModel extends ViewModel {
    private static final String TAG = "ViewWorkOrderViewModel";
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();
    private final UserRepo userRepo = new UserRepo();
    private User currentUser;
    private WorkOrder workOrder;

    public Task<WorkOrder> retrieveWorkOrder(String workOrderId) {
        return workOrderRepo.fetchWorkOrderById(workOrderId).continueWith(task -> {
            workOrder = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve work order");
            return workOrder;
        });
    }

    public Task<User> retrieveCurrentUser() {
        return userRepo.fetchUserInDatabase().continueWith(task -> {
            currentUser = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve current user");
            return currentUser;
        });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }
}
