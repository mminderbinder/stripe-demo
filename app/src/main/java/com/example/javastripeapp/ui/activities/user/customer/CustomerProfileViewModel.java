package com.example.javastripeapp.ui.activities.user.customer;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.ui.activities.user.BaseProfileViewModel;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class CustomerProfileViewModel extends BaseProfileViewModel {

    private static final String TAG = "CustomerProfileViewModel";
    private final UserRepo userRepo = new UserRepo();
    private User currentUser;

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
