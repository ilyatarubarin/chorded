package com.chorded.app.main;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.adapters.ChordGridAdapter;
import com.chorded.app.models.Chord;
import com.chorded.app.models.Song;
import com.chorded.app.session.AppSession;
import com.chorded.app.session.GuestStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SongFragment extends Fragment {

    private static final String ARG_SONG_ID = "song_id";

    private String songId;

    // UI
    private ImageView songIcon;
    private TextView tvTitle, tvArtist, tvLyrics;
    private Button btnLearn, btnUnlearn, btnPlay, btnPause;
    private RecyclerView rvSongChords;

    // Firebase
    private FirebaseFirestore db;
    private String uid;

    // Guest
    private GuestStorage guestStorage;

    // Data
    private Song currentSong;
    private final List<Chord> songChords = new ArrayList<>();
    private ChordGridAdapter chordAdapter;

    // Audio
    private MediaPlayer mediaPlayer;

    public static SongFragment newInstance(String songId) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SONG_ID, songId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        guestStorage = new GuestStorage(requireContext());

        if (getArguments() != null) {
            songId = getArguments().getString(ARG_SONG_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);

        songIcon = view.findViewById(R.id.songIcon);
        tvTitle = view.findViewById(R.id.tvSongTitle);
        tvArtist = view.findViewById(R.id.tvSongArtist);
        tvLyrics = view.findViewById(R.id.tvSongLyrics);
        rvSongChords = view.findViewById(R.id.rvSongChords);

        btnLearn = view.findViewById(R.id.btnLearnSong);
        btnUnlearn = view.findViewById(R.id.btnUnlearnSong);
        btnPlay = view.findViewById(R.id.btnPlaySong);
        btnPause = view.findViewById(R.id.btnPauseSong);

        setupChordsRecycler();
        loadSong();
        checkLearned();

        btnLearn.setOnClickListener(v -> addToLearned());
        btnUnlearn.setOnClickListener(v -> removeFromLearned());

        btnPlay.setOnClickListener(v -> {
            if (currentSong != null && currentSong.getMp3Url() != null) {
                playSong(currentSong.getMp3Url());
            }
        });

        btnPause.setOnClickListener(v -> pauseSong());

        return view;
    }

    // ----------------------------
    // CHORDS
    // ----------------------------

    private void setupChordsRecycler() {
        rvSongChords.setLayoutManager(new GridLayoutManager(getContext(), 2));

        chordAdapter = new ChordGridAdapter(
                songChords,
                chord -> requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragmentContainer,
                                ChordFragment.newInstance(chord.getId())
                        )
                        .addToBackStack(null)
                        .commit()
        );

        rvSongChords.setAdapter(chordAdapter);
    }

    private void loadSongChords(Song song) {
        List<String> chordIds = song.getChords();
        if (chordIds == null || chordIds.isEmpty()) return;

        db.collection("chords")
                .whereIn(FieldPath.documentId(), chordIds)
                .get()
                .addOnSuccessListener(query -> {
                    songChords.clear();
                    for (var doc : query) {
                        Chord chord = doc.toObject(Chord.class);
                        if (chord != null) {
                            chord.setId(doc.getId());
                            songChords.add(chord);
                        }
                    }
                    chordAdapter.notifyDataSetChanged();
                });
    }

    // ----------------------------
    // LOAD SONG
    // ----------------------------

    private void loadSong() {
        db.collection("songs").document(songId)
                .get()
                .addOnSuccessListener(doc -> {
                    currentSong = doc.toObject(Song.class);
                    if (currentSong == null) return;

                    currentSong.setId(doc.getId());

                    tvTitle.setText(currentSong.getTitle());
                    tvArtist.setText(currentSong.getArtist());
                    tvLyrics.setText(currentSong.getLyricsChordPro());

                    Glide.with(this)
                            .load(currentSong.getIconUrl())
                            .placeholder(R.drawable.song_placeholder)
                            .into(songIcon);

                    loadSongChords(currentSong);
                });
    }

    // ----------------------------
    // LEARNED SONGS
    // ----------------------------

    private void toggleState(boolean learned, boolean learning) {
        if (learned) {
            btnLearn.setVisibility(View.GONE);
            btnUnlearn.setVisibility(View.GONE);
        } else if (learning) {
            btnLearn.setVisibility(View.GONE);
            btnUnlearn.setVisibility(View.VISIBLE);
            btnUnlearn.setText("Подтвердить выучена");
        } else {
            btnLearn.setVisibility(View.VISIBLE);
            btnUnlearn.setVisibility(View.GONE);
        }
    }

    private void checkLearned() {
        if (AppSession.get().isGuest()) {
            toggleState(
                    guestStorage.isLearned(songId),
                    guestStorage.isLearningSong(songId)
            );
            return;
        }

        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedSongs");
                    List<String> learning = (List<String>) doc.get("learningSongs");

                    toggleState(
                            learned != null && learned.contains(songId),
                            learning != null && learning.contains(songId)
                    );
                });
    }

    private void addToLearned() {
        if (AppSession.get().isGuest()) {
            guestStorage.addLearningSong(songId);
            toggleState(false, true);
            return;
        }

        if (uid == null) return;

        db.collection("users").document(uid)
                .update("learningSongs",
                        com.google.firebase.firestore.FieldValue.arrayUnion(songId))
                .addOnSuccessListener(v -> toggleState(false, true));
    }

    private void removeFromLearned() {
        if (AppSession.get().isGuest()) {
            guestStorage.removeLearningSong(songId);
            guestStorage.addSong(songId);
            toggleState(true, false);
            return;
        }

        if (uid == null) return;

        db.collection("users").document(uid)
                .update(
                        "learningSongs",
                        com.google.firebase.firestore.FieldValue.arrayRemove(songId)
                );

        db.collection("users").document(uid)
                .update(
                        "learnedSongs",
                        com.google.firebase.firestore.FieldValue.arrayUnion(songId)
                )
                .addOnSuccessListener(v -> toggleState(true, false));
    }

    // ----------------------------
    // AUDIO
    // ----------------------------

    private void playSong(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
