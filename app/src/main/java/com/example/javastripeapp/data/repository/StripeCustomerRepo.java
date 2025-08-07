package com.example.javastripeapp.data.repository;

import android.util.Log;

import com.example.javastripeapp.data.RefundStatus;
import com.example.javastripeapp.data.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StripeCustomerRepo {
    private static final String TAG = "StripeCustomerRepo";
    private final FirebaseFunctions functions;

    public StripeCustomerRepo() {
        this.functions = FirebaseFunctions.getInstance();
    }

    public Task<Void> createStripeCustomer(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("accountType", user.getAccountType());
        data.put("displayName", user.getUsername());

        return functions.getHttpsCallable("createCustomer")
                .call(data)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Stripe customer creation failed", task.getException());
                        throw new RuntimeException("Failed to create Stripe customer");
                    }
                    Log.d(TAG, "Stripe customer created successfully");
                    return null;
                });
    }

    public Task<CustomerSheetSetupResult> createCustomerSheetSetupIntent(String customerId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stripeCustomerId", customerId);

        return functions.getHttpsCallable("createPaymentSheetSetupIntent")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Unable to setup payment methods");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return TaskUtils.forIllegalStateException("Response is null");
                    }
                    String setupIntentClientSecret = (String) response.get("setupIntentClientSecret");
                    String ephemeralKeySecret = (String) response.get("ephemeralKeySecret");

                    Log.d(TAG, "CustomerSheet setup intent created successfully");
                    return Tasks.forResult(new CustomerSheetSetupResult(setupIntentClientSecret, ephemeralKeySecret, customerId));
                });
    }

    public Task<Boolean> hasPaymentMethods(String stripeCustomerId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stripeCustomerId", stripeCustomerId);

        return functions.getHttpsCallable("checkCustomerHasPaymentMethods")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to check payment methods");
                    }
                    HttpsCallableResult result = task.getResult();
                    if (result == null || result.getData() == null) {
                        return TaskUtils.forIllegalStateException("No payment method data returned");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) result.getData();

                    Boolean hasPaymentMethods = (Boolean) response.get("hasPaymentMethods");
                    return Tasks.forResult(hasPaymentMethods != null && hasPaymentMethods);
                });
    }

    public Task<PaymentIntentForCheckoutResult> createPaymentIntentForCheckout(Map<String, Object> workOrderData, String stripeCustomerId) {
        String idempotencyKey = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("workOrderData", workOrderData);
        data.put("stripeCustomerId", stripeCustomerId);
        data.put("idempotencyKey", idempotencyKey);

        return functions.getHttpsCallable("createPaymentIntentForCheckout")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to create payment intent for checkout");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return TaskUtils.forIllegalStateException("Payment intent response is null");
                    }

                    String paymentIntentId = (String) response.get("paymentIntentId");
                    String clientSecret = (String) response.get("clientSecret");
                    String ephemeralKeySecret = (String) response.get("ephemeralKeySecret");

                    if (paymentIntentId == null || clientSecret == null || ephemeralKeySecret == null) {
                        return TaskUtils.forIllegalStateException("Missing payment intent data");
                    }

                    Log.d(TAG, "Payment intent created for checkout");
                    return Tasks.forResult(new PaymentIntentForCheckoutResult(paymentIntentId, clientSecret, ephemeralKeySecret));
                });
    }

    public Task<RefundResult> requestRefundFromPlatform(String workOrderId) {
        String idempotencyKey = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("workOrderId", workOrderId);
        data.put("idempotencyKey", idempotencyKey);

        return functions.getHttpsCallable("queueRefundFromPlatform")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to process refund request");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return TaskUtils.forIllegalStateException("Refund response is null");
                    }
                    Boolean success = (Boolean) response.get("success");
                    if (!Boolean.TRUE.equals(success)) {
                        return TaskUtils.forIllegalStateException("Refund request failed");
                    }
                    String status = (String) response.get("status");
                    String message = (String) response.get("message");

                    if ("processed".equals(status)) {
                        String refundId = (String) response.get("refundId");
                        Number refundAmountNumber = (Number) response.get("refundAmount");
                        int refundAmount = refundAmountNumber != null ? refundAmountNumber.intValue() : 0;

                        Log.d(TAG, "Refund processed immediately");
                        return Tasks.forResult(new RefundResult(
                                refundId,
                                refundAmount,
                                RefundStatus.PROCESSED,
                                message,
                                null
                        ));
                    } else if ("queued".equals(status)) {
                        String refundQueueId = (String) response.get("refundQueueId");

                        Log.d(TAG, "Refund queued for later processing");
                        return Tasks.forResult(new RefundResult(
                                null,
                                0,
                                RefundStatus.QUEUED,
                                message,
                                refundQueueId
                        ));
                    } else {
                        return TaskUtils.forIllegalStateException("Unknown refund status: " + status);
                    }
                });
    }

    public static class CustomerSheetSetupResult {
        private final String setUpIntentClientSecret;
        private final String ephemeralKeySecret;
        private final String customerId;

        public CustomerSheetSetupResult(String setUpIntentClientSecret, String ephemeralKeySecret, String customerId) {
            this.setUpIntentClientSecret = setUpIntentClientSecret;
            this.ephemeralKeySecret = ephemeralKeySecret;
            this.customerId = customerId;
        }

        public String getSetupIntentClientSecret() {
            return setUpIntentClientSecret;
        }

        public String getEphemeralKeySecret() {
            return ephemeralKeySecret;
        }

        public String getCustomerId() {
            return customerId;
        }
    }

    public record PaymentIntentForCheckoutResult(String paymentIntentId, String clientSecret,
                                                 String ephemeralKeySecret) {
    }

    public static class RefundResult {
        private final String refundId;
        private final int refundAmount;
        private final RefundStatus status;
        private final String message;
        private final String refundQueueId;

        public RefundResult(String refundId, int refundAmount, RefundStatus status, String message, String refundQueueId) {
            this.refundId = refundId;
            this.refundAmount = refundAmount;
            this.status = status;
            this.message = message;
            this.refundQueueId = refundQueueId;
        }

        public String getRefundId() {
            return refundId;
        }

        public int getRefundAmount() {
            return refundAmount;
        }

        public RefundStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getRefundQueueId() {
            return refundQueueId;
        }

        public boolean isProcessedImmediately() {
            return status == RefundStatus.PROCESSED && refundId != null;
        }

        public boolean isQueued() {
            return status == RefundStatus.QUEUED && refundQueueId != null;
        }
    }
}