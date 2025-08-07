package com.example.javastripeapp.ui.refunds;

import android.annotation.SuppressLint;
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
import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.data.LineItem;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.databinding.ActivityRefundBinding;
import com.example.javastripeapp.ui.BaseActivity;
import com.example.javastripeapp.ui.profile.CustomerProfileActivity;

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

            requestRefund();
        }
    }

    @SuppressLint("DefaultLocale")
    private void requestRefund() {
        binding.btnRequest.setOnClickListener(v -> {
            binding.btnRequest.setEnabled(false);
            String workOrderId = viewModel.getCurrentOrder().getWorkOrderId();

            viewModel.requestRefundFromPlatform(workOrderId).addOnSuccessListener(refundResult -> {
                if (refundResult.isProcessedImmediately()) {
                    Toast.makeText(this, "Refund processed immediately", Toast.LENGTH_LONG).show();
                } else if (refundResult.isQueued()) {
                    Toast.makeText(this, "Refund request has been submitted", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(this, CustomerProfileActivity.class);
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> {
                binding.btnRequest.setEnabled(true);
                Log.e(TAG, "Refund request failed", e);
                Toast.makeText(this, "Refund request failed. Please try again later", Toast.LENGTH_LONG).show();
            });
        });
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