package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.DatabaseDeletePost;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class FirebaseDeletePost implements DatabaseDeletePost {

    private static final String TAG = "FirebaseDeletePost";

    @Override
    public void deletePost(String collectionName, String postId, CallBackDelete callBackDelete) {
        FirebaseFirestore db = FirebaseManager.getInstance().db;
        WriteBatch batch = db.batch();

        // 1. Queue the Post itself for deletion
        batch.delete(db.collection(collectionName).document(postId));

        // 2. Determine which field to query in the "matches" collection
        String fieldToQuery;
        if (collectionName.equals("lostPosts")) {
            // If I am deleting a Lost Post, I am the "lostPostId" in the match
            fieldToQuery = "lostPostId";
        } else {
            // If I am deleting a Found Post, I am the "finderPostId" in the match
            fieldToQuery = "finderPostId";
        }

        // 3. Find ONLY the relevant matches
        Task<QuerySnapshot> queryMatches = db.collection("matches")
                .whereEqualTo(fieldToQuery, postId)
                .get();

        // 4. Process the query and delete
        queryMatches.addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    batch.delete(doc.getReference());
                    Log.d(TAG, "Queuing match deletion: " + doc.getId());
                }
            } else {
                Log.d(TAG, "No associated matches found to delete.");
            }

            // 5. Commit everything (Post + Matches)
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Batch delete successful");
                        callBackDelete.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Batch delete failed", e);
                        callBackDelete.onFailure(e);
                    });

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to query matches", e);
            callBackDelete.onFailure(e);
        });
    }
}