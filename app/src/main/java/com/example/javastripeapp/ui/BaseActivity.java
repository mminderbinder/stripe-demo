package com.example.javastripeapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.AccountType;
import com.example.javastripeapp.ui.dashboard.DashboardActivity;
import com.example.javastripeapp.ui.paymentmethods.PaymentMethodsActivity;
import com.example.javastripeapp.ui.payouts.PayoutsActivity;
import com.example.javastripeapp.ui.profile.CustomerProfileActivity;
import com.example.javastripeapp.ui.profile.ProviderProfileActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(getLayoutResourceId());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Child activities must provide their layout resource ID
     */
    protected abstract int getLayoutResourceId();

    /**
     * Child activities must provide their account type for menu
     */
    protected abstract AccountType getAccountType();

    /**
     * Set up toolbar - child activities must have R.id.toolbar in their layout
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AccountType accountType = getAccountType();
        if (accountType == null) {
            return super.onCreateOptionsMenu(menu);
        }

        // Inflate appropriate menu based on account type
        switch (accountType) {
            case CUSTOMER:
                getMenuInflater().inflate(R.menu.customer_nav_menu, menu);
                break;
            case PROVIDER:
                getMenuInflater().inflate(R.menu.provider_nav_menu, menu);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String currentClassName = this.getClass().getSimpleName();

        // Customer menu items
        if (itemId == R.id.menu_home_customer) {
            if (!"CustomerProfileActivity".equals(currentClassName)) {
                startActivity(new Intent(this, CustomerProfileActivity.class));
                return true;
            }
        } else if (itemId == R.id.menu_payment_methods) {
            if (!"PaymentMethodsActivity".equals(currentClassName)) {
                startActivity(new Intent(this, PaymentMethodsActivity.class));
                return true;
            }
        }
        // Provider menu items
        else if (itemId == R.id.menu_home_provider) {
            if (!"ProviderProfileActivity".equals(currentClassName)) {
                startActivity(new Intent(this, ProviderProfileActivity.class));
                return true;
            }
        } else if (itemId == R.id.menu_dashboard) {
            if (!"DashboardActivity".equals(currentClassName)) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            }
        } else if (itemId == R.id.menu_payouts) {
            if (!"PayoutsActivity".equals(currentClassName)) {
                startActivity(new Intent(this, PayoutsActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}