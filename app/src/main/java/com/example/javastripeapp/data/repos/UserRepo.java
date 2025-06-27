package com.example.javastripeapp.data.repos;

import com.example.javastripeapp.data.models.user.User;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class UserRepo {
    private final GenericReference<User> userRef;

    public UserRepo() {
        this.userRef = new GenericReference<>("users", User.class);
    }

    public Task<Void> createUserInDatabase(User newUser) {
        String userId = newUser.getUserId();
        return userRef.createObjectWithId(newUser, userId);
    }

    public Task<User> fetchUserById(String userId) {
        return userRef.getObject(userId);
    }

    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
        return userRef.updateObject(userId, updates);
    }
}
