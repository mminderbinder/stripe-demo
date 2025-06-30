package com.example.javastripeapp.ui.activities.registration;

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
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.databinding.ActivityRegistrationBinding;
import com.example.javastripeapp.ui.activities.login.MainActivity;
import com.example.javastripeapp.ui.activities.user.customer.CustomerProfileActivity;
import com.example.javastripeapp.ui.activities.user.provider.ProviderProfileActivity;
import com.example.javastripeapp.utils.TaskUtils;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";
    private ActivityRegistrationBinding binding;
    private RegistrationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpClickListeners();

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
    }

    private void setUpClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            validateFields();
        });
        binding.btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }


    private void validateFields() {
        String username = String.valueOf(binding.etUsername.getText()).trim();
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String password = String.valueOf(binding.etPassword.getText()).trim();
        String confirmPassword = String.valueOf(binding.etConfirmPassword.getText()).trim();

        if (username.isEmpty()) {
            showToast("Please enter a username");
            return;
        }
        if (email.isEmpty()) {
            showToast("Please enter your email");
            return;
        }
        if (password.isEmpty()) {
            showToast("Please enter your password");
            return;
        }
        if (confirmPassword.isEmpty()) {
            showToast("Please confirm your password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }
        boolean isCustomer = binding.rbCustomer.isChecked();
        AccountType accountType = isCustomer ? AccountType.CUSTOMER : AccountType.PROVIDER;

        User newUser = new User(username, email, accountType);
        createUser(newUser, password, username);
    }

    private void createUser(User user, String password, String username) {
        viewModel.createUserInAuth(user.getEmail(), password, username).continueWithTask(authTask -> {
            if (!authTask.isSuccessful()) {
                return TaskUtils.forTaskException(authTask, "Failed to create user in Firebase Auth");
            }
            user.setUserId(authTask.getResult());

            return viewModel.createUserInDatabase(user).addOnSuccessListener(unused -> {
                AccountType accountType = AccountType.fromString(user.getAccountType());
                if (accountType != null) {
                    redirectToProfile(accountType);
                }

            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create new user", e);
                Toast.makeText(this, "Failed to create user. Please try later or contact support", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void redirectToProfile(AccountType accountType) {
        Intent intent = null;
        if (accountType.equals(AccountType.CUSTOMER)) {
            intent = new Intent(this, CustomerProfileActivity.class);
        } else if (accountType.equals(AccountType.PROVIDER)) {
            intent = new Intent(this, ProviderProfileActivity.class);
        } else {
            showToast("Failed to get account type. Please try later");
        }
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}