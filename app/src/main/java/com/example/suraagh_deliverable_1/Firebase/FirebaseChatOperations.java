package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.Database.DatabaseChatOperations;
import com.example.suraagh_deliverable_1.ModelClasses.Chat;
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

public class FirebaseChatOperations implements DatabaseChatOperations {
    private static final String TAG = "FirebaseChatOperations";
    private final DatabaseReference chatsRef;
    private static final String CHATS_NODE = "chats";
    
    public FirebaseChatOperations() {
        Log.d(TAG, "Constructor: Initializing FirebaseChatOperations");
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        chatsRef = firebaseManager.reference.child(CHATS_NODE);
    }

    @Override
    public void createChat(Chat chat, ChatCallbacks.ChatCallback<Chat> callback) {
        Log.d(TAG, "createChat: Attempting to create chat");
        String chatId = chatsRef.push().getKey();
        if (chatId == null) {
            Log.e(TAG, "createChat: Failed to generate chat ID");
            callback.onError("Failed to generate chat ID");
            return;
        }

        chat.setChatId(chatId);
        chatsRef.child(chatId).setValue(chat.toMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createChat: Success. ChatId=" + chatId);
                        callback.onSuccess(chat);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Failed to create chat";
                        Log.e(TAG, "createChat: Error - " + errorMsg);
                        callback.onError(errorMsg);
                    }
                });
    }

    @Override
    public void findExistingChat(String postId, String ownerId, String finderId,
                                 ChatCallbacks.ChatCallback<Chat> callback) {
        if (postId == null || ownerId == null || finderId == null) {
            Log.e(TAG, "findExistingChat: ABORTING - One of the IDs is null");
            callback.onError("Invalid ID parameters");
            return;
        }

        Log.d(TAG, "Debug Path: " + chatsRef.toString());
        Log.d(TAG, "findExistingChat: STARTING Query. PostID: " + postId);

        chatsRef.orderByChild("postId").equalTo(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "findExistingChat: Query returned. Children count: " + 
                                snapshot.getChildrenCount());

                        if (!snapshot.exists()) {
                            Log.d(TAG, "findExistingChat: Snapshot does not exist");
                            callback.onSuccess(null);
                            return;
                        }

                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            Chat chat = chatSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                Log.d(TAG, "Checking Chat: " + chat.getChatId() +
                                        " | Owner: " + chat.getOwnerId() +
                                        " | Finder: " + chat.getFinderId());

                                if (chat.getOwnerId().equals(ownerId) &&
                                        chat.getFinderId().equals(finderId)) {
                                    Log.d(TAG, "findExistingChat: MATCH FOUND!");
                                    callback.onSuccess(chat);
                                    return;
                                }
                            }
                        }

                        Log.d(TAG, "findExistingChat: Loop finished. No matching owner/finder pair found.");
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "findExistingChat: DB ERROR: " + error.getMessage());
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void findExistingChatByBothPosts(String lostPostId, String foundPostId,
                                            ChatCallbacks.ChatCallback<Chat> callback) {
        if (lostPostId == null || foundPostId == null) {
            Log.e(TAG, "findExistingChatByBothPosts: One of the post IDs is null");
            callback.onError("Invalid post ID parameters");
            return;
        }

        Log.d(TAG, "findExistingChatByBothPosts: Looking for chat with lostPostId=" +
                lostPostId + ", foundPostId=" + foundPostId);

        chatsRef.orderByChild("lostPostId").equalTo(lostPostId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "findExistingChatByBothPosts: Query returned " +
                                snapshot.getChildrenCount() + " chats with lostPostId");

                        if (!snapshot.exists()) {
                            Log.d(TAG, "findExistingChatByBothPosts: No chats found");
                            callback.onSuccess(null);
                            return;
                        }

                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            Chat chat = chatSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                Log.d(TAG, "Checking Chat: " + chat.getChatId() +
                                        " | lostPostId: " + chat.getLostPostId() +
                                        " | foundPostId: " + chat.getFoundPostId());

                                if (foundPostId.equals(chat.getFoundPostId())) {
                                    Log.d(TAG, "findExistingChatByBothPosts: MATCH FOUND!");
                                    callback.onSuccess(chat);
                                    return;
                                }
                            }
                        }

                        Log.d(TAG, "findExistingChatByBothPosts: No matching found post ID");
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "findExistingChatByBothPosts: DB ERROR: " + error.getMessage());
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void getChatById(String chatId, ChatCallbacks.ChatCallback<Chat> callback) {
        Log.d(TAG, "getChatById: Fetching chat=" + chatId);
        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Chat chat = snapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.d(TAG, "getChatById: Chat found");
                } else {
                    Log.w(TAG, "getChatById: Chat is null");
                }
                callback.onSuccess(chat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getChatById: onCancelled: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public ValueEventListener getChatsAsFinder(String userId, ChatCallbacks.ChatsListener listener) {
        Log.d(TAG, "getChatsAsFinder: Setting up listener for userId=" + userId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Chat> chats = processChatsSnapshot(snapshot);
                Log.d(TAG, "getChatsAsFinder: Retrieved " + chats.size() + " chats");
                listener.onChatsUpdated(chats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getChatsAsFinder: onCancelled: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        };

        chatsRef.orderByChild("finderId").equalTo(userId)
                .addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    @Override
    public ValueEventListener getChatsAsOwner(String userId, ChatCallbacks.ChatsListener listener) {
        Log.d(TAG, "getChatsAsOwner: Setting up listener for userId=" + userId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Chat> chats = processChatsSnapshot(snapshot);
                Log.d(TAG, "getChatsAsOwner: Retrieved " + chats.size() + " chats");
                listener.onChatsUpdated(chats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getChatsAsOwner: onCancelled: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        };

        chatsRef.orderByChild("ownerId").equalTo(userId)
                .addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    @Override
    public void deleteChat(String chatId, ChatCallbacks.ChatCallback<Void> callback) {
        Log.d(TAG, "deleteChat: Deleting chat=" + chatId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("/chats/" + chatId, null);
        updates.put("/messages/" + chatId, null);

        FirebaseManager.getInstance().reference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteChat: Successfully deleted");
                        callback.onSuccess(null);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Failed to delete chat";
                        Log.e(TAG, "deleteChat: Error - " + errorMsg);
                        callback.onError(errorMsg);
                    }
                });
    }

    private List<Chat> processChatsSnapshot(DataSnapshot snapshot) {
        List<Chat> chats = new ArrayList<>();
        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
            Chat chat = chatSnapshot.getValue(Chat.class);
            if (chat != null) {
                chats.add(chat);
            }
        }
        Collections.sort(chats, (c1, c2) ->
                Long.compare(c2.getLastMessageTimestamp(), c1.getLastMessageTimestamp()));
        return chats;
    }
}