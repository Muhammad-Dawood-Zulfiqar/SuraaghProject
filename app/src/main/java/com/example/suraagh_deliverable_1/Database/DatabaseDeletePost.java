package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBackDelete;

public interface DatabaseDeletePost {
    public void deletePost(String CollectionName,String postId, CallBackDelete callBackDelete);
}
