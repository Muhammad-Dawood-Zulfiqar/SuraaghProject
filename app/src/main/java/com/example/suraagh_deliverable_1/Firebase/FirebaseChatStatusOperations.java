package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.suraagh_deliverable_1.Database.DatabaseChatStatusOperations;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FirebaseChatStatusOperations implements DatabaseChatStatusOperations {
    private static final String TAG = "FirebaseChatStatusOps";
    private final DatabaseReference chatsRef;
    
    public FirebaseChatStatusOperations() {
        Log.d(TAG, "Constructor: Initializing FirebaseChatStatusOperations");
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        chatsRef = firebaseManager.reference.child("chats");
    }

    @Override
    public void incrementUnreadCount(String chatId, String recipientId, boolean isOwner) {
        String unreadField = isOwner ? "unreadCountOwner" : "unreadCountFinder";
        Log.d(TAG, "incrementUnreadCount: Updating " + unreadField + " for chat=" + chatId);

        chatsRef.child(chatId).child(unreadField)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer currentCount = snapshot.getValue(Integer.class);
                        currentCount = (currentCount != null) ? currentCount : 0;
                        chatsRef.child(chatId).child(unreadField).setValue(currentCount + 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "incrementUnreadCount: Failed to read count: " + error.getMessage());
                    }
                });
    }

    @Override
    public void resetUnreadCount(String chatId, String userId, boolean isOwner) {
        String unreadField = isOwner ? "unreadCountOwner" : "unreadCountFinder";
        Log.d(TAG, "resetUnreadCount: Resetting " + unreadField + " for chat=" + chatId);
        chatsRef.child(chatId).child(unreadField).setValue(0);
    }
}