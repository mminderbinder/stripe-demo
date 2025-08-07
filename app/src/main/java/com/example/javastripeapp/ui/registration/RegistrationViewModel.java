package com.example.javastripeapp.ui.registration;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.database.AddressRepo;
import com.example.javastripeapp.data.database.AuthRepo;
import com.example.javastripeapp.data.database.UserRepo;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

public class RegistrationViewModel extends ViewModel {
    private static final String TAG = "RegistrationViewModel";
    private final AuthRepo authRepo = new AuthRepo();
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();

    public Task<String> createUserInAuth(String email, String password, String username) {
        return authRepo.createUserWithEmailPassword(email, password, username);
    }

    public Task<Void> createUserInDatabase(User user) {
        return userRepo.createUserInDatabase(user).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return TaskUtils.forTaskException(task, "Failed to create user in Database");
            }
            AccountType accountType = AccountType.fromString(user.getAccountType());

            if (accountType == null) {
                return TaskUtils.forIllegalStateException("Unrecognized account type");
            }
            if (accountType.equals(AccountType.CUSTOMER)) {
                return addressRepo.createCustomerAddresses(user.getUserId());
            }
            return addressRepo.createProviderAddresses(user.getUserId());
        });
    }
}
