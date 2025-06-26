package com.example.javastripeapp.data.repos;

public class CustomerRepo {
    private final UserRepo userRepo;

    public CustomerRepo() {
        this.userRepo = new UserRepo();
    }

}
