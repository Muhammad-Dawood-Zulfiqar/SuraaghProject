package com.example.suraagh_deliverable_1.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.suraagh_deliverable_1.ModelClasses.FoundPost;
import com.example.suraagh_deliverable_1.ModelClasses.LostPost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStorage {
    private static final String PREF_NAME = "SuraaghCache";
    private static final String KEY_LOST = "cached_lost_posts";
    private static final String KEY_FOUND = "cached_found_posts";
    private static final String KEY_USER = "cached_user_profile"; // New key for user
    private SharedPreferences prefs;
    private Gson gson;

    public LocalStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- SAVE ---
    public void saveUser(User user) {
        String json = gson.toJson(user);
        prefs.edit().putString(KEY_USER, json).apply();
    }

    public void saveLostPosts(List<Post> posts) {
        String json = gson.toJson(posts);
        prefs.edit().putString(KEY_LOST, json).apply();
    }

    public void saveFoundPosts(List<Post> posts) {
        String json = gson.toJson(posts);
        prefs.edit().putString(KEY_FOUND, json).apply();
    }

    // --- LOAD ---
    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, User.class);
    }

    public List<Post> loadLostPosts() {
        String json = prefs.getString(KEY_LOST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<LostPost>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public List<Post> loadFoundPosts() {
        String json = prefs.getString(KEY_FOUND, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<FoundPost>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Clear data on Logout
    public void clearData() {
        prefs.edit().clear().apply();
    }
}