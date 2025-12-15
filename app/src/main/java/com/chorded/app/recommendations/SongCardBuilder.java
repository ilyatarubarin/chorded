package com.chorded.app.recommendations;

import com.chorded.app.models.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongCardBuilder {

    /**
     * Режим РЕКОМЕНДАЦИЙ (input пустой)
     */
    public static SongCard buildForRecommendation(
            Song song,
            Set<String> learnedChords
    ) {
        if (song.getChords() == null || song.getChords().isEmpty()) {
            return null;
        }

        int learned = 0;
        List<String> missing = new ArrayList<>();

        for (String chord : song.getChords()) {
            if (learnedChords.contains(chord)) {
                learned++;
            } else {
                missing.add(chord);
            }
        }

        if (learned == 0) {
            return new SongCard(
                    song,
                    SongStatus.NOT_STARTED,
                    missing,
                    0,
                    "0 из " + song.getChords().size() + " аккордов выучены"
            );
        }

        if (missing.isEmpty()) {
            return new SongCard(
                    song,
                    SongStatus.LEARNED,
                    List.of(),
                    learned,
                    "Песня выучена"
            );
        }

        String summary =
                "Не хватает: " + String.join(", ", missing) +
                        " (" + learned + "/" + song.getChords().size() + ")";

        return new SongCard(
                song,
                SongStatus.IN_PROGRESS,
                missing,
                learned,
                summary
        );
    }

    /**
     * Режим ПОИСКА (аккорды введены)
     */
    public static SongCard buildForSearch(
            Song song,
            List<String> queryChords
    ) {
        if (song.getChords() == null) return null;

        Set<String> songChords = new HashSet<>(song.getChords());

        int matches = 0;
        for (String q : queryChords) {
            if (songChords.contains(q)) {
                matches++;
            }
        }

        if (matches == 0) return null;

        String summary = "Совпало аккордов: " + matches;

        return new SongCard(
                song,
                SongStatus.SEARCH,
                null,
                matches,
                summary
        );
    }
}
