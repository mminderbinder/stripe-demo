package com.example.javastripeapp.ui.activities.user.customer;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.repos.AddressRepo;
import com.example.javastripeapp.data.repos.UserRepo;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class CustomerProfileViewModel extends ViewModel {
    private static final String TAG = "CustomerProfileViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();

    public Task<User> fetchUserById(String userId) {
        return userRepo.fetchUserById(userId);
    }

    public Task<List<Address>> fetchUserAddresses(String userId) {
        return addressRepo.fetchUserAddresses(userId);
    }
}
