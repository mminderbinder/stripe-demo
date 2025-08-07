package com.example.javastripeapp.ui.profile.provider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.OnboardingStatus;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.databinding.ActivityProviderProfileBinding;
import com.example.javastripeapp.ui.BaseActivity;
import com.example.javastripeapp.ui.jobs.MyCurrentJobsActivity;
import com.example.javastripeapp.ui.login.MainActivity;
import com.example.javastripeapp.ui.workorder.ListWorkOrdersActivity;

import java.util.Objects;

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
        setUpObservers();
        retrieveUser();
        handleDeepLink(getIntent());
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_provider_profile;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.PROVIDER;
    }

    private void setUpObservers() {
        viewModel.onboardingStatus.observe(this, status -> {
            updateOnboardingUI(status);
        });
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
        binding.btnOnboardingAction.setOnClickListener(v -> handleOnboardingAction());
    }

    private void retrieveUser() {
        viewModel.retrieveUser()
                .addOnSuccessListener(user -> {
                    setUpProfileUI(user);
                    setUpClickListeners();
                    checkOnboardingStatus();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve user from database");
                    showToast("Failed to retrieve user!");
                });
    }

    private void checkOnboardingStatus() {
        showLoading();
        viewModel.checkOnboardingStatus().addOnSuccessListener(status -> {
            hideLoading();
            viewModel.updateOnboardingStatus(status);
        }).addOnFailureListener(e -> {
            hideLoading();
            showToast(e.getMessage());
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            viewModel.updateOnboardingStatus(OnboardingStatus.ERROR);
        });
    }

    private void handleOnboardingAction() {
        OnboardingStatus status = viewModel.onboardingStatus.getValue();
        if (status == null) {
            showToast("Unable to determine account status");
            return;
        }
        showLoading();
        switch (status) {
            case NOT_STARTED: {
                createConnectAccount();
                break;
            }
            case ACCOUNT_CREATED: {
                startOnboarding();
                break;
            }
            case ERROR: {
                checkOnboardingStatus();
                break;
            }
            case FULLY_ONBOARDED: {
                hideLoading();
                break;
            }
        }
    }

    private void createConnectAccount() {
        viewModel.createConnectAccount().addOnSuccessListener(unused -> {
            hideLoading();
            showToast("Account created successfully");
            retrieveUser();
        }).addOnFailureListener(e -> {
            hideLoading();
            showToast(e.getMessage());
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            viewModel.updateOnboardingStatus(OnboardingStatus.ERROR);
        });
    }

    private void startOnboarding() {
        viewModel.startOnboarding().addOnSuccessListener(url -> {
            hideLoading();
            openStripeOnboarding(url);
        }).addOnFailureListener(e -> {
            hideLoading();
            showToast(e.getMessage());
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            viewModel.updateOnboardingStatus(OnboardingStatus.ERROR);
        });
    }

    private void updateOnboardingUI(OnboardingStatus status) {
        if (status == null) return;

        switch (status) {
            case NOT_STARTED:
                binding.tvAccountStatus.setText("⚠️ Setup Required");
                binding.tvAccountDescription.setText("Complete your account setup to start receiving payments from customers.");
                binding.btnOnboardingAction.setText("COMPLETE SETUP");
                binding.btnOnboardingAction.setVisibility(View.VISIBLE);
                break;

            case ACCOUNT_CREATED:
                binding.tvAccountStatus.setText("🔄 Complete Onboarding");
                binding.tvAccountDescription.setText("Your account has been created. Complete the verification process to start receiving payments.");
                binding.btnOnboardingAction.setText("CONTINUE SETUP");
                binding.btnOnboardingAction.setVisibility(View.VISIBLE);
                break;

            case FULLY_ONBOARDED:
                binding.tvAccountStatus.setText("✅ Ready for Payouts");
                binding.tvAccountDescription.setText("Congratulations! Your account is fully set up and you can now receive payments from customers.");
                binding.btnOnboardingAction.setVisibility(View.GONE);
                break;

            case ERROR:
                binding.tvAccountStatus.setText("❌ Setup Error");
                binding.tvAccountDescription.setText("There was an issue with your account setup. Please try again or contact support if the problem persists.");
                binding.btnOnboardingAction.setText("RETRY");
                binding.btnOnboardingAction.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void openStripeOnboarding(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open onboarding URL", e);
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            showToast("Failed to open onboarding. Please try again later");
        }
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "com.example.javastripeapp".equals(data.getScheme())) {
            String path = data.getPath();
            if ("/onboarding/complete".equals(path)) {
                showToast("Welcome back. Checking your account status");
                checkOnboardingStatus();
            } else if ("/onboarding/refresh".equals(path)) {
                showToast("Refreshing onboarding link...");
                startOnboarding();
            }
        }
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

    private void showLoading() {
        binding.progressOnboarding.setVisibility(View.VISIBLE);
        binding.btnOnboardingAction.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressOnboarding.setVisibility(View.GONE);
        binding.btnOnboardingAction.setEnabled(true);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        User currentUser = viewModel.getCurrentUser();
        if (currentUser != null) {
            checkOnboardingStatus();
        }
    }
}