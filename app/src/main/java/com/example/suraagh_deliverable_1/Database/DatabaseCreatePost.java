package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBack;
import com.example.suraagh_deliverable_1.ModelClasses.Post;

public interface DatabaseCreatePost {

    public  void createPost(String CollectionName, Post post, CallBack callBack);

        void updatePost(String collectionName, String postId, Post post, CallBack callback);


}
