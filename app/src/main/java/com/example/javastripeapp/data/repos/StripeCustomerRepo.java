package com.example.javastripeapp.data.repos;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.models.workorder.WorkOrder;
import com.example.javastripeapp.data.models.workorder.line_item.LineItem;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public Task<PaymentIntentResult> createPaymentIntent(WorkOrder workOrder, String stripeCustomerId) {
        String idempotencyKey = UUID.randomUUID().toString();

        Map<String, Object> data = getStringObjectMap(workOrder, stripeCustomerId, idempotencyKey);

        return functions.getHttpsCallable("createPaymentIntent")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to create payment intent");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return TaskUtils.forIllegalStateException("Payment intent response is null");
                    }
                    String paymentIntentId = (String) response.get("paymentIntentId");
                    String clientSecret = (String) response.get("clientSecret");
                    String ephemeralKey = (String) response.get("ephemeralKey");

                    if (paymentIntentId == null || clientSecret == null || ephemeralKey == null) {
                        return TaskUtils.forIllegalStateException("Missing payment intent data");
                    }
                    Log.d(TAG, "Payment intent created successfully");
                    return Tasks.forResult(new PaymentIntentResult(paymentIntentId, clientSecret, ephemeralKey));
                });
    }

    @NonNull
    private static Map<String, Object> getStringObjectMap(WorkOrder workOrder, String stripeCustomerId, String idempotencyKey) {
        Map<String, Object> data = new HashMap<>();
        data.put("workOrderId", workOrder.getWorkOrderId());
        data.put("stripeCustomerId", stripeCustomerId);
        data.put("totalAmount", workOrder.getTotalAmount());
        data.put("idempotencyKey", idempotencyKey);

        Map<String, LineItem> lineItemMap = workOrder.getLineItemMap();
        List<Map<String, Object>> lineItemData = new ArrayList<>();

        for (LineItem item : lineItemMap.values()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("intItemCode", item.getIntItemCode());
            itemData.put("description", item.getDescription());
            itemData.put("amount", item.getAmount());
            itemData.put("taxCode", item.getTaxCode());
            lineItemData.add(itemData);
        }
        data.put("lineItems", lineItemData);
        return data;
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

    public static class PaymentIntentResult {
        private final String paymentIntentId;
        private final String clientSecret;
        private final String ephemeralKeySecret;

        public PaymentIntentResult(String paymentIntentId, String clientSecret, String ephemeralKeySecret) {
            this.paymentIntentId = paymentIntentId;
            this.clientSecret = clientSecret;
            this.ephemeralKeySecret = ephemeralKeySecret;
        }

        public String getPaymentIntentId() {
            return paymentIntentId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getEphemeralKeySecret() {
            return ephemeralKeySecret;
        }
    }
}
