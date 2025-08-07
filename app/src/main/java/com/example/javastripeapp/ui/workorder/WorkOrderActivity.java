package com.example.javastripeapp.ui.workorder;

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
import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.repository.StripeCustomerRepo;
import com.example.javastripeapp.databinding.ActivityWorkOrderBinding;
import com.example.javastripeapp.ui.BaseActivity;
import com.example.javastripeapp.ui.profile.customer.CustomerProfileActivity;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.List;
import java.util.Locale;

public class WorkOrderActivity extends BaseActivity {

    private static final String TAG = "WorkOrderActivity";
    private ActivityWorkOrderBinding binding;
    private WorkOrderViewModel viewModel;
    private Address selectedAddress;
    private PaymentSheet paymentSheet;
    private String pendingPaymentIntentId;

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
            createPaymentIntentAndShowPaymentSheet();
        } else {
            showToast("Please add a work order item");
        }
    }

    private void createPaymentIntentAndShowPaymentSheet() {
        User currentUser = viewModel.getCurrentUser();

        if (selectedAddress == null) {
            showToast("Please select an address");
            return;
        }

        if (currentUser.getStripeCustomerId() == null || currentUser.getStripeCustomerId().isEmpty()) {
            showToast("Please contact support");
            return;
        }

        viewModel.createPaymentIntentForCheckout(currentUser.getStripeCustomerId(), selectedAddress)
                .addOnSuccessListener(paymentIntentResult -> {
                    pendingPaymentIntentId = paymentIntentResult.paymentIntentId();

                    presentPaymentSheet(paymentIntentResult, currentUser);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create payment intent", e);
                    showToast("Failed to create payment intent: " + e.getMessage());
                });
    }

    private void presentPaymentSheet(StripeCustomerRepo.PaymentIntentForCheckoutResult paymentIntentResult, User currentUser) {
        try {
            PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(
                    currentUser.getStripeCustomerId(),
                    paymentIntentResult.ephemeralKeySecret()
            );

            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("ShovelHero")
                    .customer(customerConfig)
                    .allowsDelayedPaymentMethods(false)
                    .build();

            paymentSheet.presentWithPaymentIntent(paymentIntentResult.clientSecret(), configuration);

        } catch (Exception e) {
            Log.e(TAG, "Error presenting PaymentSheet", e);
            showToast("Failed to open payment sheet: " + e.getMessage());
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment completed successfully");
            createWorkOrderAfterPayment();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
            showToast("Payment canceled");
            pendingPaymentIntentId = null;

        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed failed) {
            Log.e(TAG, "Payment failed: " + failed.getError().getLocalizedMessage());
            showToast("Payment failed. Please try again.");
            pendingPaymentIntentId = null;
        }
    }

    private void createWorkOrderAfterPayment() {
        if (pendingPaymentIntentId == null) {
            Log.e(TAG, "No payment intent ID available for order creation");
            showToast("Error: No payment information available");
            return;
        }
        WorkOrder workOrder = viewModel.getWorkOrder();
        workOrder.setPaymentIntentId(pendingPaymentIntentId);

        viewModel.createWorkOrder(workOrder).addOnSuccessListener(unused -> {
            Log.d(TAG, "Work order created successfully");
            showToast("Work order created successfully");
            Intent intent = new Intent(this, CustomerProfileActivity.class);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to create work order", e);
            showToast("Failed to create work order");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}