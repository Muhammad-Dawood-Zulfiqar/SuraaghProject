package com.example.suraagh_deliverable_1.ModelClasses;

public class User {
    String userId;
    String userName;
    String email;

    public User() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTrustPoints() {
        return trustPoints;
    }

    public void setTrustPoints(int trustPoints) {
        this.trustPoints = trustPoints;
    }

    public int getReportStrikes() {
        return reportStrikes;
    }

    public void setReportStrikes(int reportStrikes) {
        this.reportStrikes = reportStrikes;
    }

    public User(String userId, String userName, String email, int trustPoints, int reportStrikes) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.trustPoints = trustPoints;
        this.reportStrikes = reportStrikes;
    }

    int trustPoints;
    int reportStrikes;

}
