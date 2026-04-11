package com.example.suraagh_deliverable_1.AI;

import android.content.Context;
import android.util.Log;

import com.example.suraagh_deliverable_1.Firebase.CallBack;
import com.example.suraagh_deliverable_1.Firebase.FirebaseCreatePost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostRepository {

    private static final String TAG = "PostRepository";
    private FirebaseFirestore db;
    private SmartMatcher smartMatcher;
    private MatchMaker matchMaker;
    private FirebaseCreatePost firebaseCreatePost;

    public PostRepository(Context context, String apiKey) {
        this.db = FirebaseManager.getInstance().db;
        this.smartMatcher = new SmartMatcher(context, apiKey);
        this.matchMaker = new MatchMaker(context, apiKey);
        this.firebaseCreatePost = new FirebaseCreatePost();
    }

    public interface PostCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // --- CREATE ---
    public void createPostWithAI(Post post, List<String> base64Images, boolean isLostPost, PostCallback callback) {
        handleAIAndUpload(post, base64Images, isLostPost, false, null, callback);
    }

    // --- UPDATE (New!) ---
    public void updatePostWithAI(Post post, String postId, List<String> base64Images, boolean isLostPost, PostCallback callback) {
        handleAIAndUpload(post, base64Images, isLostPost, true, postId, callback);
    }

    // Shared Logic for both Create and Update
// In PostRepository.java

    private void handleAIAndUpload(Post post, List<String> base64Images, boolean isLostPost, boolean isUpdate, String postId, PostCallback callback) {

        // --- STEP 1: Get Image Description (If images exist) ---
        if (base64Images != null && !base64Images.isEmpty()) {
            smartMatcher.getDescriptionsFromImages(base64Images, new SmartMatcher.DescriptionCallback() {
                @Override
                public void onDescriptionReady(String imageDescription) {
                    // Combine User Text + AI Vision Text
                    String fullText = post.getSearchableText();
                    if (imageDescription != null && !imageDescription.isEmpty()) {
                        fullText += " \nVisual Details: " + imageDescription;
                    }
                    generateEmbeddingAndSave(post, fullText, isLostPost, isUpdate, postId, callback);
                }
            });
        } else {
            // No images? Just use the text provided by user
            generateEmbeddingAndSave(post, post.getSearchableText(), isLostPost, isUpdate, postId, callback);
        }
    }

    private void generateEmbeddingAndSave(Post post, String textToEmbed, boolean isLostPost, boolean isUpdate, String postId, PostCallback callback) {
        // --- STEP 2: Generate Vector ---
        smartMatcher.getEmbedding(textToEmbed, new SmartMatcher.EmbeddingCallback() {
            @Override
            public void onEmbeddingReady(List<Double> vector) {
                if (vector != null) {
                    post.setEmbedding(vector);
                } else {
                    Log.w(TAG, "Embedding generation failed. Saving without AI vector.");
                }
                // --- STEP 3: Save to DB ---
                executeDatabaseTransaction(post, isLostPost, isUpdate, postId, callback);
            }
        });
    }
//    private void getVectorAndProceed(Post post, String textForAI, boolean isLostPost, boolean isUpdate, String postId, PostCallback callback) {
//        smartMatcher.getEmbedding(textForAI, new SmartMatcher.EmbeddingCallback() {
//            @Override
//            public void onEmbeddingReady(List<Double> vector) {
//                post.setEmbedding(vector);
//                executeDatabaseTransaction(post, isLostPost, isUpdate, postId, callback);
//            }
//
//            @Override
//            public void onError(String error) {
//                Log.e(TAG, "Embedding failed: " + error + ". Saving without AI.");
//                executeDatabaseTransaction(post, isLostPost, isUpdate, postId, callback);
//            }
//        });
//    }

    private void executeDatabaseTransaction(Post post, boolean isLostPost, boolean isUpdate, String postId, PostCallback callback) {
        String collectionName = isLostPost ? "lostPosts" : "foundPosts";

        if (isUpdate) {
            // --- UPDATE PATH ---
            firebaseCreatePost.updatePost(collectionName, postId, post, new CallBack() {
                @Override
                public void onSuccess() {
                    // MatchMaker runs AFTER the update is committed
                    matchMaker.checkForMatches(post, collectionName);
                    callback.onSuccess("Post Updated & Matches Refreshed!");
                }
                @Override
                public void onFailure(String error) { callback.onError(error); }
            });
        } else {
            // --- CREATE PATH ---
            String newId = db.collection(collectionName).document().getId();
            post.setPostId(newId);

            firebaseCreatePost.createPost(collectionName, post, new CallBack() {
                @Override
                public void onSuccess() {
                    matchMaker.checkForMatches(post, collectionName);
                    callback.onSuccess("Post Created & Matching Started!");
                }
                @Override
                public void onFailure(String error) { callback.onError(error); }
            });
        }
    }
}