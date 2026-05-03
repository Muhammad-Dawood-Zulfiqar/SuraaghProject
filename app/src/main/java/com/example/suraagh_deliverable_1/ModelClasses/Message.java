package com.example.suraagh_deliverable_1.ModelClasses;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single message in a chat conversation.
 */
public class Message {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private long timestamp;
    private boolean isRead;

    private boolean isDelivered;
    private long deliveredTimestamp;
    private String messageType; // "text", "image", "location" (for future expansion)

    // Required empty constructor for Firebase
    public Message() {
    }

    public Message(String messageId, String chatId, String senderId,
                   String senderName, String content) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.isDelivered = false; // Default to not delivered
        this.messageType = "text";
        this.deliveredTimestamp = 0; // 0 means not delivered yet
    }

    public long getDeliveredTimestamp() {
        return deliveredTimestamp;
    }

    public void setDeliveredTimestamp(long deliveredTimestamp) {
        this.deliveredTimestamp = deliveredTimestamp;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("chatId", chatId);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("content", content);
        result.put("timestamp", timestamp);
        result.put("isRead", isRead);
        result.put("isDelivered", isDelivered); // Add this
        result.put("deliveredTimestamp", deliveredTimestamp); // Add this
        result.put("messageType", messageType);
        return result;
    }

    // Getters and Setters
    @PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    @PropertyName("isRead")
    public void setRead(boolean read) {
        isRead = read;
    }

    @PropertyName("isDelivered")
    public boolean isDelivered() {
        return isDelivered;
    }

    @PropertyName("isDelivered")
    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }




    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Check if this message was sent by the specified user
     */
    public boolean isSentBy(String userId) {
        return senderId != null && senderId.equals(userId);
    }
}

