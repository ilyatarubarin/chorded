package com.chorded.app.main;


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
    private SongAdapter adapter;

    private final List<Song> allSongs = new ArrayList<>();
    private final List<Song> filteredSongs = new ArrayList<>();

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
        adapter = new SongAdapter(filteredSongs, song -> openSong(song.getId()));
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

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
        filteredSongs.clear();

// 🟢 НОВОЕ: если ввод пустой — рекомендации по выученным аккордам
        if (text.trim().isEmpty()) {

            List<String> learned = getLearnedChords();

            // если гость ещё ничего не выучил — показываем всё (как раньше)
            if (learned.isEmpty()) {
                filteredSongs.addAll(allSongs);
                adapter.notifyDataSetChanged();
                return;
            }

            for (Song song : allSongs) {
                if (song.getChords() == null) continue;

                for (String chord : song.getChords()) {
                    if (learned.contains(chord)) {
                        filteredSongs.add(song);
                        break;
                    }
                }
            }

            adapter.notifyDataSetChanged();
            return;
        }


        String[] entered = text
                .toUpperCase()
                .replace(",", " ")
                .trim()
                .split("\\s+");

        for (Song song : allSongs) {
            if (song.getChords() == null) continue;

            int score = 0;
            for (String chord : entered) {
                if (song.getChords().contains(chord)) {
                    score++;
                }
            }

            if (score > 0) {
                song.setMatchScore(score);
                filteredSongs.add(song);
            }
        }

        filteredSongs.sort((a, b) ->
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


}
