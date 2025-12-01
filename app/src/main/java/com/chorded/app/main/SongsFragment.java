package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment {

    private RecyclerView recycler;
    private SongAdapter adapter;
    private final List<Song> songs = new ArrayList<>();

    private FirebaseFirestore db;

    public SongsFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_songs, container, false);

        recycler = v.findViewById(R.id.recyclerSongs);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SongAdapter(songs, song -> openSong(song.getId()));
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadSongs();

        return v;
    }

    private void loadSongs() {
        db.collection("songs")
                .get()
                .addOnSuccessListener(query -> {
                    songs.clear();
                    for (var doc : query) {
                        Song s = doc.toObject(Song.class);
                        s.setId(doc.getId());
                        songs.add(s);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void openSong(String songId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SongFragment.newInstance(songId))
                .addToBackStack(null)
                .commit();
    }

}
