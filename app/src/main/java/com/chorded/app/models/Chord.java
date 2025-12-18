package com.chorded.app.models;

/**
 * Модель аккорда — документ в коллекции chords
 */
public class Chord {

    private String id;          // document id (например "C", "Am")
    private String name;        // читаемое имя
    // Новые поля согласно структуре в Firebase
    private String img_src;            // путь к "чистой" картинке (например "chords/Am_chord")
    private String img_src_with_name;  // путь к картинке с именем (например "chords/Am_chord_with_name")

    private String description;
    private int difficulty;

    public Chord() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImg_src() { return img_src; }
    public void setImg_src(String img_src) { this.img_src = img_src; }

    public String getImg_src_with_name() { return img_src_with_name; }
    public void setImg_src_with_name(String img_src_with_name) { this.img_src_with_name = img_src_with_name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}