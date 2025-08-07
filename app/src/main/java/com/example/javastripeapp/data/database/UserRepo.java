package com.example.javastripeapp.data.database;

import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Map;

public class UserRepo {
    private static final String TAG = "UserRepo";
    private final GenericReference<User> userRef;
    private final AuthRepo authRepo = new AuthRepo();


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
            StripeCustomerRepo customerRepo = new StripeCustomerRepo();

            if (accountType.equals(AccountType.CUSTOMER)) {
                return customerRepo.createStripeCustomer(newUser);
            }

            return Tasks.forResult(null);
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