package com.chorded.app.models;

import java.util.List;

public class User {

    private String uid;
    private String name;
    private String email;

    // Роль пользователя: "user" | "creator"
    private String role;

    // Аккорды
    private List<String> learnedChords;
    private List<String> learningChords;

    // Песни
    private List<String> learnedSongs;
    private List<String> learningSongs;

    // Прогресс
    private int totalPracticeTime;
    private int level;

    // 🔹 ПУСТОЙ КОНСТРУКТОР ОБЯЗАТЕЛЕН ДЛЯ FIRESTORE
    public User() {}

    // 🔹 Конструктор при регистрации пользователя
    public User(String uid, String name, String email, List<String> learnedSongs) {
        this.uid = uid;
        this.name = name;
        this.email = email;

        this.role = "user"; // по умолчанию обычный пользователь

        this.learnedSongs = learnedSongs;

        // Остальные поля — дефолтные
        this.learningSongs = null;
        this.learnedChords = null;
        this.learningChords = null;

        this.totalPracticeTime = 0;
        this.level = 1;
    }

    // ===== GETTERS & SETTERS =====

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // 🔹 ROLE
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // 🔹 SONGS
    public List<String> getLearnedSongs() { return learnedSongs; }
    public void setLearnedSongs(List<String> learnedSongs) { this.learnedSongs = learnedSongs; }

    public List<String> getLearningSongs() { return learningSongs; }
    public void setLearningSongs(List<String> learningSongs) { this.learningSongs = learningSongs; }

    // 🔹 CHORDS
    public List<String> getLearnedChords() { return learnedChords; }
    public void setLearnedChords(List<String> learnedChords) { this.learnedChords = learnedChords; }

    public List<String> getLearningChords() { return learningChords; }
    public void setLearningChords(List<String> learningChords) { this.learningChords = learningChords; }

    // 🔹 PROGRESS
    public int getTotalPracticeTime() { return totalPracticeTime; }
    public void setTotalPracticeTime(int totalPracticeTime) { this.totalPracticeTime = totalPracticeTime; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
