package com.example.javastripeapp.data.repos;

import android.util.Log;

import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class UserRepo {
    private static final String TAG = "UserRepo";

    private final GenericReference<User> userRef;
    private final AuthRepo authRepo = new AuthRepo();
    private final FirebaseFunctions functions = FirebaseFunctions.getInstance();

    public UserRepo() {
        this.userRef = new GenericReference<>("users", User.class);
    }

    public Task<Void> createUserInDatabase(User newUser) {
        String userId = newUser.getUserId();

        return userRef.createObjectWithId(newUser, userId).continueWithTask(task -> {
            TaskUtils.getTaskResultOrThrow(task, "Failed to create user in database");

            AccountType accountType = AccountType.fromString(newUser.getAccountType());
            if (accountType == null) {
                return TaskUtils.forIllegalStateException("Current user has no account type!");
            }

            if (accountType.equals(AccountType.CUSTOMER)) {
                return createStripeCustomer(newUser);
            }

            return Tasks.forResult(null);
        });
    }

    private Task<Void> createStripeCustomer(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("accountType", user.getAccountType());
        data.put("displayName", user.getUsername());

        return functions.getHttpsCallable("createCustomer")
                .call(data)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Stripe customer creation failed", task.getException());
                        throw new RuntimeException("Failed to create Stripe customer");
                    }
                    Log.d(TAG, "Stripe customer created successfully");
                    return null;
                });
    }

    public Task<User> fetchUserInDatabase() {
        return authRepo.fetchCurrentUserUid().continueWithTask(authTask -> {
            if (!authTask.isSuccessful()) {
                return TaskUtils.forTaskException(authTask, "Failed to retrieve user Uid");
            }
            String userId = authTask.getResult();
            return userRef.getObject(userId);
        });
    }

    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
        return userRef.updateObject(userId, updates);
    }
}