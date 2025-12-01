package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Chord;
import com.chorded.app.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChordFragment extends Fragment {

    private static final String ARG_CHORD_ID = "chord_id";

    private String chordId;

    private ImageView chordImage;
    private TextView chordTitle, chordDescription;
    private RecyclerView recyclerSongs;

    private SongAdapter adapter;
    private final List<Song> chordSongs = new ArrayList<>();
    private FirebaseFirestore db;

    public static ChordFragment newInstance(String chordId) {
        ChordFragment fragment = new ChordFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CHORD_ID, chordId);
        fragment.setArguments(b);
        return fragment;
    }

    public ChordFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chordId = getArguments() != null ? getArguments().getString(ARG_CHORD_ID) : null;
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chord_info, container, false);

        chordImage = view.findViewById(R.id.chordImage);
        chordTitle = view.findViewById(R.id.chordTitle);
        chordDescription = view.findViewById(R.id.chordDescription);

        recyclerSongs = view.findViewById(R.id.chordSongsRecycler);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(chordSongs, song -> {});
        recyclerSongs.setAdapter(adapter);

        loadChordInfo();
        loadSongsWithChord();

        return view;
    }

    private void loadChordInfo() {
        db.collection("chords").document(chordId)
                .get()
                .addOnSuccessListener(doc -> {
                    Chord c = doc.toObject(Chord.class);
                    if (c == null) return;

                    chordTitle.setText(c.getName());
                    chordDescription.setText(c.getDescription());

                    Glide.with(this)
                            .load(c.getImageUrl())
                            .placeholder(R.drawable.chord_placeholder)
                            .into(chordImage);
                });
    }

    private void loadSongsWithChord() {
        db.collection("songs")
                .whereArrayContains("chords", chordId)
                .get()
                .addOnSuccessListener(query -> {
                    chordSongs.clear();
                    for (var doc : query) {
                        Song s = doc.toObject(Song.class);
                        s.setId(doc.getId());
                        chordSongs.add(s);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
