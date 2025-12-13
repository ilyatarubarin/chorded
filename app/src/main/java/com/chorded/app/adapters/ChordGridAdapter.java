package com.chorded.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chorded.app.R;
import com.chorded.app.models.Chord;

import java.util.ArrayList;
import java.util.List;

/**
 * Сеточный адаптер для отображения аккордов (image + name).
 * Ожидает, что Chord.imageUrl содержит ссылку (Storage) или null.
 */
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

        holder.name.setText(c.getName() != null ? c.getName() : c.getId());

        // Загружаем картинку аккорда: если imageUrl есть — Glide, иначе placeholder
        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            Glide.with(holder.image.getContext())
                    .load(c.getImageUrl())
                    .placeholder(R.drawable.chord_placeholder)
                    .error(R.drawable.chord_placeholder)
                    .into(holder.image);
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
