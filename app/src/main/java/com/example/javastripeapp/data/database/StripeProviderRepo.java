package com.example.javastripeapp.data.database;

import android.util.Log;

import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository class for managing Stripe Connect accounts for service providers.
 * This class handles operations related to creating and managing Stripe Connect accounts,
 * generating onboarding links, checking account status, and processing payments for completed services.
 * It communicates with Firebase Cloud Functions to interact with the Stripe API.
 */
public class StripeProviderRepo {

    private static final String TAG = "StripeProviderRepo";
    private final FirebaseFunctions functions;

    /**
     * Constructs a new StripeProviderRepo instance.
     * Initializes the Firebase Functions instance for making calls to Cloud Functions.
     */
    public StripeProviderRepo() {
        this.functions = FirebaseFunctions.getInstance();
    }

    /**
     * Creates a Stripe Connect account for a service provider.
     *
     * @param user    The user object containing account information (username, email, account type)
     * @param address The address object containing the provider's address details
     * @return A Task that resolves to Void when the account is created successfully
     * or fails with an exception if the creation fails
     */
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

    /**
     * Creates an account link URL for onboarding a Stripe Connect account.
     * This URL can be used to redirect the user to Stripe's onboarding flow.
     *
     * @param stripeAccountId The ID of the Stripe Connect account to create a link for
     * @return A Task that resolves to a String containing the account link URL
     * or fails with an exception if the link creation fails
     */
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

    /**
     * Checks if a Stripe Connect account has completed the onboarding process.
     * An account is considered fully onboarded when charges are enabled, payouts are enabled,
     * and all required details have been submitted.
     *
     * @param stripeAccountId The ID of the Stripe Connect account to check
     * @return A Task that resolves to a Boolean indicating whether the account is fully onboarded
     * or fails with an exception if the status check fails
     */
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

    /**
     * Captures a payment for a completed service.
     * This method should be called when a service has been completed and the payment should be processed.
     * It uses an idempotency key to ensure that the payment is only captured once.
     *
     * @param workOrderId     The ID of the work order for which the payment is being captured
     * @param paymentIntentId The ID of the Stripe payment intent to capture
     * @return A Task that resolves to Void when the payment is captured successfully
     * or fails with an exception if the payment capture fails
     */
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
