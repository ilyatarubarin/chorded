package com.chorded.app.creator;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateSongFragment extends Fragment {

    private EditText etTitle, etArtist, etChords, etIconUrl, etMp3Url, etLyrics;
    private Button btnCreate;

    private FirebaseFirestore db;

    public CreateSongFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_create_song, container, false);

        etTitle = v.findViewById(R.id.etTitle);
        etArtist = v.findViewById(R.id.etArtist);
        etChords = v.findViewById(R.id.etChords);
        etIconUrl = v.findViewById(R.id.etIconUrl);
        etMp3Url = v.findViewById(R.id.etMp3Url);
        etLyrics = v.findViewById(R.id.etLyrics);
        btnCreate = v.findViewById(R.id.btnCreateSong);

        db = FirebaseFirestore.getInstance();

        btnCreate.setOnClickListener(view -> createSong());

        return v;
    }

    private void createSong() {

        String title = etTitle.getText().toString().trim();
        String artist = etArtist.getText().toString().trim();
        String chordsText = etChords.getText().toString().trim();
        String iconUrl = etIconUrl.getText().toString().trim();
        String lyrics = etLyrics.getText().toString().trim();

        // Простая валидация
        if (TextUtils.isEmpty(title) ||
                TextUtils.isEmpty(artist) ||
                TextUtils.isEmpty(chordsText)) {

            Toast.makeText(getContext(),
                    "Заполните обязательные поля",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Преобразуем "C,G,Am,F" → List<String>
        List<String> chords = Arrays.asList(chordsText.split("\\s*,\\s*"));

        Map<String, Object> song = new HashMap<>();
        song.put("title", title);
        song.put("artist", artist);
        song.put("chords", chords);
        song.put("iconUrl", iconUrl);
        song.put("lyricsChordPro", lyrics);
        song.put("difficulty", chords.size()); // простая логика

        db.collection("songs")
                .add(song)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(),
                            "Песня добавлена",
                            Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Ошибка: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void clearFields() {
        etTitle.setText("");
        etArtist.setText("");
        etChords.setText("");
        etIconUrl.setText("");
        etLyrics.setText("");
    }
}
