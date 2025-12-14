package com.chorded.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

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

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvRole, tvCount;
    private Button btnCreateSong, btnLogout;
    private RecyclerView recycler;

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
        tvCount = v.findViewById(R.id.tvLearnedSongsCount);
        btnCreateSong = v.findViewById(R.id.btnCreateSong);
        btnLogout = v.findViewById(R.id.btnLogout);

        recycler = v.findViewById(R.id.recyclerLearnedSongs);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(learnedSongs, song -> {});
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        guestStorage = new GuestStorage(requireContext());

        if (AppSession.get().isGuest()) {
            setupGuest();
            loadGuestSongs();
        } else {
            setupUser();
            loadUserSongs();
        }

        btnLogout.setOnClickListener(v1 -> {
            AppSession.get().logout(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        return v;
    }

    // ---------------- USER ----------------

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

    private void loadUserSongs() {
        String uid = AppSession.get().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = (List<String>) doc.get("learnedSongs");
                    if (ids == null) return;

                    learnedSongs.clear();

                    for (String id : ids) {
                        db.collection("songs").document(id)
                                .get()
                                .addOnSuccessListener(songDoc -> {
                                    Song s = songDoc.toObject(Song.class);
                                    s.setId(songDoc.getId());
                                    learnedSongs.add(s);
                                    tvCount.setText("Изучено песен: " + learnedSongs.size());
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    // ---------------- GUEST ----------------

    private void setupGuest() {
        tvEmail.setText("Гость");
        tvRole.setText("Guest");
        btnCreateSong.setVisibility(View.GONE);
    }

    private void loadGuestSongs() {
        Set<String> ids = guestStorage.getLearnedSongs();
        learnedSongs.clear();

        for (String id : ids) {
            db.collection("songs").document(id)
                    .get()
                    .addOnSuccessListener(songDoc -> {
                        Song s = songDoc.toObject(Song.class);
                        s.setId(songDoc.getId());
                        learnedSongs.add(s);
                        tvCount.setText("Изучено песен: " + learnedSongs.size());
                        adapter.notifyDataSetChanged();
                    });
        }
    }
}
