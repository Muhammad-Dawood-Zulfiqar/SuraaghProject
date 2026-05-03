package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.ModelClasses.Chat;

import java.util.Map;

public interface DatabaseUserOperations {
    String getCurrentUserId();
    void getUserInfo(String userId, ChatCallbacks.ChatCallback<Map<String, Object>> callback);
    void getCurrentUserName(ChatCallbacks.ChatCallback<String> callback);
    String getPostIdForUser(Chat chat, String userId);
    String getOtherUserId(Chat chat, String currentUserId);
    String getOtherUserName(Chat chat, String currentUserId);
}