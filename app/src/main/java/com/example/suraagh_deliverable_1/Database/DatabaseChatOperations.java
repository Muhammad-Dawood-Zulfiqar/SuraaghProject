package com.example.suraagh_deliverable_1.Database;
import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.google.firebase.database.ValueEventListener;

public interface DatabaseChatOperations {
    void createChat(Chat chat, ChatCallbacks.ChatCallback<Chat> callback);
    void findExistingChat(String postId, String ownerId, String finderId, ChatCallbacks.ChatCallback<Chat> callback);
    void findExistingChatByBothPosts(String lostPostId, String foundPostId, ChatCallbacks.ChatCallback<Chat> callback);
    void getChatById(String chatId, ChatCallbacks.ChatCallback<Chat> callback);
    ValueEventListener getChatsAsFinder(String userId, ChatCallbacks.ChatsListener listener);
    ValueEventListener getChatsAsOwner(String userId, ChatCallbacks.ChatsListener listener);
    void deleteChat(String chatId, ChatCallbacks.ChatCallback<Void> callback);
}