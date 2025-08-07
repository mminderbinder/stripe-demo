package com.example.javastripeapp.ui.profile;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.repository.firebase.AddressRepo;
import com.example.javastripeapp.data.repository.firebase.AuthRepo;
import com.example.javastripeapp.data.repository.firebase.UserRepo;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class BaseProfileViewModel extends ViewModel {
    private static final String TAG = "BaseProfileViewModel";
    private final AuthRepo authRepo = new AuthRepo();
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();

    public Task<User> retrieveUser() {
        return userRepo.fetchUserInDatabase();
    }

    public Task<List<Address>> fetchUserAddresses(String userId) {
        return addressRepo.fetchUserAddresses(userId);
    }

    public void signOutUser() {
        authRepo.signOutUser();
    }
}
