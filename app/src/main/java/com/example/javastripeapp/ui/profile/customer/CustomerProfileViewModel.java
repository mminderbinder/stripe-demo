package com.example.javastripeapp.ui.profile.customer;

import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.repository.UserRepo;
import com.example.javastripeapp.ui.profile.BaseProfileViewModel;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class CustomerProfileViewModel extends BaseProfileViewModel {

    private static final String TAG = "CustomerProfileViewModel";
    private final UserRepo userRepo = new UserRepo();
    private User currentUser;

    @Override
    public Task<User> retrieveUser() {
        return super.retrieveUser().continueWith(task -> {
            currentUser = TaskUtils.getTaskResultOrThrow(task, "Failed to retrieve current user");
            return currentUser;
        });
    }

    @Override
    public Task<List<Address>> fetchUserAddresses(String userId) {
        return super.fetchUserAddresses(userId);
    }

    @Override
    public void signOutUser() {
        super.signOutUser();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
