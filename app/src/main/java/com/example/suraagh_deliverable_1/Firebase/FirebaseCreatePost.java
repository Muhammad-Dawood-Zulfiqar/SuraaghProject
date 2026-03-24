package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.DatabaseCreatePost;
import com.example.suraagh_deliverable_1.Database.DatabaseDeletePost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseCreatePost implements DatabaseCreatePost {

    private static final String TAG = "FirebaseCreatePost";

    @Override
    public void createPost(String collectionName, Post post, CallBack callBack) {
        FirebaseFirestore db = FirebaseManager.getInstance().db;

        if (post.getPostId() == null) {
            callBack.onFailure("Post ID is null");
            return;
        }

        db.collection(collectionName)
                .document(post.getPostId())
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase post created with id " + post.getPostId());
                    callBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Firebase create post failed: " + e.getMessage());
                    callBack.onFailure(e.getMessage());
                });
    }

    @Override
    public void updatePost(String collectionName, String postId, Post post, CallBack callback) {
        Log.d(TAG, "Update requested. Starting Delete-then-Create sequence for: " + postId);

        // 1. Initialize the Delete Helper (This deletes Post + Matches)
        // Ensure you have FirebaseDeletePost implementing DatabaseDeletePost
        DatabaseDeletePost deleteHelper = new FirebaseDeletePost();

        // 2. Delete the old post AND its associated matches first
        deleteHelper.deletePost(collectionName, postId, new CallBackDelete() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Old post and matches deleted. Re-creating post...");

                // 3. Ensure the post object keeps the same ID
                post.setPostId(postId);

                // 4. Save the new data (Reuse the createPost logic)
                createPost(collectionName, post, callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Update Failed during Delete phase", e);
                callback.onFailure("Failed to clear old data: " + e.getMessage());
            }
        });
    }
}