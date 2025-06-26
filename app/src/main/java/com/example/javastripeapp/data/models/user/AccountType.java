package com.example.javastripeapp.data.models.user;

import androidx.annotation.NonNull;

public enum AccountType {
    PROVIDER, CUSTOMER;

    @NonNull
    @Override
    public String toString() {
        return switch (this) {
            case CUSTOMER -> "Customer";
            case PROVIDER -> "Provider";
        };
    }

    public static AccountType fromString(String accountType) {
        return switch (accountType) {
            case "Customer" -> CUSTOMER;
            case "Provider" -> PROVIDER;
            default -> null;
        };
    }
}
