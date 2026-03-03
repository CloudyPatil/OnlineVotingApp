package com.example.myapplication;

public class Candidate {
    public String name;
    public String description;
    public int voteCount;

    public Candidate() {
    }

    public Candidate(String name, String description, int voteCount) {
        this.name = name;
        this.description = description;
        this.voteCount = voteCount;
    }
}