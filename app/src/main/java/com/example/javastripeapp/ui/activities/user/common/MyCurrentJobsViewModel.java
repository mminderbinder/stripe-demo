package com.example.javastripeapp.ui.activities.user.common;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.repos.UserRepo;
import com.example.javastripeapp.data.repos.WorkOrderRepo;
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
