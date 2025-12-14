package com.chorded.app.models;

import java.util.List;

/**
 * Модель песни — соответствует документу в коллекции songs
 */
public class Song {

    private String id;              // document id
    private String title;
    private String artist;
    private List<String> chords;    // список аккордов (строки, совпадают с id аккордов)
    private int difficulty;
    private List<String> genres;
    private String lyricsChordPro;  // текст песни в ChordPro формате (строка)
    private String iconUrl;
    private String mp3Url;
    private int matchScore = 0;
    public String getMp3Url() {
        return mp3Url;
    }// ссылка на Storage / публичная ссылка

    // Временное поле в рантайме — для сортировки/фильтра


    public Song() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public List<String> getChords() { return chords; }
    public void setChords(List<String> chords) { this.chords = chords; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getLyricsChordPro() { return lyricsChordPro; }
    public void setLyricsChordPro(String lyricsChordPro) { this.lyricsChordPro = lyricsChordPro; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
}
