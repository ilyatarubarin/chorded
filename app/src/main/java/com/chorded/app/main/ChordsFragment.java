package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChordsFragment extends Fragment {

    private FirebaseFirestore db;
    private List<String> chords = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chords, container, false);
        ListView listView = view.findViewById(R.id.listChords);

        db = FirebaseFirestore.getInstance();

        // Загрузка аккордов из Firestore
        db.collection("chords").get()
                .addOnSuccessListener(snapshots -> {
                    for (var doc : snapshots) {
                        chords.add(doc.getString("name"));
                    }
                    listView.setAdapter(new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_list_item_1, chords));
                });

        return view;
    }
}
