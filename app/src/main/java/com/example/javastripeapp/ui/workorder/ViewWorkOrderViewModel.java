package com.example.javastripeapp.ui.workorder;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.WorkOrderAction;
import com.example.javastripeapp.data.database.StripeProviderRepo;
import com.example.javastripeapp.data.database.UserRepo;
import com.example.javastripeapp.data.database.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class ViewWorkOrderViewModel extends ViewModel {
    private static final String TAG = "ViewWorkOrderViewModel";
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();
    private final UserRepo userRepo = new UserRepo();
    private final StripeProviderRepo providerRepo = new StripeProviderRepo();
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

    public Task<Void> updateOrder(WorkOrderAction action) {
        WorkOrder currentOrder = getWorkOrder();
        String workOrderId = currentOrder.getWorkOrderId();
        Map<String, Object> updateMap = processUpdateMap(action);

        return workOrderRepo.updateWorkOrder(workOrderId, updateMap);
    }

    private Map<String, Object> processUpdateMap(WorkOrderAction action) {
        Map<String, Object> updates = new HashMap<>();
        User user = getCurrentUser();
        switch (action) {
            case ACCEPT_ORDER: {
                updates.put("workOrderStatus", "JOB_ACCEPTED");
                updates.put("providerId", user.getUserId());
                updates.put("stripeAccountId", user.getStripeAccountId());
                break;
            }
            case CANCEL_ORDER_CUSTOMER: {
                updates.put("workOrderStatus", "JOB_CANCELED");
                break;
            }
            case CANCEL_ORDER_PROVIDER: {
                updates.put("workOrderStatus", "JOB_REQUESTED");
                updates.put("providerId", null);
                updates.put("stripeAccountId", null);
                break;
            }
            case FULFILL_ORDER: {
                updates.put("workOrderStatus", "JOB_FULFILLED");
                break;
            }
        }
        updates.put("updatedAt", ServerValue.TIMESTAMP);
        return updates;
    }

    public Task<Void> capturePaymentForCompletedOrder() {
        WorkOrder currentOrder = getWorkOrder();
        String workOrderId = currentOrder.getWorkOrderId();
        String paymentIntentId = currentOrder.getPaymentIntentId();

        return providerRepo.capturePaymentForCompletedService(workOrderId, paymentIntentId);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }
}
