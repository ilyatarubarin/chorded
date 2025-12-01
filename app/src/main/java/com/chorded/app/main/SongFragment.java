package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SongFragment extends Fragment {

    private static final String ARG_SONG_ID = "song_id";

    private String songId;

    private ImageView songIcon;
    private TextView tvTitle, tvArtist, tvChords, tvLyrics;
    private Button btnLearn, btnUnlearn;

    private FirebaseFirestore db;
    private String uid;

    private Song currentSong;

    public static SongFragment newInstance(String songId) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SONG_ID, songId);
        fragment.setArguments(args);
        return fragment;
    }

    public SongFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (getArguments() != null) {
            songId = getArguments().getString(ARG_SONG_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_song, container, false);

        songIcon = view.findViewById(R.id.songIcon);
        tvTitle = view.findViewById(R.id.tvSongTitle);
        tvArtist = view.findViewById(R.id.tvSongArtist);
        tvChords = view.findViewById(R.id.tvSongChords);
        tvLyrics = view.findViewById(R.id.tvSongLyrics);

        btnLearn = view.findViewById(R.id.btnLearnSong);
        btnUnlearn = view.findViewById(R.id.btnUnlearnSong);

        loadSong();
        checkIfLearned();

        btnLearn.setOnClickListener(v -> addToLearned());
        btnUnlearn.setOnClickListener(v -> removeFromLearned());

        return view;
    }

    private void loadSong() {
        db.collection("songs").document(songId)
                .get()
                .addOnSuccessListener(doc -> {
                    currentSong = doc.toObject(Song.class);
                    if (currentSong == null) return;

                    currentSong.setId(doc.getId());

                    tvTitle.setText(currentSong.getTitle());
                    tvArtist.setText(currentSong.getArtist());
                    tvChords.setText("Аккорды: " + String.join(", ", currentSong.getChords()));
                    tvLyrics.setText(currentSong.getLyricsChordPro());

                    Glide.with(this)
                            .load(currentSong.getIconUrl())
                            .placeholder(R.drawable.song_placeholder)
                            .into(songIcon);
                });
    }

    private void checkIfLearned() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedSongs");
                    if (learned != null && learned.contains(songId)) {
                        btnLearn.setVisibility(View.GONE);
                        btnUnlearn.setVisibility(View.VISIBLE);
                    } else {
                        btnLearn.setVisibility(View.VISIBLE);
                        btnUnlearn.setVisibility(View.GONE);
                    }
                });
    }

    private void addToLearned() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .update("learnedSongs", com.google.firebase.firestore.FieldValue.arrayUnion(songId))
                .addOnSuccessListener(v -> {
                    btnLearn.setVisibility(View.GONE);
                    btnUnlearn.setVisibility(View.VISIBLE);
                });
    }

    private void removeFromLearned() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .update("learnedSongs", com.google.firebase.firestore.FieldValue.arrayRemove(songId))
                .addOnSuccessListener(v -> {
                    btnLearn.setVisibility(View.VISIBLE);
                    btnUnlearn.setVisibility(View.GONE);
                });
    }
}
