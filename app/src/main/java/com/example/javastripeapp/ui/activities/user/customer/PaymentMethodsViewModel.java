package com.example.javastripeapp.ui.activities.user.customer;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.repos.StripeCustomerRepo;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

public class PaymentMethodsViewModel extends ViewModel {
    private static final String TAG = "PaymentMethodsViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final StripeCustomerRepo customerRepo = new StripeCustomerRepo();
    private User currentUser;

    public Task<User> retrieveUser() {
        return userRepo.fetchUserInDatabase().continueWith(task -> {
            currentUser = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve current user");
            return currentUser;
        });
    }

    public Task<StripeCustomerRepo.CustomerSheetSetUpResult> createPaymentSheetIntent(String stripeId) {
        return customerRepo.createCustomerSheetIntent(stripeId);
    }


    public User getCurrentUser() {
        return currentUser;
    }
}
