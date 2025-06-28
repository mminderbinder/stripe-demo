package com.example.javastripeapp.ui.activities.workorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.WorkOrderStatus;
import com.example.javastripeapp.data.models.workorder.line_item.LineItem;
import com.example.javastripeapp.databinding.ActivityWorkOrderBinding;
import com.example.javastripeapp.ui.activities.user.customer.CustomerProfileActivity;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkOrderActivity extends AppCompatActivity {

    private static final String TAG = "WorkOrderActivity";
    private ActivityWorkOrderBinding binding;
    private WorkOrderViewModel viewModel;
    private Address selectedAddress;

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
        retrieveUser();
        observeViewModel();
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
        binding.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
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

            viewModel.createWorkOrder(newWorkOrder).addOnSuccessListener(unused -> {
                Intent intent = new Intent(this, CustomerProfileActivity.class);
                showToast("Order created successfully");
                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create work order", e);
                showToast("Failed to create work order. Please try again later");
            });
        } else {
            showToast("Please select an address");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}