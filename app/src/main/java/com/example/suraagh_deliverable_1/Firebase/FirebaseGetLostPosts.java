package com.example.suraagh_deliverable_1.Firebase;

import com.example.suraagh_deliverable_1.Database.DatabaseGetLostPosts;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;

public class FirebaseGetLostPosts implements DatabaseGetLostPosts {


    @Override
    public void getLostPosts(String userId,CallBackForGetPost callBackForGetPost) {
        FirebaseManager.getInstance().db.collection("lostPosts").whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(callBackForGetPost::onComplete);
    }
}
