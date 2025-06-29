package com.example.javastripeapp.ui.activities.user.common;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.databinding.ActivityMyCurrentJobsBinding;
import com.example.javastripeapp.ui.activities.workorder.WorkOrderAdapter;

import java.util.List;

public class MyCurrentJobsActivity extends AppCompatActivity {
    private static final String TAG = "MyCurrentJobsActivity";
    private ActivityMyCurrentJobsBinding binding;
    private MyCurrentJobsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMyCurrentJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(MyCurrentJobsViewModel.class);
        retrieveCurrentUser();
    }

    private void retrieveCurrentUser() {
        viewModel.retrieveCurrentUser()
                .addOnSuccessListener(this::retrieveUserWorkOrders)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve current user", e);
                    showToast("Failed to load user. Please try later");
                });
    }

    private void retrieveUserWorkOrders(User user) {
        viewModel.retrieveWorkOrdersByUser(user)
                .addOnSuccessListener(workOrderList -> {
                    if (workOrderList.isEmpty()) {
                        binding.tvGetStarted.setVisibility(RecyclerView.VISIBLE);
                    } else {
                        setUpRecyclerView(workOrderList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user's work orders", e);
                    showToast("Failed to retrieve work orders. Please try later");
                });
    }

    private void setUpRecyclerView(List<WorkOrder> workOrderList) {
        WorkOrderAdapter adapter = new WorkOrderAdapter(workOrderList);
        RecyclerView recyclerView = binding.rvOrders;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}