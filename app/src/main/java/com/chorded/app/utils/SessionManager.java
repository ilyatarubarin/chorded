package com.chorded.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static final String PREFS = "chorded_session";

    private static final String KEY_MODE = "mode";
    private static final String KEY_GUEST_LEARNED_SONGS = "guest_learned_songs";

    public static final String MODE_GUEST = "guest";
    public static final String MODE_AUTH = "auth";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // --------------------
    // MODE
    // --------------------

    public void setGuest() {
        prefs.edit()
                .putString(KEY_MODE, MODE_GUEST)
                .apply();
    }

    public void setAuth() {
        prefs.edit()
                .putString(KEY_MODE, MODE_AUTH)
                .apply();
    }

    public boolean isGuest() {
        return MODE_GUEST.equals(prefs.getString(KEY_MODE, null));
    }

    public boolean isAuth() {
        return MODE_AUTH.equals(prefs.getString(KEY_MODE, null));
    }

    public boolean hasSession() {
        return prefs.contains(KEY_MODE);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    // --------------------
    // GUEST LEARNED SONGS
    // --------------------

    public Set<String> getGuestLearnedSongs() {
        return new HashSet<>(
                prefs.getStringSet(KEY_GUEST_LEARNED_SONGS, new HashSet<>())
        );
    }

    public void addGuestLearnedSong(String songId) {
        Set<String> songs = getGuestLearnedSongs();
        songs.add(songId);
        prefs.edit().putStringSet(KEY_GUEST_LEARNED_SONGS, songs).apply();
    }

    public void removeGuestLearnedSong(String songId) {
        Set<String> songs = getGuestLearnedSongs();
        songs.remove(songId);
        prefs.edit().putStringSet(KEY_GUEST_LEARNED_SONGS, songs).apply();
    }

    public boolean isGuestSongLearned(String songId) {
        return getGuestLearnedSongs().contains(songId);
    }
}
