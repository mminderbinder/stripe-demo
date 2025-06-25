package com.example.javastripeapp.data.models.user;

public class User {
    private String userId;
    private String username;
    private String email;
    private String accountType;

    public User() {

    }

    public User(String userId, String username, String email, AccountType accountType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.accountType = accountType.name();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
