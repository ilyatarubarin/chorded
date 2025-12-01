package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.adapters.ChordGridAdapter;
import com.chorded.app.models.Chord;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChordsFragment extends Fragment {

    private RecyclerView recycler;
    private ChordGridAdapter adapter;
    private final List<Chord> chordList = new ArrayList<>();

    private FirebaseFirestore db;

    public ChordsFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chords, container, false);

        db = FirebaseFirestore.getInstance();

        recycler = view.findViewById(R.id.recyclerChords);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new ChordGridAdapter(chordList, chord -> openChord(chord.getId()));
        recycler.setAdapter(adapter);

        loadChords();

        return view;
    }

    private void loadChords() {
        db.collection("chords")
                .get()
                .addOnSuccessListener(query -> {
                    chordList.clear();
                    for (var doc : query) {
                        Chord c = doc.toObject(Chord.class);
                        c.setId(doc.getId());
                        chordList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void openChord(String chordId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, ChordFragment.newInstance(chordId))
                .addToBackStack(null)
                .commit();
    }
}
