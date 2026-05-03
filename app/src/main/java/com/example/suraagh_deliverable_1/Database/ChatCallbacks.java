package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.example.suraagh_deliverable_1.ModelClasses.Message;

import java.util.List;

public interface ChatCallbacks {
    interface ChatCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    interface ChatsListener {
        void onChatsUpdated(List<Chat> chats);
        void onError(String error);
    }
    
    interface MessagesListener {
        void onMessagesUpdated(List<Message> messages);
        void onError(String error);
    }
}