package com.example.javastripeapp.ui.activities.workorder.create;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.WorkOrderStatus;
import com.example.javastripeapp.data.models.workorder.line_item.LineItem;
import com.example.javastripeapp.databinding.ActivityWorkOrderBinding;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;
import com.example.javastripeapp.ui.activities.user.customer.CustomerProfileActivity;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkOrderActivity extends BaseActivity {

    private static final String TAG = "WorkOrderActivity";
    private ActivityWorkOrderBinding binding;
    private WorkOrderViewModel viewModel;
    private Address selectedAddress;
    private PaymentSheet paymentSheet;

    // Store the work order for cleanup if needed
    private WorkOrder pendingWorkOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(WorkOrderViewModel.class);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        setupToolbar();
        retrieveUser();
        observeViewModel();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_work_order;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }

    private void observeViewModel() {
        viewModel.workOrderPrice.observe(this, price -> {
            showPrice();
        });
    }

    private void setUpClickListeners() {
        binding.btnOrder.setOnClickListener(v -> {
            validateOrder();
        });
        binding.btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerProfileActivity.class);
            startActivity(intent);
        });
        binding.acAddresses.setOnItemClickListener((parent, view, position, id) -> {
            selectedAddress = (Address) parent.getItemAtPosition(position);
        });
    }

    private void retrieveUser() {
        viewModel.retrieveCurrentUser().addOnSuccessListener(user -> {
            setUpClickListeners();
            setUpSwitchListeners();
            retrieveAddresses(user.getUserId());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to retrieve current user");
            showToast("Failed to load user. Please try again later");
        });
    }

    private void retrieveAddresses(String userId) {
        viewModel.retrieveCustomerAddresses(userId)
                .addOnSuccessListener(this::setUpAddressDropdown)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve addresses", e);
                });
    }

    private void setUpAddressDropdown(List<Address> addressList) {
        Log.d(TAG, "Retrieved addresses. Size: " + addressList.size());
        if (!addressList.isEmpty()) {
            ArrayAdapter<Address> addressArrayAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    addressList
            );
            addressArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            binding.acAddresses.setAdapter(addressArrayAdapter);
        }
    }

    private void setUpSwitchListeners() {
        binding.swDriveway.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            viewModel.toggleDriveway(isChecked);
        });
        binding.swWalkway.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            viewModel.toggleWalkway(isChecked);
        });
        binding.swSidewalk.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            viewModel.toggleSidewalk(isChecked);
        });
    }

    private void showPrice() {
        Double total = viewModel.workOrderPrice.getValue();
        binding.tvTitleTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    private void validateOrder() {
        if (binding.swDriveway.isChecked() || binding.swSidewalk.isChecked() || binding.swWalkway.isChecked()) {
            createWorkOrder();
        } else {
            showToast("Please add a work order item");
        }
    }

    private void createWorkOrder() {
        User currentUser = viewModel.getCurrentUser();
        Map<String, LineItem> lineItemMap = viewModel.processLineItems();
        WorkOrderStatus status = WorkOrderStatus.JOB_REQUESTED;
        Double total = viewModel.workOrderPrice.getValue();

        if (selectedAddress != null) {
            WorkOrder newWorkOrder = new WorkOrder(
                    currentUser.getUserId(),
                    lineItemMap,
                    status,
                    total,
                    selectedAddress);

            if (currentUser.getStripeCustomerId() == null || currentUser.getStripeCustomerId().isEmpty()) {
                showToast("Please contact support");
                return;
            }

            viewModel.createWorkOrder(newWorkOrder)
                    .addOnSuccessListener(unused -> {
                        this.pendingWorkOrder = newWorkOrder;
                        presentPaymentSheetForOrder(newWorkOrder);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create work order", e);
                        showToast("Failed to create order: " + e.getMessage());
                    });
        } else {
            showToast("Please select an address");
        }
    }

    private void presentPaymentSheetForOrder(WorkOrder workOrder) {
        User currentUser = viewModel.getCurrentUser();

        viewModel.createPaymentIntentForWorkOrder(workOrder, currentUser.getStripeCustomerId())
                .addOnSuccessListener(paymentIntentResult -> {
                    Log.d(TAG, "Payment intent created, presenting PaymentSheet");

                    try {
                        PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(
                                currentUser.getStripeCustomerId(),
                                paymentIntentResult.getEphemeralKeySecret()
                        );

                        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("ShovelHero")
                                .customer(customerConfig)
                                .allowsDelayedPaymentMethods(false)
                                .build();

                        paymentSheet.presentWithPaymentIntent(paymentIntentResult.getClientSecret(), configuration);

                    } catch (Exception e) {
                        Log.e(TAG, "Error presenting PaymentSheet", e);
                        showToast("Failed to open payment sheet: " + e.getMessage());
                        cleanupFailedOrder();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create payment intent for PaymentSheet", e);
                    showToast("Failed to setup payment: " + e.getMessage());
                    cleanupFailedOrder();
                });
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            showToast("Order placed successfully!");

            Intent intent = new Intent(this, CustomerProfileActivity.class);
            startActivity(intent);
            finish();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            showToast("Payment canceled");
            cleanupFailedOrder();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed failed) {
            Log.e(TAG, "Payment failed: " + failed.getError().getLocalizedMessage());
            showToast("Payment failed. Please try again.");
            cleanupFailedOrder();
        }
    }

    private void cleanupFailedOrder() {
        if (pendingWorkOrder != null && pendingWorkOrder.getWorkOrderId() != null) {
            viewModel.deleteWorkOrder(pendingWorkOrder.getWorkOrderId())
                    .addOnSuccessListener(unused -> Log.d(TAG, "Work order cleaned up"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to cleanup work order", e));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}