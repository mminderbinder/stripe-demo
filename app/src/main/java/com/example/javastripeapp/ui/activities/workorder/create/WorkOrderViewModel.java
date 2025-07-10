package com.example.javastripeapp.ui.activities.workorder.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.line_item.LineItem;
import com.example.javastripeapp.data.models.workorder.line_item.LineItemType;
import com.example.javastripeapp.data.repos.AddressRepo;
import com.example.javastripeapp.data.repos.StripeCustomerRepo;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.data.repos.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkOrderViewModel extends ViewModel {
    private static final String TAG = "WorkOrderViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();
    private final StripeCustomerRepo customerRepo = new StripeCustomerRepo();

    private final MutableLiveData<Double> _workOrderPrice = new MutableLiveData<>(4.33);
    public LiveData<Double> workOrderPrice = _workOrderPrice;

    private final MutableLiveData<Boolean> _drivewaySelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _walkwaySelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _sidewalkSelected = new MutableLiveData<>(false);
    private User currentUser;

    public void toggleDriveway(boolean isChecked) {
        if (Boolean.TRUE.equals(_drivewaySelected.getValue()) != isChecked) {
            _drivewaySelected.setValue(isChecked);
            updatePrice(isChecked, LineItemType.DRIVEWAY.getPrice());
        }
    }

    public void toggleWalkway(boolean isChecked) {
        if (Boolean.TRUE.equals(_walkwaySelected.getValue()) != isChecked) {
            _walkwaySelected.setValue(isChecked);
            updatePrice(isChecked, LineItemType.WALKWAY.getPrice());
        }
    }

    public void toggleSidewalk(boolean isChecked) {
        if (Boolean.TRUE.equals(_sidewalkSelected.getValue()) != isChecked) {
            _sidewalkSelected.setValue(isChecked);
            updatePrice(isChecked, LineItemType.SIDEWALK.getPrice());
        }
    }

    public void updatePrice(boolean isAdding, double itemPrice) {
        double currentPrice = _workOrderPrice.getValue() != null ? _workOrderPrice.getValue() : LineItemType.SERVICE_FEE.getPrice();
        double newPrice = isAdding ? currentPrice + itemPrice : currentPrice - itemPrice;
        _workOrderPrice.setValue(newPrice);
    }

    public Map<String, LineItem> processLineItems() {
        Map<String, LineItem> lineItemMap = new HashMap<>();

        lineItemMap.put(LineItemType.SERVICE_FEE.getItemCode(), LineItemType.SERVICE_FEE.createLineItem());

        if (Boolean.TRUE.equals(_drivewaySelected.getValue())) {
            lineItemMap.put(LineItemType.DRIVEWAY.getItemCode(), LineItemType.DRIVEWAY.createLineItem());
        }
        if (Boolean.TRUE.equals(_walkwaySelected.getValue())) {
            lineItemMap.put(LineItemType.WALKWAY.getItemCode(), LineItemType.WALKWAY.createLineItem());
        }
        if (Boolean.TRUE.equals(_sidewalkSelected.getValue())) {
            lineItemMap.put(LineItemType.SIDEWALK.getItemCode(), LineItemType.SIDEWALK.createLineItem());
        }
        return lineItemMap;
    }

    public Task<Void> createWorkOrder(WorkOrder workOrder) {
        return workOrderRepo.createWorkOrder(workOrder);
    }

    public Task<User> retrieveCurrentUser() {
        return userRepo.fetchUserInDatabase().continueWith(task -> {
            currentUser = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve current user");
            return currentUser;
        });
    }

    public Task<StripeCustomerRepo.PaymentIntentResult> createPaymentIntentForWorkOrder(WorkOrder workOrder, String stripeCustomerId) {
        return customerRepo.createPaymentIntent(workOrder, stripeCustomerId);
    }

    public Task<List<Address>> retrieveCustomerAddresses(String userId) {
        return addressRepo.fetchUserAddresses(userId);
    }

    public Task<Void> deleteWorkOrder(String workOrderId) {
        return workOrderRepo.deleteWorkOrder(workOrderId);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}