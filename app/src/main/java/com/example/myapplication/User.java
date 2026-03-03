package com.example.myapplication;

public class User {
    public String fullName;
    public String password;
    public boolean hasVoted;
    public String votedFor;
    public boolean isAdmin;

    // Empty constructor required for Firebase
    public User() {
    }

    public User(String fullName, String password, boolean hasVoted,
                String votedFor, boolean isAdmin) {
        this.fullName = fullName;
        this.password = password;
        this.hasVoted = hasVoted;
        this.votedFor = votedFor;
        this.isAdmin = isAdmin;
    }
}