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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Chord;
import com.chorded.app.models.Song;
import com.chorded.app.session.AppSession;
import com.chorded.app.session.GuestStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChordFragment extends Fragment {

    private static final String ARG_CHORD_ID = "chord_id";

    private ImageView chordImage;
    private TextView tvChordTitle;

    private Button btnLearnChord;

    private RecyclerView recyclerSongs;
    private SongAdapter adapter;
    private final List<Song> chordSongs = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    private GuestStorage guestStorage;

    private String chordId;

    public static ChordFragment newInstance(String chordId) {
        ChordFragment fragment = new ChordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHORD_ID, chordId);
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
            chordId = getArguments().getString(ARG_CHORD_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_chord_info, container, false);

        chordImage = view.findViewById(R.id.chordImage);
        tvChordTitle = view.findViewById(R.id.chordTitle);

        btnLearnChord = view.findViewById(R.id.btnLearnChord);

        recyclerSongs = view.findViewById(R.id.chordSongsRecycler);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(chordSongs, song -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, SongFragment.newInstance(song.getId()))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerSongs.setAdapter(adapter);

        bindChord();
        setupLearnButton();
        loadSongsWithChord();

        return view;
    }

    private void bindChord() {
        if (chordId == null) return;

        tvChordTitle.setText(chordId);

        // Если у тебя есть картинка аккорда в модели/хранилище — подставь сюда URL.
        // Сейчас оставляю дефолтно: если chordId совпадает с именем файла/ресурса — ты можешь заменить.
        // Пример: Glide.with(this).load(chord.getImageUrl()).into(chordImage);

        // Заглушка (если есть дефолтная картинка):
        // chordImage.setImageResource(R.drawable.ic_chord_placeholder);
    }

    private void setupLearnButton() {
        if (chordId == null) return;

        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            boolean learned = guestStorage.isChordLearned(chordId);
            toggleLearnButton(learned);

            btnLearnChord.setOnClickListener(v -> {
                guestStorage.addChord(chordId);
                toggleLearnButton(true);
            });
            return;
        }

        // 👤 USER
        if (uid == null) {
            // если вдруг uid нет — просто скрываем кнопку
            btnLearnChord.setVisibility(View.GONE);
            return;
        }

        // Проверяем в Firestore: learnedChords содержит chordId?
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedChords");
                    boolean isLearned = learned != null && learned.contains(chordId);

                    toggleLearnButton(isLearned);

                    btnLearnChord.setOnClickListener(v -> {
                        if (isLearned) return;

                        db.collection("users").document(uid)
                                .update("learnedChords", FieldValue.arrayUnion(chordId))
                                .addOnSuccessListener(x -> toggleLearnButton(true));
                    });
                });
    }

    private void toggleLearnButton(boolean learned) {
        if (learned) {
            btnLearnChord.setEnabled(false);
            btnLearnChord.setText("Аккорд выучен");
        } else {
            btnLearnChord.setEnabled(true);
            btnLearnChord.setText("Выучить аккорд");
        }
        btnLearnChord.setVisibility(View.VISIBLE);
    }

    private void loadSongsWithChord() {
        if (chordId == null) return;

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
