package com.example.suraagh_deliverable_1.ModelClasses;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    private String chatId;
    private String lostPostId;
    private String lostPostTitle;
    private String foundPostId;
    private String foundPostTitle;
    private String ownerId;
    private String ownerName;
    private String finderId;
    private String finderName;
    private String lastMessage;
    private long lastMessageTimestamp;
    private String lastMessageSenderId;
    private int unreadCountOwner;
    private int unreadCountFinder;
    private long createdAt;

    // Required empty constructor for Firebase
    public Chat() {}

    // Constructor for creating chat from match results
    public Chat(String chatId, String lostPostId, String lostPostTitle,
                String foundPostId, String foundPostTitle,
                String ownerId, String ownerName,
                String finderId, String finderName) {
        this.chatId = chatId;
        this.lostPostId = lostPostId;
        this.lostPostTitle = lostPostTitle;
        this.foundPostId = foundPostId;
        this.foundPostTitle = foundPostTitle;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.finderId = finderId;
        this.finderName = finderName;
        this.lastMessage = "";
        this.lastMessageTimestamp = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.unreadCountOwner = 0;
        this.unreadCountFinder = 0;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("chatId", chatId);
        result.put("lostPostId", lostPostId);
        result.put("lostPostTitle", lostPostTitle);
        result.put("foundPostId", foundPostId);
        result.put("foundPostTitle", foundPostTitle);
        result.put("ownerId", ownerId);
        result.put("ownerName", ownerName);
        result.put("finderId", finderId);
        result.put("finderName", finderName);
        result.put("lastMessage", lastMessage);
        result.put("lastMessageTimestamp", lastMessageTimestamp);
        result.put("lastMessageSenderId", lastMessageSenderId);
        result.put("unreadCountOwner", unreadCountOwner);
        result.put("unreadCountFinder", unreadCountFinder);
        result.put("createdAt", createdAt);
        return result;
    }

    // Getters and Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLostPostId() {
        return lostPostId;
    }

    public void setLostPostId(String lostPostId) {
        this.lostPostId = lostPostId;
    }

    public String getLostPostTitle() {
        return lostPostTitle;
    }

    public void setLostPostTitle(String lostPostTitle) {
        this.lostPostTitle = lostPostTitle;
    }

    public String getFoundPostId() {
        return foundPostId;
    }

    public void setFoundPostId(String foundPostId) {
        this.foundPostId = foundPostId;
    }

    public String getFoundPostTitle() {
        return foundPostTitle;
    }

    public void setFoundPostTitle(String foundPostTitle) {
        this.foundPostTitle = foundPostTitle;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getFinderId() {
        return finderId;
    }

    public void setFinderId(String finderId) {
        this.finderId = finderId;
    }

    public String getFinderName() {
        return finderName;
    }

    public void setFinderName(String finderName) {
        this.finderName = finderName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public int getUnreadCountOwner() {
        return unreadCountOwner;
    }

    public void setUnreadCountOwner(int unreadCountOwner) {
        this.unreadCountOwner = unreadCountOwner;
    }

    public int getUnreadCountFinder() {
        return unreadCountFinder;
    }

    public void setUnreadCountFinder(int unreadCountFinder) {
        this.unreadCountFinder = unreadCountFinder;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the unread count for a specific user
     */
    public int getUnreadCountForUser(String userId) {
        if (userId.equals(ownerId)) {
            return unreadCountOwner;
        } else if (userId.equals(finderId)) {
            return unreadCountFinder;
        }
        return 0;
    }

    /**
     * Check if the current user is the owner in this chat
     */
    public boolean isUserOwner(String userId) {
        return userId.equals(ownerId);
    }

    /**
     * Check if the current user is the finder in this chat
     */
    public boolean isUserFinder(String userId) {
        return userId.equals(finderId);
    }

    /**
     * Get the correct post ID based on who is viewing
     * - Finder sees the LOST post (owner lost something)
     * - Owner sees the FOUND post (finder found something)
     */
    public String getPostIdForUser(String userId) {
        if (isUserOwner(userId)) {
            return foundPostId; // Owner sees found post
        } else if (isUserFinder(userId)) {
            return lostPostId; // Finder sees lost post
        }
        return "";
    }

    /**
     * Get the appropriate post title based on user's role
     */
    public String getPostTitleForUser(String userId) {
        if (isUserOwner(userId)) {
            return foundPostTitle; // Owner sees found post title
        } else if (isUserFinder(userId)) {
            return lostPostTitle; // Finder sees lost post title
        }
        return "";
    }

    /**
     * Get the other participant's ID
     * @deprecated Use getOtherUserId() instead - keeping for backward compatibility
     */
    public String getOtherParticipantId(String currentUserId) {
        return getOtherUserId(currentUserId);
    }

    /**
     * Get the other participant's name
     * @deprecated Use getOtherUserName() instead - keeping for backward compatibility
     */
    public String getOtherParticipantName(String currentUserId) {
        return getOtherUserName(currentUserId);
    }

    /**
     * Get the other participant's ID
     */
    public String getOtherUserId(String currentUserId) {
        if (currentUserId.equals(ownerId)) {
            return finderId;
        } else if (currentUserId.equals(finderId)) {
            return ownerId;
        }
        return "";
    }

    /**
     * Get the other participant's name
     */
    public String getOtherUserName(String currentUserId) {
        if (currentUserId.equals(ownerId)) {
            return finderName;
        } else if (currentUserId.equals(finderId)) {
            return ownerName;
        }
        return "";
    }

    /**
     * Get post type for display
     */
    public String getPostTypeForUser(String userId) {
        if (isUserOwner(userId)) {
            return "FOUND";
        } else if (isUserFinder(userId)) {
            return "LOST";
        }
        return "";
    }
}