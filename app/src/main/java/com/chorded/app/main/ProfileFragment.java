package com.chorded.app.main;
import java.util.HashSet;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chorded.app.recommendations.SongStatus;
import com.chorded.app.recommendations.SongCardBuilder;
import com.chorded.app.recommendations.SongCard;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.auth.LoginActivity;
import com.chorded.app.creator.CreateSongFragment;
import com.chorded.app.models.Song;
import com.chorded.app.models.User;
import com.chorded.app.session.AppSession;
import com.chorded.app.session.GuestStorage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.chorded.app.adapters.SongCardAdapter;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvRole;
    private final List<SongCard> inProgressCards = new ArrayList<>();
    private SongCardAdapter inProgressAdapter;

    private TextView tvLearnedSongsCount;
    private RecyclerView recyclerLearnedSongs;

    private TextView tvLearnedChordsCount;
    private TextView tvLearnedChordsList;

    private Button btnCreateSong, btnLogout;

    private final List<Song> learnedSongs = new ArrayList<>();
    private SongAdapter adapter;

    private FirebaseFirestore db;
    private GuestStorage guestStorage;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        tvEmail = v.findViewById(R.id.tvUserEmail);
        tvRole = v.findViewById(R.id.tvUserRole);

        tvLearnedSongsCount = v.findViewById(R.id.tvLearnedSongsCount);

        // 👇 добавь эти 2 TextView в fragment_profile.xml (см. ниже)
        tvLearnedChordsCount = v.findViewById(R.id.tvLearnedChordsCount);
        tvLearnedChordsList = v.findViewById(R.id.tvLearnedChordsList);

        btnCreateSong = v.findViewById(R.id.btnCreateSong);
        btnLogout = v.findViewById(R.id.btnLogout);


        recyclerLearnedSongs = v.findViewById(R.id.recyclerLearnedSongs);
        recyclerLearnedSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(learnedSongs, song -> {
            // можно открыть SongFragment при желании
        });
        recyclerLearnedSongs.setAdapter(adapter);

        RecyclerView recyclerInProgress =
                v.findViewById(R.id.recyclerInProgressSongs);

        recyclerInProgress.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        inProgressAdapter = new SongCardAdapter(
                inProgressCards,
                card -> openSong(card.getSong().getId())
        );

        recyclerInProgress.setAdapter(inProgressAdapter);


        db = FirebaseFirestore.getInstance();
        guestStorage = new GuestStorage(requireContext());

        if (AppSession.get().isGuest()) {
            setupGuest();
            loadGuestChords();
            loadGuestSongs();
        } else {
            setupUser();
            loadUserChords();
            loadUserSongs();
        }

        btnLogout.setOnClickListener(v1 -> {
            AppSession.get().logout(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
        loadInProgressSongs();

        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadInProgressSongs();
    }

    private void setupGuest() {
        tvEmail.setText("Гость");
        tvRole.setText("Guest");
        btnCreateSong.setVisibility(View.GONE);
    }

    private void setupUser() {
        String uid = AppSession.get().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user == null) return;

                    tvEmail.setText(user.getEmail());

                    if ("creator".equals(user.getRole())) {
                        tvRole.setText("Creator");
                        btnCreateSong.setVisibility(View.VISIBLE);
                        btnCreateSong.setOnClickListener(v ->
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragmentContainer, new CreateSongFragment())
                                        .addToBackStack(null)
                                        .commit());
                    } else {
                        tvRole.setText("User");
                        btnCreateSong.setVisibility(View.GONE);
                    }
                });
    }

    // ==========================
    // CHORDS
    // ==========================

    private void loadGuestChords() {
        Set<String> chords = guestStorage.getLearnedChords();

        tvLearnedChordsCount.setText("Выученные аккорды: " + chords.size());
        tvLearnedChordsList.setText(
                chords.isEmpty() ? "Нет выученных аккордов" : TextUtils.join(", ", chords)
        );
    }

    private void loadUserChords() {
        String uid = AppSession.get().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> chords = (List<String>) doc.get("learnedChords");
                    int count = chords == null ? 0 : chords.size();

                    tvLearnedChordsCount.setText("Выученные аккорды: " + count);
                    tvLearnedChordsList.setText(
                            (chords == null || chords.isEmpty())
                                    ? "Нет выученных аккордов"
                                    : TextUtils.join(", ", chords)
                    );
                });
    }

    // ==========================
    // SONGS (твоя логика + чуть аккуратнее)
    // ==========================

    private void loadUserSongs() {
        String uid = AppSession.get().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = (List<String>) doc.get("learnedSongs");

                    learnedSongs.clear();

                    int count = ids == null ? 0 : ids.size();
                    tvLearnedSongsCount.setText("Выученные песни: " + count);

                    if (ids == null) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (String id : ids) {
                        db.collection("songs").document(id)
                                .get()
                                .addOnSuccessListener(songDoc -> {
                                    Song s = songDoc.toObject(Song.class);
                                    if (s == null) return;
                                    s.setId(songDoc.getId());
                                    learnedSongs.add(s);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private void loadGuestSongs() {
        Set<String> ids = guestStorage.getLearnedSongs();

        learnedSongs.clear();
        tvLearnedSongsCount.setText("Выученные песни: " + ids.size());

        for (String id : ids) {
            db.collection("songs").document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Song s = doc.toObject(Song.class);
                        if (s == null) return;
                        s.setId(doc.getId());
                        learnedSongs.add(s);
                        adapter.notifyDataSetChanged();
                    });
        }
    }
    private void loadInProgressSongs() {

        Set<String> learnedChords;

        // 👻 GUEST
        if (AppSession.get().isGuest()) {
            learnedChords = guestStorage.getLearnedChords();
        }
        // 👤 USER
        else {
            String uid = AppSession.get().getUid();
            if (uid == null) return;

            // ⚠️ мы уже один раз грузим пользователя,
            // но для простоты пока делаем отдельно
            learnedChords = new HashSet<>();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        List<String> chords =
                                (List<String>) doc.get("learnedChords");

                        if (chords != null) {
                            learnedChords.addAll(chords);
                        }

                        buildInProgressCards(learnedChords);
                    });

            return;
        }

        // guest сразу
        buildInProgressCards(learnedChords);
    }
    private void buildInProgressCards(Set<String> learnedChords) {

        inProgressCards.clear();

        db.collection("songs")
                .get()
                .addOnSuccessListener(query -> {

                    for (var doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song == null) continue;

                        song.setId(doc.getId());

                        SongCard card =
                                SongCardBuilder.buildForRecommendation(song, learnedChords);

                        if (card == null) continue;

                        if (card.getStatus() == SongStatus.IN_PROGRESS) {
                            inProgressCards.add(card);
                        }
                    }

                    inProgressCards.sort((a, b) ->
                            Integer.compare(b.getMatchScore(), a.getMatchScore())
                    );

                    inProgressAdapter.notifyDataSetChanged();
                });
    }
    private void openSong(String songId) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SongFragment.newInstance(songId))
                .addToBackStack(null)
                .commit();
    }



}
