package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.Database.DatabaseUserOperations;
import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUserOperations implements DatabaseUserOperations {
    private static final String TAG = "FirebaseUserOperations";
    private final FirebaseAuth auth;
    private final CollectionReference usersCollection;
    
    public FirebaseUserOperations() {
        Log.d(TAG, "Constructor: Initializing FirebaseUserOperations");
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        auth = firebaseManager.auth;
        usersCollection = firebaseManager.db.collection("users");
    }

    @Override
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        Log.w(TAG, "getCurrentUserId: No user logged in");
        return null;
    }

    @Override
    public void getUserInfo(String userId, ChatCallbacks.ChatCallback<Map<String, Object>> callback) {
        Log.d(TAG, "getUserInfo: Fetching Firestore data for user=" + userId);
        usersCollection.document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "getUserInfo: Document found");
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userName", documentSnapshot.getString("userName"));
                        userData.put("email", documentSnapshot.getString("email"));
                        userData.put("trustPoints", documentSnapshot.getLong("trustPoints"));
                        callback.onSuccess(userData);
                    } else {
                        Log.w(TAG, "getUserInfo: Document does not exist");
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getUserInfo: Fetch failed: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getCurrentUserName(ChatCallbacks.ChatCallback<String> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        getUserInfo(userId, new ChatCallbacks.ChatCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                String userName = (String) result.get("userName");
                Log.d(TAG, "getCurrentUserName: Found name=" + userName);
                callback.onSuccess(userName != null ? userName : "User");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "getCurrentUserName: Error=" + error);
                callback.onError(error);
            }
        });
    }

    @Override
    public String getPostIdForUser(Chat chat, String userId) {
        if (chat == null || userId == null) return "";
        if (chat.isUserOwner(userId)) {
            return chat.getFoundPostId();
        } else if (chat.isUserFinder(userId)) {
            return chat.getLostPostId();
        }
        return "";
    }

    @Override
    public String getOtherUserId(Chat chat, String currentUserId) {
        if (chat == null || currentUserId == null) return "";
        return chat.getOtherUserId(currentUserId);
    }

    @Override
    public String getOtherUserName(Chat chat, String currentUserId) {
        if (chat == null || currentUserId == null) return "";
        return chat.getOtherUserName(currentUserId);
    }
}