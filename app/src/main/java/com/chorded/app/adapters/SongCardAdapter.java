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
import com.chorded.app.recommendations.SongCard;

import java.util.List;

public class SongCardAdapter
        extends RecyclerView.Adapter<SongCardAdapter.ViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(SongCard card);
    }

    private final List<SongCard> cards;
    private final OnSongClickListener listener;

    public SongCardAdapter(
            List<SongCard> cards,
            OnSongClickListener listener
    ) {
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        SongCard card = cards.get(position);

        holder.title.setText(card.getSong().getTitle());
        holder.artist.setText(card.getSong().getArtist());
        holder.summary.setText(card.getSummary());

        Glide.with(holder.itemView.getContext())
                .load(card.getSong().getIconUrl())
                .placeholder(R.drawable.song_placeholder)
                .into(holder.icon);

        holder.itemView.setOnClickListener(v ->
                listener.onSongClick(card)
        );
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, artist, summary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconSong);
            title = itemView.findViewById(R.id.tvSongTitle);
            artist = itemView.findViewById(R.id.tvSongArtist);
            summary = itemView.findViewById(R.id.tvSongSummary);
        }
    }
}
