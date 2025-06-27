package com.example.javastripeapp.ui.activities.login;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.repos.AuthRepo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";
    private final AuthRepo authRepo = new AuthRepo();

    public Task<AuthResult> signInUser(String email, String password) {
        return authRepo.signInUser(email, password);
    }

    public Task<FirebaseUser> retrieveFirebaseUser() {
        return authRepo.fetchCurrentFirebaseUser();
    }
}
