package com.chorded.app.session;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class GuestStorage {

    private static final String PREFS = "guest_storage";

    private static final String KEY_LEARNED_SONGS = "learned_songs";
    private static final String KEY_LEARNED_CHORDS = "learned_chords";

    private final SharedPreferences prefs;

    public GuestStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ==========================
    // SONGS
    // ==========================

    public Set<String> getLearnedSongs() {
        Set<String> set = prefs.getStringSet(KEY_LEARNED_SONGS, null);
        return set == null ? new HashSet<>() : new HashSet<>(set);
    }

    public void addSong(String songId) {
        Set<String> songs = getLearnedSongs();
        songs.add(songId);
        prefs.edit().putStringSet(KEY_LEARNED_SONGS, songs).apply();
    }

    public void removeSong(String songId) {
        Set<String> songs = getLearnedSongs();
        songs.remove(songId);
        prefs.edit().putStringSet(KEY_LEARNED_SONGS, songs).apply();
    }

    public boolean isLearned(String songId) {
        return getLearnedSongs().contains(songId);
    }

    // ==========================
    // CHORDS
    // ==========================

    public Set<String> getLearnedChords() {
        Set<String> set = prefs.getStringSet(KEY_LEARNED_CHORDS, null);
        return set == null ? new HashSet<>() : new HashSet<>(set);
    }

    public void addChord(String chordId) {
        Set<String> chords = getLearnedChords();
        chords.add(chordId);
        prefs.edit().putStringSet(KEY_LEARNED_CHORDS, chords).apply();
    }

    public void removeChord(String chordId) {
        Set<String> chords = getLearnedChords();
        chords.remove(chordId);
        prefs.edit().putStringSet(KEY_LEARNED_CHORDS, chords).apply();
    }

    public boolean isChordLearned(String chordId) {
        return getLearnedChords().contains(chordId);
    }

    // ==========================
    // CLEAR
    // ==========================

    public void clear() {
        prefs.edit().clear().apply();
    }
}
