package com.example.suraagh_deliverable_1.chat;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.*;
import com.example.suraagh_deliverable_1.Firebase.*;
import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.example.suraagh_deliverable_1.ModelClasses.Message;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class ChatRepository implements
        DatabaseChatOperations,
        DatabaseMessageOperations,
        DatabaseUserOperations,
        DatabaseChatStatusOperations,
        DatabaseListenerManagement {

    private static final String TAG = "ChatRepository";

    private static ChatRepository instance;

    private final DatabaseChatOperations chatOperations;
    private final DatabaseMessageOperations messageOperations;
    private final DatabaseUserOperations userOperations;
    private final DatabaseChatStatusOperations chatStatusOperations;
    private final DatabaseListenerManagement listenerManagement;

    private ChatRepository() {
        Log.d(TAG, "Constructor: Initializing ChatRepository");

        this.chatOperations = new FirebaseChatOperations();
        this.messageOperations = new FirebaseMessageOperations();
        this.userOperations = new FirebaseUserOperations();
        this.chatStatusOperations = new FirebaseChatStatusOperations();
        this.listenerManagement = new FirebaseListenerManagement();
    }

    public static synchronized ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    // ======================== Chat Operations =============================

    @Override
    public void createChat(Chat chat, ChatCallbacks.ChatCallback<Chat> callback) {
        chatOperations.createChat(chat, callback);
    }

    @Override
    public void findExistingChat(String postId, String ownerId, String finderId,
                                 ChatCallbacks.ChatCallback<Chat> callback) {
        chatOperations.findExistingChat(postId, ownerId, finderId, callback);
    }

    @Override
    public void findExistingChatByBothPosts(String lostPostId, String foundPostId,
                                            ChatCallbacks.ChatCallback<Chat> callback) {
        chatOperations.findExistingChatByBothPosts(lostPostId, foundPostId, callback);
    }

    @Override
    public void getChatById(String chatId, ChatCallbacks.ChatCallback<Chat> callback) {
        chatOperations.getChatById(chatId, callback);
    }

    @Override
    public ValueEventListener getChatsAsFinder(String userId, ChatCallbacks.ChatsListener listener) {
        return chatOperations.getChatsAsFinder(userId, listener);
    }

    @Override
    public ValueEventListener getChatsAsOwner(String userId, ChatCallbacks.ChatsListener listener) {
        return chatOperations.getChatsAsOwner(userId, listener);
    }

    @Override
    public void deleteChat(String chatId, ChatCallbacks.ChatCallback<Void> callback) {
        chatOperations.deleteChat(chatId, callback);
    }

    // ======================== Message Operations =============================

    @Override
    public void sendMessage(Message message, ChatCallbacks.ChatCallback<Message> callback) {
        messageOperations.sendMessage(message, callback);
    }

    @Override
    public ValueEventListener getMessages(String chatId, ChatCallbacks.MessagesListener listener) {
        return messageOperations.getMessages(chatId, listener);
    }

    @Override
    public void markMessagesAsRead(String chatId, String userId) {
        messageOperations.markMessagesAsRead(chatId, userId);
    }

    @Override
    public void markSpecificMessagesAsRead(String chatId, String userId, List<String> messageIds) {
        messageOperations.markSpecificMessagesAsRead(chatId, userId, messageIds);
    }

    // ======================== User Operations =============================

    @Override
    public String getCurrentUserId() {
        return userOperations.getCurrentUserId();
    }

    @Override
    public void getUserInfo(String userId, ChatCallbacks.ChatCallback<Map<String, Object>> callback) {
        userOperations.getUserInfo(userId, callback);
    }

    @Override
    public void getCurrentUserName(ChatCallbacks.ChatCallback<String> callback) {
        userOperations.getCurrentUserName(callback);
    }

    @Override
    public String getPostIdForUser(Chat chat, String userId) {
        return userOperations.getPostIdForUser(chat, userId);
    }

    @Override
    public String getOtherUserId(Chat chat, String currentUserId) {
        return userOperations.getOtherUserId(chat, currentUserId);
    }

    @Override
    public String getOtherUserName(Chat chat, String currentUserId) {
        return userOperations.getOtherUserName(chat, currentUserId);
    }

    // ======================== Chat Status Operations =============================

    @Override
    public void incrementUnreadCount(String chatId, String recipientId, boolean isOwner) {
        chatStatusOperations.incrementUnreadCount(chatId, recipientId, isOwner);
    }

    @Override
    public void resetUnreadCount(String chatId, String userId, boolean isOwner) {
        chatStatusOperations.resetUnreadCount(chatId, userId, isOwner);
    }

    // ======================== Listener Management =============================

    @Override
    public void removeChatsListener(String userId, String field, ValueEventListener listener) {
        listenerManagement.removeChatsListener(userId, field, listener);
    }

    @Override
    public void removeMessagesListener(String chatId, ValueEventListener listener) {
        listenerManagement.removeMessagesListener(chatId, listener);
    }

    @Override
    public Query getChatsQuery(String userId, String field) {
        return listenerManagement.getChatsQuery(userId, field);
    }
}
