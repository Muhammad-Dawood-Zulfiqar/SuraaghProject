package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.DatabaseCreateMatch;
import com.example.suraagh_deliverable_1.ModelClasses.Match;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseCreateMatch implements DatabaseCreateMatch {

    private static final String TAG = "FirebaseCreateMatch";

    @Override
    public void createMatch(Match match, CallBack callBack) {
        FirebaseFirestore db = FirebaseManager.getInstance().db;

        if (match.getMatchId() == null) {
            callBack.onFailure("Match ID cannot be null");
            return;
        }

        db.collection("matches")
                .document(match.getMatchId())
                .set(match)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Match created successfully: " + match.getMatchId());
                    callBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create match", e);
                    callBack.onFailure(e.getMessage());
                });
    }
}