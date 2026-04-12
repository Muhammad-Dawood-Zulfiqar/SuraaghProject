package com.example.suraagh_deliverable_1.Database;

import com.example.suraagh_deliverable_1.Firebase.CallBack;
import com.example.suraagh_deliverable_1.ModelClasses.Match;

public interface DatabaseCreateMatch {
    void createMatch(Match match, CallBack callBack);
}