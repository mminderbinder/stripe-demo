package com.example.javastripeapp.ui.activities.workorder.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.WorkOrderAction;
import com.example.javastripeapp.data.models.workorder.WorkOrderStatus;
import com.example.javastripeapp.databinding.ActivityViewWorkOrderBinding;
import com.example.javastripeapp.ui.activities.user.customer.CustomerProfileActivity;
import com.example.javastripeapp.ui.activities.user.provider.ProviderProfileActivity;
import com.example.javastripeapp.utils.DateUtils;

public class ViewWorkOrderActivity extends AppCompatActivity {
    private static final String TAG = "ViewWorkOrderActivity";
    private ActivityViewWorkOrderBinding binding;
    private ViewWorkOrderViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityViewWorkOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(ViewWorkOrderViewModel.class);

        hideAllButtons();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("WO_ID")) {
                String workOrderId = intent.getStringExtra("WO_ID");
                if (workOrderId != null) {
                    retrieveWorkOrder(workOrderId);
                    setUpClickListeners();
                }
            }
        }
    }

    private void setUpClickListeners() {
        binding.btnAccept.setOnClickListener(v -> acceptOrder());
        binding.btnCancel.setOnClickListener(v -> cancelOrder());
        binding.btnDone.setOnClickListener(v -> completeOrder());
    }

    private void retrieveWorkOrder(String workOrderId) {
        viewModel.retrieveWorkOrder(workOrderId)
                .addOnSuccessListener(this::setWorkOrderInfo)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve work order", e);
                    showToast("Failed to retrieve order. Please try later");
                });
    }

    private void setWorkOrderInfo(WorkOrder workOrder) {
        binding.tvWorkOrderId.setText(workOrder.getWorkOrderId());
        binding.tvStatus.setText(workOrder.getWorkOrderStatus());
        binding.tvTotalAmount.setText("$" + workOrder.getTotalAmount());
        binding.tvCustomerId.setText(workOrder.getCustomerId());

        if (workOrder.getProviderId() != null) {
            binding.tvProviderId.setText(workOrder.getProviderId());
        }
        Address workOrderAddress = workOrder.getJobAddress();
        binding.tvJobAddress.setText(workOrderAddress.getFormattedAddress());

        String createdAt = DateUtils.formatCurrentDateTime((Long) workOrder.getCreatedAt());
        String updatedAt = DateUtils.formatCurrentDateTime((Long) workOrder.getUpdatedAt());
        binding.tvCreatedAt.setText(createdAt);
        binding.tvUpdatedAt.setText(updatedAt);

        retrieveUser();
    }

    private void retrieveUser() {
        viewModel.retrieveCurrentUser()
                .addOnSuccessListener(this::setActionButtonVisibilities)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve current user", e);
                });
    }

    private void setActionButtonVisibilities(User user) {
        WorkOrder workOrder = viewModel.getWorkOrder();
        WorkOrderStatus status = WorkOrderStatus.fromString(workOrder.getWorkOrderStatus());
        AccountType accountType = AccountType.fromString(user.getAccountType());

        if (status == null || accountType == null) {
            showToast("Failed to retrieve user info");
            return;
        }
        if (accountType.equals(AccountType.CUSTOMER)) {
            binding.btnAccept.setVisibility(View.GONE);
            binding.btnDone.setVisibility(View.GONE);

            switch (status) {
                case JOB_REQUESTED, JOB_ACCEPTED: {
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    hideAllButtons();
            }
        } else if (accountType.equals(AccountType.PROVIDER)) {
            switch (status) {
                case JOB_REQUESTED: {
                    binding.btnAccept.setVisibility(View.VISIBLE);
                    break;
                }
                case JOB_ACCEPTED: {
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnDone.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    hideAllButtons();
            }
        }
    }

    private void acceptOrder() {
        Intent intent = new Intent(this, ProviderProfileActivity.class);
        updateOrderAndRedirect(WorkOrderAction.ACCEPT_ORDER, intent);
    }

    private void cancelOrder() {
        User currentUser = viewModel.getCurrentUser();
        AccountType accountType = AccountType.fromString(currentUser.getAccountType());
        if (accountType == null) return;

        Intent intent;
        if (accountType.equals(AccountType.CUSTOMER)) {
            intent = new Intent(this, CustomerProfileActivity.class);
            updateOrderAndRedirect(WorkOrderAction.CANCEL_ORDER_CUSTOMER, intent);

        } else if (accountType.equals(AccountType.PROVIDER)) {
            intent = new Intent(this, ProviderProfileActivity.class);
            updateOrderAndRedirect(WorkOrderAction.CANCEL_ORDER_PROVIDER, intent);
        } else {
            showToast("Failed to cancel work order. Please try later");
        }
    }

    private void completeOrder() {
        Intent intent = new Intent(this, ProviderProfileActivity.class);
        updateOrderAndRedirect(WorkOrderAction.FULFILL_ORDER, intent);
    }

    private void updateOrderAndRedirect(WorkOrderAction action, Intent intent) {
        viewModel.updateOrder(action).addOnSuccessListener(unused -> {
            showToast("Order updated successfully");
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to update work order", e);
            showToast("Order update failed. Please try again later");
        });
    }
    
    private void hideAllButtons() {
        binding.btnAccept.setVisibility(View.GONE);
        binding.btnCancel.setVisibility(View.GONE);
        binding.btnDone.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}