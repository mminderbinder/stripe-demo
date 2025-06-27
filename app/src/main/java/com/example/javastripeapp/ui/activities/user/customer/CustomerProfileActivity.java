package com.example.javastripeapp.ui.activities.user.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.databinding.ActivityUserProfileBinding;

public class CustomerProfileActivity extends AppCompatActivity {
    private static final String TAG = "CustomerProfileActivity";
    private ActivityUserProfileBinding binding;
    private CustomerProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(CustomerProfileViewModel.class);

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra("USER_ID")) {
                String userId = intent.getStringExtra("USER_ID");
                retrieveUser(userId);
            }
        }
    }

    private void retrieveUser(String userId) {
        viewModel.fetchUserById(userId)
                .addOnSuccessListener(this::setUpProfileUI)
                .addOnFailureListener(e -> {

                });
    }

    private void setUpProfileUI(User currentUser) {
        String username = currentUser.getUsername();
        binding.tvUsername.setText(username);
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}