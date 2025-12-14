package com.chorded.app.main;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recommendations, container, false);

        input = v.findViewById(R.id.inputChords);
        recycler = v.findViewById(R.id.recyclerRecommendations);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private void loadAllSongs() {
        db.collection("songs")
                .get()
                .addOnSuccessListener(query -> {
                    allSongs.clear();
                    for (var doc : query) {
                        Song song = doc.toObject(Song.class);
                        song.setId(doc.getId());
                        allSongs.add(song);
                    }

                    filter(input.getText().toString());
                });
    }

    /**
     * Фильтрация: пользователь вводит аккорды, выдаём подходящие песни в порядке
     * количества совпадений (matchScore).
     */
    private void filter(String text) {
        filteredSongs.clear();

        // если строка пустая → выводим ВСЕ песни
        if (text.trim().isEmpty()) {
            filteredSongs.addAll(allSongs);
            adapter.notifyDataSetChanged();
            return;
        }

        String[] entered = text.toUpperCase().replace(",", " ").split(" +");

        for (Song song : allSongs) {
            int score = 0;

            if (song.getChords() != null) {
                for (String chord : entered) {
                    if (song.getChords().contains(chord)) {
                        score++;
                    }
                }
            }

            if (score > 0) {
                song.setMatchScore(score);
                filteredSongs.add(song);
            }
        }

        // сортировка по убыванию matchScore
        filteredSongs.sort((a, b) -> b.getMatchScore() - a.getMatchScore());

        adapter.notifyDataSetChanged();
    }
    private void openSong(String songId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SongFragment.newInstance(songId))
                .addToBackStack(null)
                .commit();
    }
}
