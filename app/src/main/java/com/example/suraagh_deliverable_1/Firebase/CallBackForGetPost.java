package com.example.suraagh_deliverable_1.Firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public interface CallBackForGetPost {
    public void onComplete(Task<QuerySnapshot> task);
}
