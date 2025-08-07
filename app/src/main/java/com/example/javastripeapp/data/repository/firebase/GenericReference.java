package com.example.javastripeapp.data.repository.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenericReference<T> {
    private final DatabaseReference dbRef;
    private final Class<T> tclass;
    private static final String TAG = "GenericReferenceRT";

    public GenericReference(String parentNode, Class<T> tclass) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        if (parentNode != null && !parentNode.isEmpty()) {
            rootRef = rootRef.child(parentNode);
        }
        this.dbRef = rootRef;
        this.tclass = tclass;
    }

    public GenericReference(String parentNode, String keyId, String childProperty, Class<T> tclass) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        if (parentNode != null && !parentNode.isEmpty()) {
            rootRef = rootRef.child(parentNode);
        }
        if (keyId != null && !keyId.isEmpty()) {
            rootRef = rootRef.child(keyId);
        }
        if (childProperty != null && !childProperty.isEmpty()) {
            rootRef = rootRef.child(childProperty);
        }
        this.dbRef = rootRef;
        this.tclass = tclass;
    }

    public Task<Void> createNewObject(T object) {
        if (object == null) {
            return Tasks.forException(new IllegalArgumentException("Passed in object is null"));
        }
        String objectId = dbRef.push().getKey();

        if (objectId == null) {
            return Tasks.forException(new IllegalStateException("Failed to generate new ID"));
        }
        try {
            setDynamicIdField(object, objectId);
        } catch (Exception e) {
            return Tasks.forException(e);
        }
        return dbRef.child(objectId).setValue(object);
    }

    public Task<String> createObject(T object) {
        if (object == null) {
            return Tasks.forException(new IllegalArgumentException("Passed in object is null"));
        }
        String objectId = dbRef.push().getKey();

        if (objectId == null) {
            return Tasks.forException(new IllegalStateException("Failed to generate new ID"));
        }
        try {
            setDynamicIdField(object, objectId);
        } catch (Exception e) {
            return Tasks.forException(e);
        }
        return dbRef.child(objectId).setValue(object).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return Tasks.forException(new Exception("Failed to create object"));
            }
            return Tasks.forResult(objectId);
        });
    }

    public Task<Void> createObjectWithId(T object, String objectId) {
        if (object == null) {
            return Tasks.forException(new IllegalArgumentException("Passed in object is null"));
        }
        if (objectId == null || objectId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Passed objectId is null or empty"));
        }
        return dbRef.child(objectId).setValue(object);
    }

    private void setDynamicIdField(T object, String objectId) {
        try {
            String idFieldName = getDynamicIdFieldName();
            Field idField = tclass.getDeclaredField(idFieldName);
            idField.setAccessible(true);
            idField.set(object, objectId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set dynamic ID field", e);
        }
    }

    private String getDynamicIdFieldName() {
        return switch (tclass.getSimpleName()) {
            case "WorkOrder" -> "workOrderId";
            case "User" -> "userId";
            case "Address" -> "addressId";
            default -> "fixDynamicId";
        };
    }

    public Task<T> getObject(String objectId) {
        if (objectId == null || objectId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Provided objectId is null or empty"));
        }
        return dbRef.child(objectId).get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return Tasks.forException(new Exception("Failed to retrieve object: " + objectId));
            }
            DataSnapshot snapshot = task.getResult();

            if (snapshot == null || !snapshot.exists()) {
                return Tasks.forException(new IllegalStateException("Snapshot does not exist for " + tclass.getSimpleName() + " with ID: " + objectId));
            }
            T objectInfo = snapshot.getValue(tclass);
            if (objectInfo == null) {
                return Tasks.forException(new IllegalStateException("Object is null for objectId: " + objectId + ". Possible deserialization issue."));
            }
            return Tasks.forResult(objectInfo);
        });
    }

    public Task<List<T>> getAllObjects() {
        dbRef.keepSynced(false);

        return dbRef.get().continueWithTask(task -> {
            List<T> resultList = new ArrayList<>();

            if (!task.isSuccessful()) {
                return Tasks.forException(new Exception("Failed to retrieve objects."));
            }
            DataSnapshot snapshot = task.getResult();

            if (snapshot == null || !snapshot.exists()) {
                Log.d(TAG, "No objects found in the database.");
                return Tasks.forResult(Collections.emptyList());
            }
            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                T object = childSnapshot.getValue(tclass);
                if (object != null) {
                    resultList.add(object);
                } else {
                    Log.e(TAG, "Failed to parse object from snapshot at key: " + childSnapshot.getKey());
                }
            }
            Log.d(TAG, "Successfully retrieved " + resultList.size() + " objects.");
            return Tasks.forResult(resultList);
        });
    }

    public Task<List<T>> findByField(String field, String value) {
        if (field == null || field.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Passed in field is null or empty"));
        }
        if (value == null || value.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Passed in value is null or empty"));
        }
        return dbRef.orderByChild(field).equalTo(value).get()
                .continueWithTask(this::processSnapshot);
    }

    private Task<List<T>> processSnapshot(Task<DataSnapshot> task) {
        if (!task.isSuccessful()) {
            Exception originalException = task.getException();
            Log.e(TAG, "Database query failed", originalException);
            return Tasks.forException(originalException != null ? originalException :
                    new Exception("Unknown database error"));
        }
        List<T> resultList = new ArrayList<>();
        DataSnapshot snapshot = task.getResult();

        if (snapshot != null && snapshot.exists()) {
            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                T object = childSnapshot.getValue(tclass);
                if (object != null) {
                    resultList.add(object);
                }
            }
        }
        return Tasks.forResult(resultList);
    }

    public Task<Void> updateObject(String objectId, Map<String, Object> updates) {
        if (objectId == null || objectId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Passed objectId is null or empty"));
        }
        if (updates == null || updates.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Updates map is null or empty"));
        }
        return dbRef.child(objectId).updateChildren(updates)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(new Exception("Failed to update object with ID: " + objectId + ". Updates: " + updates));
                    }
                    Log.d(TAG, "Successfully updated object with ID: " + objectId);
                    return Tasks.forResult(null);
                });
    }

    public Task<Void> deleteObject(String objectId) {
        if (objectId == null || objectId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Passed in object ID is null or empty"));
        }
        return dbRef.child(objectId).removeValue();
    }
}
