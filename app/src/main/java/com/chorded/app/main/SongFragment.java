package com.chorded.app.main;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongFragment extends Fragment {

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_song, container, false);

        LinearLayout containerView = view.findViewById(R.id.songContainer);
        db = FirebaseFirestore.getInstance();

        String title = getArguments().getString("title");

        db.collection("songs").whereEqualTo("title", title).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;

                    String chordPro = snap.getDocuments().get(0).getString("chordPro");

                    for (String line : chordPro.split("\n")) {
                        containerView.addView(formatLine(line));
                    }
                });

        return view;
    }

    private TextView formatLine(String line) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Pattern p = Pattern.compile("\\[([A-G][mM]?(?:7|sus2|sus4|add9)?)\\]");
        Matcher m = p.matcher(line);

        int last = 0;

        while (m.find()) {
            builder.append(line, last, m.start());

            String chord = m.group(1);
            int start = builder.length();

            builder.append(chord);

            builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, start + chord.length(), 0);
            builder.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_bright)),
                    start, start + chord.length(), 0);

            last = m.end();
        }

        builder.append(line.substring(last));

        TextView tv = new TextView(getContext());
        tv.setText(builder);
        tv.setTextSize(16);
        return tv;
    }
}
