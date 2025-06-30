package com.example.javastripeapp.ui.activities.user.provider;

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
import com.example.javastripeapp.databinding.ActivityProviderProfileBinding;
import com.example.javastripeapp.ui.activities.login.MainActivity;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;
import com.example.javastripeapp.ui.activities.user.common.MyCurrentJobsActivity;
import com.example.javastripeapp.ui.activities.workorder.list.ListWorkOrdersActivity;

public class ProviderProfileActivity extends BaseActivity {

    private static final String TAG = "ProviderProfileActivity";
    private ActivityProviderProfileBinding binding;
    private ProviderViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProviderProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(ProviderViewModel.class);
        setupToolbar();
        retrieveUser();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_provider_profile;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.PROVIDER;
    }


    private void setUpClickListeners() {
        binding.btnProviderAction.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListWorkOrdersActivity.class);
            startActivity(intent);
        });
        binding.btnProviderJobs.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyCurrentJobsActivity.class);
            startActivity(intent);
        });
        binding.btnLogout.setOnClickListener(v -> signOutUser());
    }

    private void retrieveUser() {
        viewModel.retrieveUser()
                .addOnSuccessListener(user -> {
                    setUpProfileUI(user);
                    setUpClickListeners();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve user from database");
                    showToast("Failed to retrieve user!");
                });
    }

    private void setUpProfileUI(User currentUser) {
        String username = currentUser.getUsername();
        binding.tvUsername.setText(username);
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