package com.example.suraagh_deliverable_1.ModelClasses;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Post implements Serializable {
    String id;
    String userId;

    // Primary storage for Date (Milliseconds)
    long timestamp;

    // Item Details
    String thing;
    String type;
    String material;
    String color;
    String size;
    String additionalInfo;

    // Location Details
    double longitude;
    double latitude;
    String address;
    List<String> imageUrl;

    // AI Embedding
    List<Double> embedding;

    public Post() {
        // Empty constructor needed for Firestore
    }

    @Exclude
    public String getSearchableText() {
        StringBuilder sb = new StringBuilder();

        // STEP 1: Construct the "Core Identity" (Adjectives + Noun)
        // Result: "Navy Blue Leather Men's Wallet" (Much stronger than separate fields)
        if (color != null && !color.isEmpty()) sb.append(color).append(" ");
        if (size != null && !size.isEmpty()) sb.append(size).append(" ");
        if (material != null && !material.isEmpty()) sb.append(material).append(" ");

        // The Main Object
        sb.append(thing != null ? thing : "Item");

        // STEP 2: Add Category Context
        if (type != null && !type.isEmpty()) sb.append(" (").append(type).append("). ");
        else sb.append(". ");

        // STEP 3: Add Unique Identifiers (Crucial for specific matching)
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            sb.append("Key Features: ").append(additionalInfo).append(". ");
        }

        // STEP 4: Location Context
        if (address != null && !address.isEmpty()) {
            sb.append("Location context: ").append(address).append(".");
        }

        return sb.toString().trim();
    }

    // --- NEW: Helper to get readable Date String from Timestamp ---
    // Usage: post.getDate() returns "12/10/2024"
    // @Exclude prevents Firestore from saving this as a field in the database
    @Exclude
    public String getDate() {
        if (timestamp == 0) return "";
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // --- GETTERS AND SETTERS ---

    @Exclude
    public String getPostId() { return id; }

    @DocumentId
    public void setPostId(String postId) { this.id = postId; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    // Timestamp Getter/Setter (Used by Firestore)
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getThing() { return thing; }
    public void setThing(String thing) { this.thing = thing; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getImageUrl() { return imageUrl; }
    public void setImageUrl(List<String> imageUrl) { this.imageUrl = imageUrl; }

    // Constructor
    public Post(String postId, String userId, String type, String material, String color, String size, String additionalInfo, double longitude, double latitude, String address, List<String> imageUrl, long timestamp) {
        this.id = postId;
        this.userId = userId;
        this.type = type;
        this.material = material;
        this.color = color;
        this.size = size;
        this.additionalInfo = additionalInfo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }
}