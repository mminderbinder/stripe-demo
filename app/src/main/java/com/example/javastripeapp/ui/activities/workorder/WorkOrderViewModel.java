package com.example.javastripeapp.ui.activities.workorder;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.repos.AddressRepo;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.data.repos.WorkOrderRepo;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class WorkOrderViewModel extends ViewModel {
    private static final String TAG = "WorkOrderViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final AddressRepo addressRepo = new AddressRepo();
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();

    public Task<User> retrieveCurrentUser() {
        return userRepo.fetchUserInDatabase();
    }

    public Task<List<Address>> retrieveCustomerAddresses(String userId) {
        return addressRepo.fetchUserAddresses(userId);
    }
}
