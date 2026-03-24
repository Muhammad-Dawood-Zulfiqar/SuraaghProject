package com.example.suraagh_deliverable_1.Firebase;

import com.example.suraagh_deliverable_1.Database.DataBaseGetFoundPosts;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;

public class FirebaseGetFoundPosts implements DataBaseGetFoundPosts {

    @Override
    public void getFoundPosts(String userID,CallBackForGetPost callBackForGetPost) {
        FirebaseManager.getInstance().db.collection("foundPosts").whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(callBackForGetPost::onComplete);
    }
}
