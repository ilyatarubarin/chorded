package com.chorded.app.recommendations;

import com.chorded.app.models.Song;

import java.util.List;

public class SongCard {

    private final Song song;

    private final SongStatus status;

    private final List<String> missingChords; // null или пусто для SEARCH
    private final int matchScore;              // для сортировки
    private final String summary;               // текст под песней

    public SongCard(
            Song song,
            SongStatus status,
            List<String> missingChords,
            int matchScore,
            String summary
    ) {
        this.song = song;
        this.status = status;
        this.missingChords = missingChords;
        this.matchScore = matchScore;
        this.summary = summary;
    }

    // ---------- GETTERS ----------

    public Song getSong() {
        return song;
    }

    public SongStatus getStatus() {
        return status;
    }

    public List<String> getMissingChords() {
        return missingChords;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public String getSummary() {
        return summary;
    }
}
