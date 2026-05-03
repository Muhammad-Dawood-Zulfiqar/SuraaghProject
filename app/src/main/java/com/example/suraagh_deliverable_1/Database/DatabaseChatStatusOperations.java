package com.example.suraagh_deliverable_1.Database;

public interface DatabaseChatStatusOperations {
    void incrementUnreadCount(String chatId, String recipientId, boolean isOwner);
    void resetUnreadCount(String chatId, String userId, boolean isOwner);
}