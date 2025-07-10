package com.example.javastripeapp.ui.activities.user.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.databinding.ActivityCustomerProfileBinding;
import com.example.javastripeapp.ui.activities.login.MainActivity;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;
import com.example.javastripeapp.ui.activities.user.common.MyCurrentJobsActivity;
import com.example.javastripeapp.ui.activities.workorder.create.WorkOrderActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerProfileActivity extends BaseActivity {
    private static final String TAG = "CustomerProfileActivity";
    private ActivityCustomerProfileBinding binding;
    private CustomerProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCustomerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(CustomerProfileViewModel.class);
        setupToolbar();

        retrieveUser();
        setUpClickListeners();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_customer_profile;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }

    private void setUpClickListeners() {
        binding.btnCustomerAction.setOnClickListener(v -> startWorkOrderActivity());
        binding.btnLogout.setOnClickListener(v -> signOutUser());
        binding.btnCurrentOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyCurrentJobsActivity.class);
            startActivity(intent);
        });
    }

    private void retrieveUser() {
        viewModel.retrieveUser()
                .addOnSuccessListener(this::setUpProfileUI)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve user from database");
                    showToast("Failed to retrieve user!");
                });
    }

    private void setUpProfileUI(User currentUser) {
        String username = currentUser.getUsername();
        binding.tvUsername.setText(username);
    }

    private void startWorkOrderActivity() {
        User currentUser = viewModel.getCurrentUser();
        Intent intent = new Intent(this, WorkOrderActivity.class);
        intent.putExtra("USER_ID", currentUser.getUserId());
        startActivity(intent);
    }

    private void signOutUser() {
        Intent intent = new Intent(this, MainActivity.class);
        viewModel.signOutUser();
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}