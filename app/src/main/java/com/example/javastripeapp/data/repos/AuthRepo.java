package com.example.javastripeapp.data.repos;

import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepo {
    private final FirebaseAuth mAuth;

    public AuthRepo() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public Task<String> createUserWithEmailPassword(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .continueWith(authTask -> {
                    AuthResult result = TaskUtils.getTaskResultOrThrow(authTask, "Failed to retrieve auth result");
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        throw new IllegalStateException("Current user is null");
                    }

                    return user.getUid();
                });
    }
}
