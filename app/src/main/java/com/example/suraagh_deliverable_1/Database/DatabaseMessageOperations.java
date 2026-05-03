package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.ModelClasses.Message;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public interface DatabaseMessageOperations {
    void sendMessage(Message message, ChatCallbacks.ChatCallback<Message> callback);
    ValueEventListener getMessages(String chatId, ChatCallbacks.MessagesListener listener);
    void markMessagesAsRead(String chatId, String userId);
    void markSpecificMessagesAsRead(String chatId, String userId, List<String> messageIds);
}