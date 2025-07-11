package com.example.javastripeapp.data.repos;

import android.util.Log;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StripeProviderRepo {

    private static final String TAG = "StripeProviderRepo";
    private final FirebaseFunctions functions;

    public StripeProviderRepo() {
        this.functions = FirebaseFunctions.getInstance();
    }

    public Task<Void> createStripeConnectAccount(User user, Address address) {
        Map<String, Object> data = new HashMap<>();
        data.put("accountType", user.getAccountType());
        data.put("displayName", user.getUsername());
        data.put("email", user.getEmail());
        data.put("line1", address.getLine1());
        data.put("city", address.getCity());
        data.put("state", address.getProvince());
        data.put("postalCode", address.getPostalCode());
        data.put("country", "CA");

        return functions.getHttpsCallable("createConnectAccount")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to create Stripe connect account");
                    }
                    Log.d(TAG, "Stripe account created successfully");
                    return Tasks.forResult(null);
                });
    }

    public Task<String> createAccountLink(String stripeAccountId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stripeAccountId", stripeAccountId);

        return functions.getHttpsCallable("createConnectAccountLink")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to create onboarding link");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return Tasks.forException(new IllegalStateException("Response is null"));
                    }
                    String accountLinkUrl = (String) response.get("url");
                    if (accountLinkUrl == null) {
                        return Tasks.forException(new IllegalStateException("Account link Url is null"));
                    }
                    Log.d(TAG, "Connect account link created successfully");
                    return Tasks.forResult(accountLinkUrl);
                });
    }

    public Task<Boolean> isAccountFullyOnboarded(String stripeAccountId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stripeAccountId", stripeAccountId);

        return functions.getHttpsCallable("getConnectAccountStatus")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to get account status");
                    }
                    HttpsCallableResult result = task.getResult();
                    if (result == null || result.getData() == null) {
                        return TaskUtils.forIllegalStateException("No account status data received");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) result.getData();

                    Boolean chargesEnabled = (Boolean) response.get("charges_enabled");
                    Boolean payoutsEnabled = (Boolean) response.get("payouts_enabled");
                    Boolean detailsSubmitted = (Boolean) response.get("details_submitted");

                    boolean isFullyOnboarded = Boolean.TRUE.equals(chargesEnabled) &&
                            Boolean.TRUE.equals(payoutsEnabled) && Boolean.TRUE.equals(detailsSubmitted);

                    return Tasks.forResult(isFullyOnboarded);
                });
    }

    public Task<Void> capturePaymentForCompletedService(String workOrderId, String paymentIntentId) {
        String idempotencyKey = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("workOrderId", workOrderId);
        data.put("paymentIntentId", paymentIntentId);
        data.put("idempotencyKey", idempotencyKey);

        return functions.getHttpsCallable("capturePaymentForCompletedService")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to capture payment for completed service");
                    }
                    Log.d(TAG, "Payment intent captured successfully");
                    return Tasks.forResult(null);
                });
    }
}
