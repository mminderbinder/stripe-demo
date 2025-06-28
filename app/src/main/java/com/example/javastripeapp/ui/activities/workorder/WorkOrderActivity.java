package com.example.javastripeapp.ui.activities.workorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.databinding.ActivityWorkOrderBinding;
import com.example.javastripeapp.ui.activities.user.customer.CustomerProfileActivity;

import java.util.List;

public class WorkOrderActivity extends AppCompatActivity {

    private static final String TAG = "WorkOrderActivity";
    private ActivityWorkOrderBinding binding;
    private WorkOrderViewModel viewModel;

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

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("CURRENT_USER")) {
                User currentUser = intent.getParcelableExtra("CURRENT_USER");
                if (currentUser != null) {
                    setUpClickListeners();
                    retrieveAddresses(currentUser.getUserId());
                }
            }
        }
    }

    private void setUpClickListeners() {

        binding.btnOrder.setOnClickListener(v -> {

        });
        binding.btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerProfileActivity.class);
            startActivity(intent);
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
}