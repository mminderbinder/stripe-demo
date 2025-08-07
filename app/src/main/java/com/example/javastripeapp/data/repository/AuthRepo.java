package com.example.javastripeapp.data.repository;

import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthRepo {
    private final FirebaseAuth mAuth;

    public AuthRepo() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public Task<String> createUserWithEmailPassword(String email, String password, String username) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(authTask -> {
                    if (!authTask.isSuccessful()) {
                        return TaskUtils.forTaskException(authTask, "Failed to create user in Firebase Auth");
                    }
                    FirebaseUser user = authTask.getResult().getUser();
                    if (user == null) {
                        return TaskUtils.forIllegalStateException("Current user is null");
                    }
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    return user.updateProfile(profileUpdates).continueWith(usernameTask -> {
                        TaskUtils.getTaskResultOrThrow(usernameTask, "Failed to update user's display name");
                        return user.getUid();
                    });
                });
    }

    public Task<AuthResult> signInUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<FirebaseUser> fetchCurrentFirebaseUser() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                return TaskUtils.forIllegalStateException("No user signed in. User is null");
            }
            return Tasks.forResult(currentUser);
        } catch (Exception e) {
            return Tasks.forException(e);
        }
    }

    public Task<String> fetchCurrentUserUid() {
        return fetchCurrentFirebaseUser().continueWith(userTask -> {
            FirebaseUser currentUser = TaskUtils.getTaskResultOrThrow(userTask, "Failed to get current user");
            return currentUser.getUid();
        });
    }

    public void signOutUser() {
        FirebaseAuth.getInstance().signOut();
    }
}
