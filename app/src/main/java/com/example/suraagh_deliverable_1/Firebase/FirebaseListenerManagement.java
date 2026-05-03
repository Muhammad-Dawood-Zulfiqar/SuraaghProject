package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.DatabaseListenerManagement;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseListenerManagement implements DatabaseListenerManagement {
    private static final String TAG = "FirebaseListenerManagement";
    private final DatabaseReference chatsRef;
    private final DatabaseReference messagesRef;
    
    public FirebaseListenerManagement() {
        Log.d(TAG, "Constructor: Initializing FirebaseListenerManagement");
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        chatsRef = firebaseManager.reference.child("chats");
        messagesRef = firebaseManager.reference.child("messages");
    }

    @Override
    public void removeChatsListener(String userId, String field, ValueEventListener listener) {
        Log.d(TAG, "removeChatsListener: Removing listener for user=" + userId);
        chatsRef.orderByChild(field).equalTo(userId).removeEventListener(listener);
    }

    @Override
    public void removeMessagesListener(String chatId, ValueEventListener listener) {
        Log.d(TAG, "removeMessagesListener: Removing listener for chat=" + chatId);
        messagesRef.child(chatId).removeEventListener(listener);
    }

    @Override
    public Query getChatsQuery(String userId, String field) {
        return chatsRef.orderByChild(field).equalTo(userId);
    }
}