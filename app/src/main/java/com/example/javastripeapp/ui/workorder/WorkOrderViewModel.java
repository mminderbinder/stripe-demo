package com.example.javastripeapp.ui.workorder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.LineItem;
import com.example.javastripeapp.data.LineItemType;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.WorkOrderStatus;
import com.example.javastripeapp.data.database.AddressRepo;
import com.example.javastripeapp.data.database.StripeCustomerRepo;
import com.example.javastripeapp.data.database.UserRepo;
import com.example.javastripeapp.data.database.WorkOrderRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkOrderViewModel extends ViewModel {
    private static final String TAG = "WorkOrderViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();
    private final StripeCustomerRepo customerRepo = new StripeCustomerRepo();
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();
    private final MutableLiveData<Double> _workOrderPrice = new MutableLiveData<>(4.33);
    public LiveData<Double> workOrderPrice = _workOrderPrice;
    private final MutableLiveData<Boolean> _drivewaySelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _walkwaySelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _sidewalkSelected = new MutableLiveData<>(false);
    private User currentUser;
    private WorkOrder workOrder;

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

    public Task<User> retrieveCurrentUser() {
        return userRepo.fetchUserInDatabase().continueWith(task -> {
            currentUser = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve current user");
            return currentUser;
        });
    }

    public Task<List<Address>> retrieveCustomerAddresses(String userId) {
        return addressRepo.fetchUserAddresses(userId);
    }

    public Task<Void> createWorkOrder(WorkOrder workOrder) {
        return workOrderRepo.createWorkOrder(workOrder);
    }

    public Task<StripeCustomerRepo.PaymentIntentForCheckoutResult> createPaymentIntentForCheckout(String stripeCustomerId, Address jobAddress) {
        Map<String, LineItem> lineItemMap = processLineItems();
        Double totalAmount = _workOrderPrice.getValue();

        if (currentUser == null) {
            return TaskUtils.forIllegalStateException("No current user");
        }

        WorkOrderStatus status = WorkOrderStatus.JOB_REQUESTED;

        workOrder = new WorkOrder(
                currentUser.getUserId(),
                lineItemMap,
                status,
                totalAmount,
                jobAddress
        );

        Map<String, Object> workOrderData = new HashMap<>();
        workOrderData.put("customerId", currentUser.getUserId());
        workOrderData.put("totalAmount", totalAmount);
        workOrderData.put("jobAddress", jobAddress.toStripeAddressMap());
        workOrderData.put("lineItemMap", convertLineItemsToMap(lineItemMap));

        return customerRepo.createPaymentIntentForCheckout(workOrderData, stripeCustomerId);
    }

    private Map<String, Object> convertLineItemsToMap(Map<String, LineItem> lineItemMap) {
        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, LineItem> entry : lineItemMap.entrySet()) {
            LineItem item = entry.getValue();
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("intItemCode", item.getIntItemCode());
            itemMap.put("description", item.getDescription());
            itemMap.put("amount", item.getAmount());
            itemMap.put("taxCode", item.getTaxCode());
            convertedMap.put(entry.getKey(), itemMap);
        }
        return convertedMap;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }
}