package com.example.javastripeapp.data.models.user;

import com.google.firebase.database.Exclude;

public class User {
    private String userId;
    private String username;
    private String email;
    private String accountType;
    private String stripeCustomerId;
    private Boolean hasPaymentMethod;
    private String stripeAccountId;
    private Boolean payoutEnabled;

    public User() {
        this.hasPaymentMethod = false;
        this.payoutEnabled = false;
    }

    public User(String username, String email, AccountType accountType) {
        this.username = username;
        this.email = email;
        this.accountType = accountType.toString();
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

    // STRIPE
    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public Boolean getHasPaymentMethod() {
        return hasPaymentMethod;
    }

    public void setHasPaymentMethod(Boolean hasPaymentMethod) {
        this.hasPaymentMethod = hasPaymentMethod;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public Boolean getPayoutEnabled() {
        return payoutEnabled;
    }

    public void setPayoutEnabled(Boolean payoutEnabled) {
        this.payoutEnabled = payoutEnabled;
    }

    // HELPER METHODS

    @Exclude
    public boolean isCustomer() {
        return "CUSTOMER".equals(accountType);
    }

    @Exclude
    public boolean isProvider() {
        return "PROVIDER".equals(accountType);
    }

    @Exclude
    public boolean canMakePayments() {
        return isCustomer() && stripeCustomerId != null && hasPaymentMethod != null && hasPaymentMethod;
    }

    @Exclude
    public boolean canReceivePayments() {
        return isProvider() && payoutEnabled != null && payoutEnabled;
    }
}
