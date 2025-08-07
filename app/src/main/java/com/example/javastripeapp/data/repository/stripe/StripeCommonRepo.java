package com.example.javastripeapp.data.repository.stripe;

import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class StripeCommonRepo {
    private static final String TAG = "StripeCommonRepo";
    private final FirebaseFunctions functions;

    public StripeCommonRepo() {
        this.functions = FirebaseFunctions.getInstance();
    }

    public Task<CancellationResult> cancelPaymentIntent(String workOrderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("workOrderId", workOrderId);

        return functions.getHttpsCallable("cancelPaymentIntent")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return TaskUtils.forTaskExceptionMessage(task, "Failed to cancel payment intent");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) task.getResult().getData();

                    if (response == null) {
                        return TaskUtils.forIllegalStateException("Response is null");
                    }
                    Boolean success = (Boolean) response.get("success");
                    Boolean isCanceled = (Boolean) response.get("canceled");
                    String message = (String) response.get("message");

                    if (success == null || !success) {
                        return TaskUtils.forIllegalStateException("Failed to cancel payment intent");
                    }
                    boolean wasCanceled = isCanceled != null && isCanceled;
                    String resultMessage = message != null ? message : "Cancellation successful";

                    return Tasks.forResult(new CancellationResult(wasCanceled, resultMessage));
                });
    }

    public static class CancellationResult {
        private final boolean wasCancelled;
        private final String message;

        public CancellationResult(boolean wasCancelled, String message) {
            this.wasCancelled = wasCancelled;
            this.message = message;
        }

        public boolean wasCancelled() {
            return wasCancelled;
        }

        public String getMessage() {
            return message;
        }
    }
}
