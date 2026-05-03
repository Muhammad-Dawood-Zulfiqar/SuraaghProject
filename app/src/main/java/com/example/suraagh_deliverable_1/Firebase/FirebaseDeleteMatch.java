package com.example.suraagh_deliverable_1.Firebase;

import android.util.Log;

import com.example.suraagh_deliverable_1.Database.DatabaseDeleteMatch;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseDeleteMatch implements DatabaseDeleteMatch {

    private static final String TAG = "FirebaseDeleteMatch";

    @Override
    public void deleteMatch(String matchId, CallBackDelete callBack) {
        FirebaseFirestore db = FirebaseManager.getInstance().db;

        db.collection("matches")
                .document(matchId)
                .delete()
                .addOnSuccessListener(callBack::onSuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete match: " + matchId, e);
                    callBack.onFailure(e);
                });
    }
}