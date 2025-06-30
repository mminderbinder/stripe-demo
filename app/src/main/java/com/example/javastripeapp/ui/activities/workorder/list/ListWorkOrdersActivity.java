package com.example.javastripeapp.ui.activities.workorder.list;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.databinding.ActivityListWorkOrdersBinding;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;
import com.example.javastripeapp.ui.activities.workorder.WorkOrderAdapter;

import java.util.List;

public class ListWorkOrdersActivity extends BaseActivity {
    private static final String TAG = "ListWorkOrdersActivity";
    private ActivityListWorkOrdersBinding binding;
    private ListWorkOrdersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListWorkOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(ListWorkOrdersViewModel.class);
        setupToolbar();
        retrieveRequestedOrders();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_list_work_orders;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.PROVIDER;
    }


    private void retrieveRequestedOrders() {
        viewModel.retrieveRequestedWorkOrders()
                .addOnSuccessListener(this::setUpRecycler)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve work orders", e);
                    showToast("Failed to retrieve work orders");
                });
    }

    private void setUpRecycler(List<WorkOrder> workOrders) {
        WorkOrderAdapter adapter = new WorkOrderAdapter(workOrders);
        RecyclerView recyclerView = binding.rvOrders;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}