package com.example.javastripeapp.ui.activities.user.customer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.databinding.ActivityOrderHistoryBinding;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;

public class OrderHistoryActivity extends BaseActivity {
    private ActivityOrderHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_order_history;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }
}