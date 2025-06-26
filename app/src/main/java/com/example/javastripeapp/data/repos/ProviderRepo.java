package com.example.javastripeapp.data.repos;

public class ProviderRepo {
    private final UserRepo userRepo;

    public ProviderRepo() {
        this.userRepo = new UserRepo();
    }

}
