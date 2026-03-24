package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBackForGetPost;

public interface DataBaseGetFoundPosts {
    public void getFoundPosts(String userId, CallBackForGetPost callBackForGetPost);
}
