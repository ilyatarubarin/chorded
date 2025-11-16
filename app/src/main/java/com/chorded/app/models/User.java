package com.chorded.app.models;

import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private List<String> learnedChords;

    public User() {} // Для Firestore

    public User(String id, String name, String email, List<String> learnedChords) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.learnedChords = learnedChords;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<String> getLearnedChords() { return learnedChords; }
}
