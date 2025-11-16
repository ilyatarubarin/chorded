package com.chorded.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChordFragment extends Fragment {

    private static final String ARG_NAME = "chordName";

    private String chordName;

    public static ChordFragment newInstance(String name) {
        ChordFragment fragment = new ChordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chord, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        TextView tvName = v.findViewById(R.id.tvChordName);
        TextView tvDiagram = v.findViewById(R.id.tvChordDiagram);
        TextView tvStatus = v.findViewById(R.id.tvChordStatus);
        Button btnLearn = v.findViewById(R.id.btnLearnChord);

        if (getArguments() != null)
            chordName = getArguments().getString(ARG_NAME);

        tvName.setText(chordName);

        // Загружаем аккорд из Firestore
        db.collection("chords").document(chordName).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> frets = (List<String>) doc.get("diagramFrets");
                        List<Long> fingers = (List<Long>) doc.get("diagramFingers");

                        // диаграмма в текстовом виде
                        String diagram = "Струны: " + frets + "\nПальцы: " + fingers;
                        tvDiagram.setText(diagram);
                    }
                });

        // Проверяем, изучен ли аккорд
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    List<String> learned = (List<String>) doc.get("learnedChords");
                    if (learned == null) learned = new ArrayList<>();

                    if (learned.contains(chordName)) {
                        tvStatus.setText("Аккорд изучен ✔");
                        btnLearn.setText("Удалить из изученных");

                    } else {
                        tvStatus.setText("Аккорд пока не изучен");
                        btnLearn.setText("Выучить аккорд");
                    }

                    // обработчик кнопки
                    List<String> finalLearned = learned;
                    btnLearn.setOnClickListener(v2 -> {

                        boolean already = finalLearned.contains(chordName);

                        if (already) {
                            finalLearned.remove(chordName);
                            tvStatus.setText("Аккорд пока не изучен");
                            btnLearn.setText("Выучить аккорд");
                        } else {
                            finalLearned.add(chordName);
                            tvStatus.setText("Аккорд изучен ✔");
                            btnLearn.setText("Удалить из изученных");
                        }

                        db.collection("users").document(uid)
                                .update("learnedChords", finalLearned);
                    });
                });

        return v;
    }
}
