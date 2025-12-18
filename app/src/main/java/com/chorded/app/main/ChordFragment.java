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

import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Chord; // Импортируем обновленную модель
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

    private String chordId;

    // UI
    private ImageView chordImage;
    private TextView tvChordTitle;
    private Button btnLearnChord;
    private RecyclerView recyclerSongs;

    // Data
    private final List<Song> chordSongs = new ArrayList<>();
    private SongAdapter adapter;

    // State
    private boolean isLearned = false;

    // Services
    private FirebaseFirestore db;
    private String uid;
    private GuestStorage guestStorage;

    public static ChordFragment newInstance(String chordId) {
        ChordFragment fragment = new ChordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHORD_ID, chordId);
        fragment.setArguments(args);
        return fragment;
    }

    public ChordFragment() {}

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
        adapter = new SongAdapter(chordSongs, song ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, SongFragment.newInstance(song.getId()))
                        .addToBackStack(null)
                        .commit()
        );
        recyclerSongs.setAdapter(adapter);

        bindChord();      // Здесь мы только ставим заголовок
        loadChordInfo();  // <--- ДОБАВИЛИ: Загружаем картинку аккорда
        loadLearnState();
        setupLearnButton();
        loadSongsWithChord();

        return view;
    }

    // -------------------------
    // CHORD INFO
    // -------------------------

    private void bindChord() {
        if (chordId == null) return;
        tvChordTitle.setText(chordId);
        // Сразу ставим заглушку, пока грузится реальная картинка
        chordImage.setImageResource(R.drawable.chord_placeholder);
    }

    /**
     * Загружаем данные самого аккорда (чтобы получить img_src)
     */
    private void loadChordInfo() {
        if (chordId == null) return;

        db.collection("chords").document(chordId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Chord chord = documentSnapshot.toObject(Chord.class);
                    if (chord != null) {
                        // ЛОГИКА ДЛЯ ЭКРАНА АККОРДА (пункт 3): берем img_src
                        String rawPath = chord.getImg_src();

                        if (rawPath != null && !rawPath.isEmpty()) {
                            // Отрезаем папку "chords/", если она есть
                            String resName = rawPath.contains("/")
                                    ? rawPath.substring(rawPath.lastIndexOf("/") + 1)
                                    : rawPath;

                            // Получаем идентификатор ресурса
                            if (getContext() != null) {
                                int resId = getResources().getIdentifier(resName, "drawable", requireContext().getPackageName());
                                if (resId != 0) {
                                    chordImage.setImageResource(resId);
                                }
                            }
                        }
                    }
                });
    }

    // -------------------------
    // LEARN / UNLEARN (TOGGLE)
    // -------------------------

    // ... (остальной код без изменений: loadLearnState, setupLearnButton, updateLearnButton, loadSongsWithChord) ...

    private void loadLearnState() {
        if (chordId == null) return;

        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            isLearned = guestStorage.isChordLearned(chordId);
            updateLearnButton();
            return;
        }

        // 👤 USER
        if (uid == null) {
            btnLearnChord.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedChords");
                    isLearned = learned != null && learned.contains(chordId);
                    updateLearnButton();
                });
    }

    private void setupLearnButton() {
        btnLearnChord.setOnClickListener(v -> {

            // 👻 GUEST
            if (AppSession.get().isGuest()) {
                if (isLearned) {
                    guestStorage.removeChord(chordId);
                } else {
                    guestStorage.addChord(chordId);
                }
                isLearned = !isLearned;
                updateLearnButton();
                return;
            }

            // 👤 USER
            if (uid == null) return;

            if (isLearned) {
                db.collection("users").document(uid)
                        .update("learnedChords",
                                FieldValue.arrayRemove(chordId))
                        .addOnSuccessListener(x -> {
                            isLearned = false;
                            updateLearnButton();
                        });
            } else {
                db.collection("users").document(uid)
                        .update("learnedChords",
                                FieldValue.arrayUnion(chordId))
                        .addOnSuccessListener(x -> {
                            isLearned = true;
                            updateLearnButton();
                        });
            }
        });
    }

    private void updateLearnButton() {
        if (isLearned) {
            btnLearnChord.setText("Удалить из изученных");
        } else {
            btnLearnChord.setText("Выучить аккорд");
        }
        btnLearnChord.setEnabled(true);
        btnLearnChord.setVisibility(View.VISIBLE);
    }

    // -------------------------
    // SONGS WITH CHORD
    // -------------------------

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
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
    }
}