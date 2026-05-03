package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBackDelete;

public interface DatabaseDeleteMatch {
    void deleteMatch(String matchId, CallBackDelete callBack);
}