package com.chorded.app.session;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class GuestStorage {

    private static final String PREFS = "guest_storage";
    private static final String KEY_LEARNED_SONGS = "learned_songs";

    private final SharedPreferences prefs;

    public GuestStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Set<String> getLearnedSongs() {
        return new HashSet<>(prefs.getStringSet(KEY_LEARNED_SONGS, new HashSet<>()));
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

    public void clear() {
        prefs.edit().clear().apply();
    }
}
