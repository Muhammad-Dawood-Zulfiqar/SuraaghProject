package com.example.suraagh_deliverable_1.AI;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.suraagh_deliverable_1.Database.DatabaseCreateMatch;
import com.example.suraagh_deliverable_1.Firebase.CallBack;
import com.example.suraagh_deliverable_1.Firebase.FirebaseCreateMatch;
import com.example.suraagh_deliverable_1.ModelClasses.Match;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MatchMaker {

    private static final String TAG = "SURAAGH_AI_MATCH";
    private SmartMatcher smartMatcher;
    private FirebaseFirestore db;
    private Context context;

    // DIP Interface for WRITING
    private DatabaseCreateMatch databaseCreateMatch;

    // --- CONFIGURATION ---
    private static final double MAX_DISTANCE_KM = 10.0;
    private static final double MIN_SIMILARITY_PERCENT = 86.0;
    private static final int SEARCH_LIMIT = 300;
    // 15 Day Window (Total 30 days range)
    private static final long TIME_WINDOW_MS = TimeUnit.DAYS.toMillis(15);

    public MatchMaker(Context context, String apiKey) {
        this.context = context;
        this.db = FirebaseManager.getInstance().db;
        this.smartMatcher = new SmartMatcher(context, apiKey);
        this.databaseCreateMatch = new FirebaseCreateMatch();
        Log.d(TAG, "MatchMaker Initialized");
    }

    public void checkForMatches(Post newPost, String collectionName) {
        Log.d(TAG, "checkForMatches() triggered. Collection: " + collectionName);

        if (newPost == null || newPost.getEmbedding() == null) {
            Log.e(TAG, "ABORT: Cannot match. Post or Embedding is null.");
            return;
        }

        Log.d(TAG, "New Post ID: " + newPost.getPostId() + " | Type: " + newPost.getType() + " | UserID: " + newPost.getUserId());

        // Determine target collection (Lost searches Found, Found searches Lost)
        String candidateCollection = collectionName.equals("lostPosts") ? "foundPosts" : "lostPosts";
        boolean isNewPostFoundItem = collectionName.equals("foundPosts");

        Log.d(TAG, "Targeting candidate collection: " + candidateCollection);

        // --- PRODUCTION OPTIMIZATION: RANGE QUERY ---
        long minTimestamp = newPost.getTimestamp() - TIME_WINDOW_MS;
        long maxTimestamp = newPost.getTimestamp() + TIME_WINDOW_MS;

        Log.d(TAG, "Query Params -> Type: " + newPost.getType() + " | MinTime: " + minTimestamp + " | MaxTime: " + maxTimestamp);

        db.collection(candidateCollection)
                .whereEqualTo("type", newPost.getType()) // 1. Filter by Category (Phone, Pet, etc)
                .whereGreaterThanOrEqualTo("timestamp", minTimestamp) // 2. Filter by Date Range Start
                .whereLessThanOrEqualTo("timestamp", maxTimestamp)    // 3. Filter by Date Range End
                .limit(SEARCH_LIMIT) // 4. Safety Limit
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Log.d(TAG, "Query executed successfully. Found " + querySnapshots.size() + " documents.");

                    if (querySnapshots.isEmpty()) {
                        Log.w(TAG, "Query returned 0 candidates. No matches can be processed.");
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Log.d(TAG, "Evaluating candidate Doc ID: " + doc.getId());
                        try {
                            Post candidate = doc.toObject(Post.class);
                            candidate.setPostId(doc.getId());

                            // Double check User ID (Don't match with yourself)
                            if (candidate.getUserId() != null && candidate.getUserId().equals(newPost.getUserId())) {
                                Log.d(TAG, "Skipped Doc ID: " + doc.getId() + " (Belongs to same UserID)");
                                continue;
                            }

                            Log.d(TAG, "Candidate passed preliminary checks. Sending to processCandidate()...");
                            processCandidate(newPost, candidate, isNewPostFoundItem);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing candidate Doc ID: " + doc.getId() + ". Error: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Match Query entirely Failed: " + e.getMessage());
                });
    }

    private void processCandidate(Post newPost, Post candidate, boolean isNewPostFoundItem) {
        if (candidate.getEmbedding() == null) {
            Log.w(TAG, "Candidate " + candidate.getPostId() + " skipped (Embedding is null).");
            return;
        }

        // 1. Text Similarity (Semantic) - 70% Weight
        double textScore = smartMatcher.calculateSimilarity(newPost.getEmbedding(), candidate.getEmbedding());
        Log.d(TAG, "Step 1: Text Score calculated = " + textScore);

        // 2. Location Score - 20% Weight
        double dist = LocationUtils.getDistanceInKm(
                newPost.getLatitude(), newPost.getLongitude(),
                candidate.getLatitude(), candidate.getLongitude()
        );
        Log.d(TAG, "Step 2: Distance calculated = " + dist + " km");

        // Linear Decay: 0km = 1.0 score, 10km = 0.0 score.
        double distanceScore = (dist > MAX_DISTANCE_KM) ? 0.0 : (1.0 - (dist / MAX_DISTANCE_KM));

        // If location is 0,0 (User didn't provide), we ignore distance penalty (Score 0.5 neutral)
        if (newPost.getLatitude() == 0 && newPost.getLongitude() == 0) {
            distanceScore = 0.5;
            Log.d(TAG, "Location is 0,0. Distance Score defaulted to 0.5");
        } else {
            Log.d(TAG, "Distance Score applied = " + distanceScore);
        }

        // 3. Date Score - 10% Weight
        double dateScore = DateUtils.calculateTimeScore(newPost.getTimestamp(), candidate.getTimestamp());
        Log.d(TAG, "Step 3: Date Score calculated = " + dateScore);

        // --- FINAL WEIGHTED FORMULA ---
        double totalScore = (textScore * 0.70) + (distanceScore * 0.20) + (dateScore * 0.10);
        float percent = (float) (totalScore * 100);

        Log.d(TAG, String.format("Match Results: Target ID %s | Text: %.2f | Dist: %.2f | Date: %.2f | TOTAL: %.2f%%",
                candidate.getPostId(), textScore, distanceScore, dateScore, percent));

        if (percent >= MIN_SIMILARITY_PERCENT) {
            Log.d(TAG, "SUCCESS: " + percent + "% meets threshold of " + MIN_SIMILARITY_PERCENT + "%. Triggering uploadMatch().");
            uploadMatch(newPost, candidate, percent, isNewPostFoundItem);
        } else {
            Log.w(TAG, "DROPPED: " + percent + "% is below threshold of " + MIN_SIMILARITY_PERCENT + "%.");
        }
    }

    private void uploadMatch(Post newPost, Post candidatePost, float percent, boolean isNewPostFoundItem) {
        Log.d(TAG, "uploadMatch() initiated.");

        Match match = new Match();
        match.setMatchId(UUID.randomUUID().toString());
        match.setPercentageMatch(percent);

        if (isNewPostFoundItem) {
            match.setFinderId(newPost.getUserId());
            match.setFinderPostId(newPost.getPostId());
            match.setOwnerId(candidatePost.getUserId());
            match.setLostPostId(candidatePost.getPostId());
        } else {
            match.setOwnerId(newPost.getUserId());
            match.setLostPostId(newPost.getPostId());
            match.setFinderId(candidatePost.getUserId());
            match.setFinderPostId(candidatePost.getPostId());
        }

        Log.d(TAG, "Match Data Ready -> MatchID: " + match.getMatchId() +
                ", FinderID: " + match.getFinderId() +
                ", OwnerID: " + match.getOwnerId() +
                ", LostPostID: " + match.getLostPostId() +
                ", FinderPostID: " + match.getFinderPostId());

        if (databaseCreateMatch == null) {
            Log.e(TAG, "CRITICAL: databaseCreateMatch is null. Cannot write to DB.");
            return;
        }

        Log.d(TAG, "Calling databaseCreateMatch.createMatch()...");

        databaseCreateMatch.createMatch(match, new CallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ DATABASE WRITE SUCCESS: Match Saved Successfully!");

                // --- NEW NOTIFICATION LOGIC ---
                String targetUserId = candidatePost.getUserId();
                String itemName = candidatePost.getThing() != null ? candidatePost.getThing() : candidatePost.getType();

                Log.d(TAG, "Proceeding to notify target user: " + targetUserId);
                notifyUserOfMatch(targetUserId, itemName, match.getMatchId());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "❌ DATABASE WRITE FAILED. Reason: " + errorMessage);
            }
        });
    }

    // ==========================================
    // NOTIFICATION HELPERS
    // ==========================================

    private void notifyUserOfMatch(String targetUserId, String itemName, String matchId) {
        Log.d(TAG, "notifyUserOfMatch() triggered for user: " + targetUserId);

        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                        String fcmToken = documentSnapshot.getString("fcmToken");

                        if (fcmToken != null && !fcmToken.isEmpty()) {
                            Log.d(TAG, "FCM Token retrieved. Sending via Vercel...");
                            sendPushNotificationViaVercel(fcmToken, itemName, matchId);
                        } else {
                            Log.w(TAG, "User document exists, but FCM token is null or empty.");
                        }
                    } else {
                        Log.w(TAG, "User document doesn't exist or has no 'fcmToken' field.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user token from Firestore", e));
    }

    private void sendPushNotificationViaVercel(String fcmToken, String itemName, String matchId) {
        String vercelUrl = "https://suraagh-backend.vercel.app/api/sendNotification";
        Log.d(TAG, "Preparing Notification JSON for Vercel endpoint...");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("token", fcmToken);
            jsonBody.put("title", "Match Found! 🎉");
            jsonBody.put("body", "We found a potential match for: " + itemName);
            jsonBody.put("matchId", matchId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, vercelUrl, jsonBody,
                    response -> Log.d(TAG, "Notification successfully sent via Vercel!"),
                    error -> Log.e(TAG, "Failed to trigger Vercel notification. " + error.toString())
            );

            Volley.newRequestQueue(context).add(request);
            Log.d(TAG, "Vercel Volley request added to queue.");

        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON body for notification", e);
        }
    }
}