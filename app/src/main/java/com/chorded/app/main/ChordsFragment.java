package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChordsFragment extends Fragment {

    private ListView listChords;
    private ArrayAdapter<String> adapter;

    private List<String> chords = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chords, container, false);

        db = FirebaseFirestore.getInstance();
        listChords = view.findViewById(R.id.listChords);

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                chords
        );

        listChords.setAdapter(adapter);

        loadChords();
        setupItemClick();

        return view;
    }

    // ----------------------------------------------------------
    //  Загрузка всех аккордов из Firestore → коллекция "chords"
    // ----------------------------------------------------------
    private void loadChords() {
        db.collection("chords").get()
                .addOnSuccessListener(query -> {

                    chords.clear();

                    for (var doc : query) {
                        String chordName = doc.getId();
                        chords.add(chordName);
                    }

                    // сортируем, чтобы по алфавиту
                    Collections.sort(chords);

                    adapter.notifyDataSetChanged();
                });
    }

    // ----------------------------------------------------------
    //  При клике открываем экран выбранного аккорда
    // ----------------------------------------------------------
    private void setupItemClick() {
        listChords.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {

            String selectedChord = chords.get(position);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, ChordFragment.newInstance(selectedChord))
                    .addToBackStack(null)
                    .commit();
        });
    }
}
