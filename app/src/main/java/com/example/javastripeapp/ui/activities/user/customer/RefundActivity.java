package com.example.javastripeapp.ui.activities.user.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.line_item.LineItem;
import com.example.javastripeapp.databinding.ActivityRefundBinding;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;

import java.util.Map;

public class RefundActivity extends BaseActivity {
    private static final String TAG = "RefundActivity";
    private ActivityRefundBinding binding;
    private RefundViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRefundBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RefundViewModel.class);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("WO_ID")) {
            String workOrderId = intent.getStringExtra("WO_ID");
            if (workOrderId != null) {
                retrieveWorkOrder(workOrderId);
            }
        }
        setupToolbar();
    }

    private void retrieveWorkOrder(String workOrderId) {
        viewModel.fetchWorkOrder(workOrderId)
                .addOnSuccessListener(this::setUpRefundWorkOrderInfo)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve work order", e);
                    Toast.makeText(this, "Failed to retrieve work order", Toast.LENGTH_LONG).show();
                });
    }


    private void setUpRefundWorkOrderInfo(WorkOrder workOrder) {
        Map<String, LineItem> lineItemMap = workOrder.getLineItemMap();

        for (LineItem lineItem : lineItemMap.values()) {

            if (lineItem.getIntItemCode().equals(300)) {
                binding.tvDriveway.setText(lineItem.getDescription());
            }
            if (lineItem.getIntItemCode().equals(310)) {
                binding.tvWalkway.setText(lineItem.getDescription());
            }
            if (lineItem.getIntItemCode().equals(320)) {
                binding.tvSidewalk.setText(lineItem.getDescription());
            }
            binding.tvTotal.setText("$" + workOrder.getTotalAmount());
        }
    }

    private void requestRefund() {

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_refund;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }
}