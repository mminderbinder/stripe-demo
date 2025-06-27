package com.example.javastripeapp.data.models.user;

import androidx.annotation.NonNull;

public enum AccountType {
    PROVIDER, CUSTOMER;

    @NonNull
    @Override
    public String toString() {
        return switch (this) {
            case CUSTOMER -> "CUSTOMER";
            case PROVIDER -> "PROVIDER";
        };
    }

    public static AccountType fromString(String accountType) {
        return switch (accountType) {
            case "CUSTOMER" -> CUSTOMER;
            case "PROVIDER" -> PROVIDER;
            default -> null;
        };
    }
}
