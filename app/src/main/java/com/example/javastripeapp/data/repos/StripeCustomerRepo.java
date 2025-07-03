package com.example.javastripeapp.data.repos;

import android.util.Log;

import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

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
                        return TaskUtils.forTaskException(task, "Unable to setup payment methods");
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
                        return TaskUtils.forTaskException(task, "Failed to check payment methods");
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
}
