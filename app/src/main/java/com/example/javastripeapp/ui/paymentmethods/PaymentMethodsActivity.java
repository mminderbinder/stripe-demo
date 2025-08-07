package com.example.javastripeapp.ui.paymentmethods;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.databinding.ActivityPaymentMethodsBinding;
import com.example.javastripeapp.ui.BaseActivity;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

public class PaymentMethodsActivity extends BaseActivity {
    private static final String TAG = "PaymentMethodsActivity";
    private ActivityPaymentMethodsBinding binding;
    private PaymentMethodsViewModel viewModel;
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentMethodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();
        viewModel = new ViewModelProvider(this).get(PaymentMethodsViewModel.class);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        loadUserData();

        binding.btnManagePaymentMethods.setOnClickListener(v -> presentPaymentSheetFlow());
    }

    private void loadUserData() {
        viewModel.retrieveUser().addOnSuccessListener(user -> {
            Log.d(TAG, "User data loaded successfully");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user", e);
            showToast("Failed to load user data.");
            finish();
        });
    }

    private void presentPaymentSheetFlow() {
        User currentUser = viewModel.getCurrentUser();
        if (currentUser != null && currentUser.getStripeCustomerId() != null) {
            String stripeCustomerId = currentUser.getStripeCustomerId();
            createSetupIntentAndPresent(stripeCustomerId);
        } else {
            showToast("Payment setup incomplete. Please contact support.");
        }
    }

    private void createSetupIntentAndPresent(String stripeCustomerId) {
        viewModel.createPaymentSheetSetupIntent(stripeCustomerId)
                .addOnSuccessListener(this::presentPaymentSheet)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create setup intent", e);
                    showToast(e.getMessage());
                });
    }

    private void presentPaymentSheet(PaymentMethodsViewModel.PaymentSheetData result) {
        try {
            PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(
                    result.customerId(),
                    result.ephemeralKeySecret()
            );
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("ShovelHero")
                    .customer(customerConfig)
                    .allowsDelayedPaymentMethods(false)
                    .build();

            paymentSheet.presentWithSetupIntent(result.setupIntentClientSecret(), configuration);

        } catch (Exception e) {
            Log.e(TAG, "Error presenting PaymentSheet", e);
            showToast("Failed to open payment methods");
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            checkAndUpdatePaymentMethodStatus();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            checkAndUpdatePaymentMethodStatus();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed failed) {
            Log.e(TAG, "PaymentSheet failed: " + failed.getError().getLocalizedMessage());
            showToast("Unable to manage payment methods. Please try again.");
        }
    }

    private void checkAndUpdatePaymentMethodStatus() {
        User currentUser = viewModel.getCurrentUser();
        boolean hadPaymentMethods = currentUser.getHasPaymentMethod() != null && currentUser.getHasPaymentMethod();

        viewModel.hasPaymentMethods(currentUser.getStripeCustomerId()).addOnSuccessListener(hasPaymentMethods -> {
            if (hadPaymentMethods != hasPaymentMethods) {
                updateUserPaymentMethodStatus(currentUser, hasPaymentMethods);

                if (hasPaymentMethods) {
                    showToast("Payment method saved");
                } else {
                    showToast("Payment method removed");
                }
            }
            viewModel.updateCurrentUserPaymentMethodStatus(hasPaymentMethods);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to check payment method status", e);
            showToast("Changes saved");
        });
    }

    private void updateUserPaymentMethodStatus(User user, boolean hasPaymentMethod) {
        viewModel.updatePaymentMethodStatus(user.getUserId(), hasPaymentMethod)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User payment method status updated: " + hasPaymentMethod))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update payment method status", e));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_payment_methods;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}