package com.example.javastripeapp.utils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class TaskUtils {
    public static <T> Task<T> forIllegalArgumentException(String message) {
        return Tasks.forException(new IllegalArgumentException(message));
    }

    public static <T> Task<T> forIllegalStateException(String message) {
        return Tasks.forException(new IllegalStateException(message));
    }

    public static <T> Task<T> forTaskException(Task<?> failedTask, String defaultMessage) {
        return Tasks.forException(getTaskException(failedTask, defaultMessage));
    }

    private static Exception getTaskException(Task<?> task, String defaultMessage) {
        return task.getException() != null
                ? task.getException()
                : new Exception(defaultMessage);
    }

    public static <T> T getTaskResultOrThrow(Task<T> task, String errorMessage) {
        if (!task.isSuccessful()) {
            throw new RuntimeException(getTaskException(task, errorMessage));
        }
        return task.getResult();
    }
}
