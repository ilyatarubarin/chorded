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
import com.chorded.app.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    private List<Song> songs;
    private OnSongClickListener listener;

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.summary.setText("Аккорды: " + song.getChords().toString());

        Glide.with(holder.itemView.getContext())
                .load(song.getIconUrl())
                .placeholder(R.drawable.song_placeholder)
                .into(holder.icon);

        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, artist, summary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.iconSong);
            title = itemView.findViewById(R.id.tvSongTitle);
            artist = itemView.findViewById(R.id.tvSongArtist);
            summary = itemView.findViewById(R.id.tvSongSummary);
        }
    }
}
