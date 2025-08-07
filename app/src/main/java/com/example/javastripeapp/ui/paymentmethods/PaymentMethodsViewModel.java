package com.example.javastripeapp.ui.paymentmethods;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.repository.StripeCustomerRepo;
import com.example.javastripeapp.data.repository.UserRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsViewModel extends ViewModel {
    private static final String TAG = "PaymentMethodsViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final StripeCustomerRepo stripeCustomerRepo = new StripeCustomerRepo();

    private User currentUser;

    public Task<User> retrieveUser() {
        return userRepo.fetchUserInDatabase()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        this.currentUser = task.getResult();
                    }
                    return task.getResult();
                });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Task<PaymentSheetData> createPaymentSheetSetupIntent(String stripeCustomerId) {
        return stripeCustomerRepo.createCustomerSheetSetupIntent(stripeCustomerId)
                .continueWith(task -> {
                    StripeCustomerRepo.CustomerSheetSetupResult result = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve result");
                    return new PaymentSheetData(
                            result.getCustomerId(),
                            result.getSetupIntentClientSecret(),
                            result.getEphemeralKeySecret()
                    );
                });
    }

    public Task<Boolean> hasPaymentMethods(String stripeCustomerId) {
        return stripeCustomerRepo.hasPaymentMethods(stripeCustomerId);
    }

    public Task<Void> updatePaymentMethodStatus(String userId, boolean hasPaymentMethod) {
        Map<String, Object> updatesMap = new HashMap<>();
        updatesMap.put("hasPaymentMethod", hasPaymentMethod);
        return userRepo.updateUser(userId, updatesMap);
    }

    public void updateCurrentUserPaymentMethodStatus(boolean hasPaymentMethod) {
        if (currentUser != null) {
            currentUser.setHasPaymentMethod(hasPaymentMethod);
        }
    }

    /**
     * Simple record for PaymentSheet setup
     */
    public record PaymentSheetData(String customerId, String setupIntentClientSecret,
                                   String ephemeralKeySecret) {
    }
}
