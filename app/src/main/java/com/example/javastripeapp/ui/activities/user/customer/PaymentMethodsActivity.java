package com.example.javastripeapp.ui.activities.user.customer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.databinding.ActivityPaymentMethodsBinding;
import com.example.javastripeapp.ui.activities.user.common.BaseActivity;

public class PaymentMethodsActivity extends BaseActivity {
    private ActivityPaymentMethodsBinding binding;
    private PaymentMethodsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentMethodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();
        viewModel = new ViewModelProvider(this).get(PaymentMethodsViewModel.class);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_payment_methods;
    }

    @Override
    protected AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }
}