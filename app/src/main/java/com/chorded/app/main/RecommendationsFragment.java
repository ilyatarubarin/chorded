package com.chorded.app.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RecommendationsFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<RecommendationItem> recommendations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        RecyclerView recycler = view.findViewById(R.id.recyclerRec);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        RecAdapter adapter = new RecAdapter();
        recycler.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser().getUid();

        // 1️⃣ получаем аккорды пользователя
        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    List<String> learned = (List<String>) userDoc.get("learnedChords");
                    if (learned == null) learned = new ArrayList<>();

                    List<String> finalLearned = learned;

                    // 2️⃣ теперь загружаем песни
                    db.collection("songs").get().addOnSuccessListener(songDocs -> {

                        recommendations.clear();

                        for (var doc : songDocs) {
                            String title = doc.getString("title");
                            String artist = doc.getString("artist");
                            List<String> chords = (List<String>) doc.get("chords");

                            if (chords == null || chords.isEmpty()) continue;

                            int total = chords.size();
                            int matched = 0;

                            List<String> missing = new ArrayList<>();

                            // считаем совпадения и недостающие
                            for (String chord : chords) {
                                if (finalLearned.contains(chord)) matched++;
                                else missing.add(chord);
                            }

                            double percent = (matched * 100.0) / total;

                            // добавляем результат в список
                            recommendations.add(new RecommendationItem(
                                    title, artist, percent, matched, total, missing
                            ));
                        }

                        // 3️⃣ сортируем по проценту совпадения
                        Collections.sort(recommendations, (a, b) ->
                                Double.compare(b.percent, a.percent));

                        adapter.notifyDataSetChanged();
                    });
                });

        return view;
    }

    // -----------------------------------------------------------
    // Класс данных для рекомендации
    // -----------------------------------------------------------
    private static class RecommendationItem {
        String title;
        String artist;
        double percent;
        int matched;
        int total;
        List<String> missing;

        RecommendationItem(String title, String artist, double percent,
                           int matched, int total, List<String> missing) {
            this.title = title;
            this.artist = artist;
            this.percent = percent;
            this.matched = matched;
            this.total = total;
            this.missing = missing;
        }
    }

    // -----------------------------------------------------------
    // Адаптер списка рекомендаций
    // -----------------------------------------------------------
    private class RecAdapter extends RecyclerView.Adapter<RecAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommendation, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int pos) {
            RecommendationItem item = recommendations.get(pos);

            h.title.setText(item.title + " — " + item.artist);
            h.percent.setText(String.format("Совпадение: %.0f%%", item.percent));
            h.summary.setText(item.matched + "/" + item.total + " аккордов");

            if (item.missing.isEmpty()) {
                h.missing.setText("Все аккорды изучены ✔");
                h.missing.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                h.missing.setText("Нужно выучить: " + item.missing);
                h.missing.setTextColor(Color.parseColor("#F44336"));
            }
        }

        @Override
        public int getItemCount() {
            return recommendations.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView title, percent, summary, missing;
            Holder(View v) {
                super(v);
                title = v.findViewById(R.id.recTitle);
                percent = v.findViewById(R.id.recPercent);
                summary = v.findViewById(R.id.recSummary);
                missing = v.findViewById(R.id.recMissing);
            }
        }
    }
}
