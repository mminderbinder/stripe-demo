package com.example.javastripeapp.ui.activities.user.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.databinding.ActivityCustomerProfileBinding;
import com.example.javastripeapp.ui.activities.login.MainActivity;
import com.example.javastripeapp.ui.activities.workorder.WorkOrderActivity;

public class CustomerProfileActivity extends AppCompatActivity {
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

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra("CURRENT_USER")) {
                User user = intent.getParcelableExtra("CURRENT_USER");
                if (user == null) return;
                viewModel.setCurrentUser(user);
                setUpProfileUI(user);
            } else {
                retrieveUser();
            }
            setUpClickListeners();
        }
    }

    private void setUpClickListeners() {
        binding.btnCustomerAction.setOnClickListener(v -> startWorkOrderActivity());
        binding.btnLogout.setOnClickListener(v -> signOutUser());
    }

    private void retrieveUser() {
        viewModel.retrieveUserById()
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
        intent.putExtra("CURRENT_USER", currentUser);
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