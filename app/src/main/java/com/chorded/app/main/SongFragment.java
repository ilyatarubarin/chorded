package com.chorded.app.main;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.models.Song;
import com.chorded.app.session.AppSession;
import com.chorded.app.session.GuestStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SongFragment extends Fragment {

    private static final String ARG_SONG_ID = "song_id";

    private String songId;

    // UI
    private ImageView songIcon;
    private TextView tvTitle, tvArtist, tvChords, tvLyrics;
    private Button btnLearn, btnUnlearn, btnPlay, btnPause;

    // Firebase
    private FirebaseFirestore db;
    private String uid;

    // Guest
    private GuestStorage guestStorage;

    // Data
    private Song currentSong;

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
        tvChords = view.findViewById(R.id.tvSongChords);
        tvLyrics = view.findViewById(R.id.tvSongLyrics);

        btnLearn = view.findViewById(R.id.btnLearnSong);
        btnUnlearn = view.findViewById(R.id.btnUnlearnSong);
        btnPlay = view.findViewById(R.id.btnPlaySong);
        btnPause = view.findViewById(R.id.btnPauseSong);

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
                    tvChords.setText("Аккорды: " + String.join(", ", currentSong.getChords()));
                    tvLyrics.setText(currentSong.getLyricsChordPro());

                    Glide.with(this)
                            .load(currentSong.getIconUrl())
                            .placeholder(R.drawable.song_placeholder)
                            .into(songIcon);
                });
    }

    // ----------------------------
    // LEARNED SONGS (USER + GUEST)
    // ----------------------------

    private void checkLearned() {
        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            toggle(guestStorage.isLearned(songId));
            return;
        }

        // 👤 USER
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedSongs");
                    toggle(learned != null && learned.contains(songId));
                });
    }

    private void addToLearned() {
        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            guestStorage.addSong(songId);
            toggle(true);
            return;
        }

        // 👤 USER
        if (uid == null) return;

        db.collection("users").document(uid)
                .update("learnedSongs",
                        com.google.firebase.firestore.FieldValue.arrayUnion(songId))
                .addOnSuccessListener(v -> toggle(true));
    }

    private void removeFromLearned() {
        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            guestStorage.removeSong(songId);
            toggle(false);
            return;
        }

        // 👤 USER
        if (uid == null) return;

        db.collection("users").document(uid)
                .update("learnedSongs",
                        com.google.firebase.firestore.FieldValue.arrayRemove(songId))
                .addOnSuccessListener(v -> toggle(false));
    }

    private void toggle(boolean learned) {
        btnLearn.setVisibility(learned ? View.GONE : View.VISIBLE);
        btnUnlearn.setVisibility(learned ? View.VISIBLE : View.GONE);
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
