package com.chorded.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chorded.app.R;
import com.chorded.app.models.Chord;

import java.util.ArrayList;
import java.util.List;

public class ChordGridAdapter extends RecyclerView.Adapter<ChordGridAdapter.Holder> {

    public interface OnChordClick {
        void onChordClick(Chord chord);
    }

    private List<Chord> items;
    private final OnChordClick listener;

    public ChordGridAdapter(List<Chord> items, OnChordClick listener) {
        this.items = items == null ? new ArrayList<>() : items;
        this.listener = listener;
    }

    public void updateList(List<Chord> newList) {
        this.items = newList == null ? new ArrayList<>() : new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chord, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Chord c = items.get(position);
        Context context = holder.itemView.getContext();

        // Текст скрыт в XML (visibility=gone), можно не устанавливать,
        // но оставим на всякий случай, если решишь вернуть.
        // holder.name.setText(c.getName() != null ? c.getName() : c.getId());

        // ЛОГИКА ДЛЯ СПИСКА (пункт 2): берем img_src_with_name
        String rawPath = c.getImg_src_with_name();

        if (rawPath != null && !rawPath.isEmpty()) {
            // Отрезаем папку "chords/", если она есть в пути
            // Пример: "chords/Am_chord_with_name" -> "Am_chord_with_name"
            String resName = rawPath.contains("/")
                    ? rawPath.substring(rawPath.lastIndexOf("/") + 1)
                    : rawPath;

            // Ищем ресурс по имени
            int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());

            if (resId != 0) {
                holder.image.setImageResource(resId);
            } else {
                holder.image.setImageResource(R.drawable.chord_placeholder);
            }
        } else {
            holder.image.setImageResource(R.drawable.chord_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChordClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        public Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.chordImage);
            name = itemView.findViewById(R.id.chordName);
        }
    }
}