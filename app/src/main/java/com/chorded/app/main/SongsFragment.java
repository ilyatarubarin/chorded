package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SongsFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Map<String, Object>> songs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        RecyclerView recycler = view.findViewById(R.id.recyclerSongs);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        SongsAdapter adapter = new SongsAdapter();
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        db.collection("songs").get().addOnSuccessListener(snapshot -> {
            songs.clear();
            for (var doc : snapshot) songs.add(doc.getData());
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    // ---------------------------- Адаптер ----------------------------

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_song, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int pos) {
            Map<String, Object> song = songs.get(pos);

            h.title.setText((String) song.get("title"));
            h.artist.setText((String) song.get("artist"));

            String icon = (String) song.get("iconUrl");
            Glide.with(h.icon.getContext()).load(icon).into(h.icon);

            h.itemView.setOnClickListener(v -> {
                SongFragment fragment = new SongFragment();
                Bundle b = new Bundle();
                b.putString("title", (String) song.get("title"));
                fragment.setArguments(b);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, artist;

            Holder(View v) {
                super(v);
                icon = v.findViewById(R.id.iconSong);
                title = v.findViewById(R.id.tvSongTitle);
                artist = v.findViewById(R.id.tvSongArtist);
            }
        }
    }
}
