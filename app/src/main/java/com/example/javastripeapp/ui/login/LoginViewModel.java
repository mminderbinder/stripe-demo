package com.example.javastripeapp.ui.login;

import androidx.lifecycle.ViewModel;

import com.example.javastripeapp.data.User;
import com.example.javastripeapp.data.database.AuthRepo;
import com.example.javastripeapp.data.database.UserRepo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";
    private final AuthRepo authRepo = new AuthRepo();
    private final UserRepo userRepo = new UserRepo();

    public Task<AuthResult> signInUser(String email, String password) {
        return authRepo.signInUser(email, password);
    }

    public Task<FirebaseUser> retrieveFirebaseUser() {
        return authRepo.fetchCurrentFirebaseUser();
    }

    public Task<User> getUserFromDatabase() {
        return userRepo.fetchUserInDatabase();
    }
}
