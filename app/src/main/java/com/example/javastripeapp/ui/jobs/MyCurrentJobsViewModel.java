package com.example.javastripeapp.ui.jobs;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.data.database.UserRepo;
import com.example.javastripeapp.data.database.WorkOrderRepo;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MyCurrentJobsViewModel extends ViewModel {
    private static final String TAG = "MyCurrentJobsViewModel";
    private final UserRepo userRepo = new UserRepo();
    private final WorkOrderRepo workOrderRepo = new WorkOrderRepo();

    public Task<User> retrieveCurrentUser() {
        return userRepo.fetchUserInDatabase();
    }

    public Task<List<WorkOrder>> retrieveWorkOrdersByUser(User user) {
        return workOrderRepo.fetchWorkOrdersByUser(user);
    }
}
