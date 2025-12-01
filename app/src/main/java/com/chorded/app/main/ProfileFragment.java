package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.adapters.SongAdapter;
import com.chorded.app.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvCount;
    private RecyclerView recycler;

    private SongAdapter adapter;
    private final List<Song> learnedSongs = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        tvEmail = v.findViewById(R.id.tvUserEmail);
        tvCount = v.findViewById(R.id.tvLearnedSongsCount);
        recycler = v.findViewById(R.id.recyclerLearnedSongs);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(learnedSongs, song -> {});
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadProfileInfo();
        loadLearnedSongs();

        return v;
    }

    private void loadProfileInfo() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        tvEmail.setText(email);
    }

    private void loadLearnedSongs() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = (List<String>) doc.get("learnedSongs");
                    if (ids == null) return;

                    learnedSongs.clear();

                    for (String songId : ids) {
                        db.collection("songs").document(songId)
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
}
