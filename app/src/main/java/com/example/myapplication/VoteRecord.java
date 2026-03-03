package com.example.myapplication;

public class VoteRecord {
    public String username;
    public String candidateName;
    public String branch;
    public String events;
    public long timestamp;

    public VoteRecord() {
    }

    public VoteRecord(String username, String candidateName,
                      String branch, String events, long timestamp) {
        this.username = username;
        this.candidateName = candidateName;
        this.branch = branch;
        this.events = events;
        this.timestamp = timestamp;
    }
}