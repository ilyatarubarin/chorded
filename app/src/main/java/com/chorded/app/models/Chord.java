package com.chorded.app.models;

/**
 * Модель аккорда — документ в коллекции chords
 */
public class Chord {

    private String id;          // document id (например "C", "Am")
    private String name;        // читаемое имя
    private String imageUrl;    // ссылка на картинку (Storage) или локальный drawable имя (опция)
    private String description;
    private int difficulty;

    public Chord() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}
