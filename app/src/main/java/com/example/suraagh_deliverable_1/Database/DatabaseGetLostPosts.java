package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBackForGetPost;

public interface DatabaseGetLostPosts {
    public void getLostPosts(String userId, CallBackForGetPost callBackForGetPost);
}
