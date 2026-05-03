package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.Database.DatabaseMessageOperations;
import com.example.suraagh_deliverable_1.ModelClasses.Message;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseMessageOperations implements DatabaseMessageOperations {
    private static final String TAG = "FirebaseMessageOperations";
    private final DatabaseReference messagesRef;
    private final DatabaseReference chatsRef;
    
    public FirebaseMessageOperations() {
        Log.d(TAG, "Constructor: Initializing FirebaseMessageOperations");
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        messagesRef = firebaseManager.reference.child("messages");
        chatsRef = firebaseManager.reference.child("chats");
    }

    @Override
    public void sendMessage(Message message, ChatCallbacks.ChatCallback<Message> callback) {
        Log.d(TAG, "sendMessage: Sending message to chatId=" + message.getChatId());
        String messageId = messagesRef.child(message.getChatId()).push().getKey();
        if (messageId == null) {
            Log.e(TAG, "sendMessage: Failed to generate message ID");
            callback.onError("Failed to generate message ID");
            return;
        }

        message.setMessageId(messageId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/messages/" + message.getChatId() + "/" + messageId, message.toMap());
        updates.put("/chats/" + message.getChatId() + "/lastMessage", message.getContent());
        updates.put("/chats/" + message.getChatId() + "/lastMessageTimestamp", message.getTimestamp());
        updates.put("/chats/" + message.getChatId() + "/lastMessageSenderId", message.getSenderId());

        FirebaseManager.getInstance().reference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendMessage: Message sent successfully");
                        callback.onSuccess(message);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send message";
                        Log.e(TAG, "sendMessage: Error - " + errorMsg);
                        callback.onError(errorMsg);
                    }
                });
    }

    @Override
    public ValueEventListener getMessages(String chatId, ChatCallbacks.MessagesListener listener) {
        Log.d(TAG, "getMessages: Listening for messages in chat=" + chatId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                Collections.sort(messages, (m1, m2) ->
                        Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                Log.d(TAG, "getMessages: Loaded " + messages.size() + " messages");
                listener.onMessagesUpdated(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getMessages: onCancelled: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        };

        messagesRef.child(chatId).addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    @Override
    public void markMessagesAsRead(String chatId, String userId) {
        Log.d(TAG, "markMessagesAsRead: Marking messages in chat=" + chatId);
        messagesRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                int markedCount = 0;
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null && !message.getSenderId().equals(userId) && !message.isRead()) {
                        updates.put(messageSnapshot.getKey() + "/isRead", true);
                        markedCount++;
                    }
                }
                if (!updates.isEmpty()) {
                    Log.d(TAG, "markMessagesAsRead: Updating " + markedCount + " messages to read status");
                    messagesRef.child(chatId).updateChildren(updates);
                } else {
                    Log.d(TAG, "markMessagesAsRead: No new messages to mark as read");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "markMessagesAsRead: onCancelled: " + error.getMessage());
            }
        });
    }

    @Override
    public void markSpecificMessagesAsRead(String chatId, String userId, List<String> messageIds) {
        Log.d(TAG, "markSpecificMessagesAsRead: Marking " + messageIds.size() +
                " specific messages in chat=" + chatId);

        if (messageIds == null || messageIds.isEmpty() || chatId == null || userId == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        for (String messageId : messageIds) {
            if (messageId != null && !messageId.isEmpty()) {
                updates.put("/messages/" + chatId + "/" + messageId + "/isRead", true);
            }
        }

        if (!updates.isEmpty()) {
            FirebaseManager.getInstance().reference.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "markSpecificMessagesAsRead: Successfully updated " +
                                    updates.size() + " messages");
                        } else {
                            Log.e(TAG, "markSpecificMessagesAsRead: Failed to update messages");
                        }
                    });
        }
    }
}