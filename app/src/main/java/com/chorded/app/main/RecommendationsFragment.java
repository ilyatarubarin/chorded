package com.chorded.app.main;
import com.chorded.app.recommendations.SongCard;
import com.chorded.app.recommendations.SongCardBuilder;
import android.util.Log;
import java.util.Set;
import com.chorded.app.adapters.SongCardAdapter;

import com.chorded.app.recommendations.SongCard;
import com.chorded.app.recommendations.SongCardBuilder;
import com.chorded.app.recommendations.SongStatus;

import java.util.Set;
import java.util.HashSet;

import com.chorded.app.session.GuestStorage;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Song;
import com.chorded.app.utils.SimpleTextWatcher;
import com.chorded.app.session.AppSession;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment {

    private EditText input;
    private RecyclerView recycler;
    private SongCardAdapter adapter;
    private Set<String> learnedChordCache = new HashSet<>();

    private final List<Song> allSongs = new ArrayList<>();
    private final List<SongCard> cards = new ArrayList<>();

    private FirebaseFirestore db;

    public RecommendationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_recommendations, container, false);

        input = v.findViewById(R.id.inputChords);
        recycler = v.findViewById(R.id.recyclerRecommendations);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SongCardAdapter(
                cards,
                card -> openSong(card.getSong().getId())
        );
        recycler.setAdapter(adapter);

        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadLearnedChords();


        loadAllSongs();

        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return v;
    }

    // ----------------------------------
    // LOAD SONGS
    // ----------------------------------

    private void loadAllSongs() {
        db.collection("songs")
                .get()
                .addOnSuccessListener(query -> {
                    allSongs.clear();

                    for (var doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song == null) continue;

                        song.setId(doc.getId());
                        allSongs.add(song);
                    }

                    // для guest и user логика одинаковая
                    filter(input.getText().toString());
                })
                .addOnFailureListener(e -> {
                    // полезно для отладки
                    e.printStackTrace();
                });
    }

    // ----------------------------------
    // FILTER
    // ----------------------------------

    private void filter(String text) {
        cards.clear();

        // ---------- РЕЖИМ РЕКОМЕНДАЦИЙ ----------
        if (text.trim().isEmpty()) {

            Set<String> learned = getLearnedChordSet();

            for (Song song : allSongs) {
                SongCard card =
                        SongCardBuilder.buildForRecommendation(song, learned);

                if (card == null) continue;

                // ❌ изученные песни пока не показываем
                if (card.getStatus() == SongStatus.LEARNED) continue;

                cards.add(card);
            }

            cards.sort((a, b) ->
                    Integer.compare(b.getMatchScore(), a.getMatchScore())
            );

            adapter.notifyDataSetChanged();
            return;
        }

        // ---------- РЕЖИМ ПОИСКА ----------
        List<String> queryChords = parseChords(text);

        for (Song song : allSongs) {
            SongCard card =
                    SongCardBuilder.buildForSearch(song, queryChords);

            if (card != null) {
                cards.add(card);
            }
        }

        cards.sort((a, b) ->
                Integer.compare(b.getMatchScore(), a.getMatchScore())
        );

        adapter.notifyDataSetChanged();
    }


    // ----------------------------------
    // NAVIGATION
    // ----------------------------------

    private void openSong(String songId) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SongFragment.newInstance(songId))
                .addToBackStack(null)
                .commit();
    }
    private List<String> getLearnedChords() {
        if (AppSession.get().isGuest()) {
            return new ArrayList<>(
                    new GuestStorage(requireContext()).getLearnedChords()
            );
        }
        return new ArrayList<>();
    }

    private List<String> parseChords(String text) {
        List<String> result = new ArrayList<>();

        String[] parts = text
                .replace(",", " ")
                .trim()
                .split("\\s+");

        for (String p : parts) {
            if (!p.isEmpty()) result.add(normalizeChord(p));

        }

        return result;
    }


    private Set<String> getLearnedChordSet() {
        return new HashSet<>(learnedChordCache);
    }

    private String normalizeChord(String chord) {
        if (chord.length() == 0) return chord;
        return chord.substring(0, 1).toUpperCase() + chord.substring(1);
    }

    private void loadLearnedChords() {

        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            learnedChordCache.clear();
            learnedChordCache.addAll(
                    new GuestStorage(requireContext()).getLearnedChords()
            );
            return;
        }

        // 👤 USER
        String uid = AppSession.get().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> chords =
                            (List<String>) doc.get("learnedChords");

                    learnedChordCache.clear();

                    if (chords != null) {
                        learnedChordCache.addAll(chords);
                    }

                    // 🔄 пересчитываем рекомендации
                    filter(input.getText().toString());
                });
    }






}
