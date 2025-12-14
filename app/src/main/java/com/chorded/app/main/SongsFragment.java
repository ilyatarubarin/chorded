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

public class SongsFragment extends Fragment {

    private RecyclerView recycler;
    private EditText inputSearch;                // 🔹 НОВОЕ
    private SongAdapter adapter;

    private final List<Song> allSongs = new ArrayList<>();     // 🔹 НОВОЕ
    private final List<Song> filteredSongs = new ArrayList<>();// 🔹 НОВОЕ

    private FirebaseFirestore db;

    public SongsFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_songs, container, false);

        recycler = v.findViewById(R.id.recyclerSongs);
        inputSearch = v.findViewById(R.id.inputSearch); // 🔹 НОВОЕ

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(filteredSongs, song -> openSong(song.getId()));
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadSongs();

        // 🔹 НОВОЕ — поиск
        inputSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return v;
    }

    // ----------------------------
    // LOAD SONGS
    // ----------------------------

    private void loadSongs() {
        db.collection("songs")
                .get()
                .addOnSuccessListener(query -> {
                    allSongs.clear();
                    filteredSongs.clear();

                    for (var doc : query) {
                        Song s = doc.toObject(Song.class);
                        if (s == null) continue;

                        s.setId(doc.getId());
                        allSongs.add(s);
                    }

                    // по умолчанию показываем все
                    filteredSongs.addAll(allSongs);
                    adapter.notifyDataSetChanged();
                });
    }

    // ----------------------------
    // FILTER
    // ----------------------------

    private void filter(String text) {
        filteredSongs.clear();

        if (text.trim().isEmpty()) {
            filteredSongs.addAll(allSongs);
            adapter.notifyDataSetChanged();
            return;
        }

        String q = text.toLowerCase().trim();

        for (Song song : allSongs) {

            boolean match =
                    contains(song.getTitle(), q)
                            || contains(song.getArtist(), q)
                            || containsInList(song.getGenres(), q); // 🔹 ВАЖНО

            if (match) {
                filteredSongs.add(song);
            }
        }

        adapter.notifyDataSetChanged();
    }
    private boolean containsInList(List<String> list, String query) {
        if (list == null) return false;

        for (String item : list) {
            if (item != null && item.toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }


    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase().contains(query);
    }

    // ----------------------------
    // NAVIGATION
    // ----------------------------

    private void openSong(String songId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SongFragment.newInstance(songId))
                .addToBackStack(null)
                .commit();
    }
}
