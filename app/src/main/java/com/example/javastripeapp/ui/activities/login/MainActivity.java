package com.example.javastripeapp.ui.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.javastripeapp.R;
import com.example.javastripeapp.databinding.ActivityMainBinding;
import com.example.javastripeapp.ui.activities.profile.UserProfileActivity;
import com.example.javastripeapp.ui.activities.registration.RegistrationActivity;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        setUpClickListeners();
    }

    private void setUpClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            validateAndSignIn();
        });
        binding.btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void retrieveFirebaseUser() {
        viewModel.retrieveFirebaseUser().addOnSuccessListener(user -> {
            String userId = user.getUid();
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to retrieve Firebase user", e);
            showToast("An error occurred. Please try later or contact support");
        });
    }

    private void validateAndSignIn() {
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String password = String.valueOf(binding.etPassword.getText()).trim();

        if (email.isEmpty()) {
            showToast("Please enter your email");
            return;
        }
        if (password.isEmpty()) {
            showToast("Please enter your password");
            return;
        }
        signInUserWithEmailPassword(email, password);
    }

    private void signInUserWithEmailPassword(String email, String password) {
        viewModel.signInUser(email, password).addOnSuccessListener(authResult -> {
            FirebaseUser user = authResult.getUser();
            if (user != null) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("USER_ID", user.getUid());
                startActivity(intent);
            } else {
                showToast("No user signed in! Please try later or contact support");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to sign in user", e);
            showToast("Authentication Failed. Please try later or contact support");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveFirebaseUser();
    }
}