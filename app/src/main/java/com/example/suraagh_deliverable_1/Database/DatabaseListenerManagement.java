package com.example.suraagh_deliverable_1.Database;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public interface DatabaseListenerManagement {
    void removeChatsListener(String userId, String field, ValueEventListener listener);
    void removeMessagesListener(String chatId, ValueEventListener listener);
    Query getChatsQuery(String userId, String field);
}