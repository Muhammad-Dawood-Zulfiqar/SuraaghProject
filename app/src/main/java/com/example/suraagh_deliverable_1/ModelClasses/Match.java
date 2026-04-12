package com.example.suraagh_deliverable_1.ModelClasses;

public class Match {
    String matchId;
    String ownerId;
    String finderId;
    float percentageMatch;
    String finderPostId;
    String lostPostId;

    public Match()
    {

    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFinderId() {
        return finderId;
    }

    public void setFinderId(String finderId) {
        this.finderId = finderId;
    }

    public float getPercentageMatch() {
        return percentageMatch;
    }

    public void setPercentageMatch(float percentageMatch) {
        this.percentageMatch = percentageMatch;
    }

    public String getFinderPostId() {
        return finderPostId;
    }

    public void setFinderPostId(String finderPostId) {
        this.finderPostId = finderPostId;
    }

    public String getLostPostId() {
        return lostPostId;
    }

    public void setLostPostId(String lostPostId) {
        this.lostPostId = lostPostId;
    }

    public Match(String matchId, String ownerId, String finderId, float percentageMatch, String finderPostId, String lostPostId) {
        this.matchId = matchId;
        this.ownerId = ownerId;
        this.finderId = finderId;
        this.percentageMatch = percentageMatch;
        this.finderPostId = finderPostId;
        this.lostPostId = lostPostId;
    }
}
