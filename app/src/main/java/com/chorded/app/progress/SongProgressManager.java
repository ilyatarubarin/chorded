package com.chorded.app.progress;

import android.content.Context;

import com.chorded.app.session.AppSession;
import com.chorded.app.session.GuestStorage;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SongProgressManager {

    private static SongProgressManager instance;

    private FirebaseFirestore db;
    private GuestStorage guestStorage;

    private SongProgressManager(Context context) {
        db = FirebaseFirestore.getInstance();
        guestStorage = new GuestStorage(context.getApplicationContext());
    }

    public static SongProgressManager get(Context context) {
        if (instance == null) {
            instance = new SongProgressManager(context);
        }
        return instance;
    }

    // =========================
    // STATUS
    // =========================

    public SongStatus getStatus(String songId, List<String> learned, List<String> learning) {
        if (learned != null && learned.contains(songId)) {
            return SongStatus.LEARNED;
        }
        if (learning != null && learning.contains(songId)) {
            return SongStatus.LEARNING;
        }
        return SongStatus.NONE;
    }

    public SongStatus getGuestStatus(String songId) {
        if (guestStorage.getLearnedSongs().contains(songId)) {
            return SongStatus.LEARNED;
        }
        if (guestStorage.getLearningSongs().contains(songId)) {
            return SongStatus.LEARNING;
        }
        return SongStatus.NONE;
    }

    // =========================
    // ACTIONS — GUEST
    // =========================

    public void guestStartLearning(String songId) {
        guestStorage.removeSong(songId);
        guestStorage.addLearningSong(songId);
    }

    public void guestMarkLearned(String songId) {
        guestStorage.removeLearningSong(songId);
        guestStorage.addSong(songId);
    }

    public void guestClear(String songId) {
        guestStorage.removeLearningSong(songId);
        guestStorage.removeSong(songId);
    }

    // =========================
    // ACTIONS — USER
    // =========================

    public void userStartLearning(String uid, String songId) {
        db.collection("users").document(uid)
                .update(
                        "learningSongs", FieldValue.arrayUnion(songId),
                        "learnedSongs", FieldValue.arrayRemove(songId)
                );
    }

    public void userMarkLearned(String uid, String songId) {
        db.collection("users").document(uid)
                .update(
                        "learningSongs", FieldValue.arrayRemove(songId),
                        "learnedSongs", FieldValue.arrayUnion(songId)
                );
    }

    public void userClear(String uid, String songId) {
        db.collection("users").document(uid)
                .update(
                        "learningSongs", FieldValue.arrayRemove(songId),
                        "learnedSongs", FieldValue.arrayRemove(songId)
                );
    }
}
