package com.example.suraagh_deliverable_1.Utilities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseManager {
    private static FirebaseManager instance;
    public DatabaseReference reference;
    public FirebaseAuth auth;
    public FirebaseMessaging fm;
    public FirebaseFirestore db;

    private FirebaseManager(){
        reference = FirebaseDatabase.getInstance("https://suraaghh-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        auth = FirebaseAuth.getInstance();
        fm = FirebaseMessaging.getInstance();
        db=FirebaseFirestore.getInstance();
    }
    public static FirebaseManager getInstance(){
        if(instance == null)instance = new FirebaseManager();
        return instance;
    }

}