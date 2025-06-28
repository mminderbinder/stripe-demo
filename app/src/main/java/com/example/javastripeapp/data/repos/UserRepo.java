package com.example.javastripeapp.data.repos;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class UserRepo {
    private final GenericReference<User> userRef;
    private final AuthRepo authRepo = new AuthRepo();

    public UserRepo() {
        this.userRef = new GenericReference<>("users", User.class);
    }

    public Task<Void> createUserInDatabase(User newUser) {
        String userId = newUser.getUserId();
        return userRef.createObjectWithId(newUser, userId);
    }

    public Task<User> fetchUserInDatabase() {
        return authRepo.fetchCurrentUserUid().continueWithTask(authTask -> {
            if (!authTask.isSuccessful()) {
                return TaskUtils.forTaskException(authTask, "Failed to retrieve user Uid");
            }
            String userId = authTask.getResult();
            return userRef.getObject(userId);
        });
    }

    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
        return userRef.updateObject(userId, updates);
    }
}
