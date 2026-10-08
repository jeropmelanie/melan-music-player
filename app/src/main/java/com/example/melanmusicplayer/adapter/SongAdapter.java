package com.example.melanmusicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.melanmusicplayer.R;
import com.example.melanmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    private final List<Song> songs = new ArrayList<>();
    private final OnSongClickListener listener;

    public SongAdapter(List<Song> initialSongs, OnSongClickListener listener) {
        this.listener = listener;
        if (initialSongs != null) {
            songs.addAll(initialSongs);
        }
    }

    public void updateSongs(List<Song> newSongs) {
        songs.clear();
        if (newSongs != null) {
            songs.addAll(newSongs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        if (song == null) {
            return;
        }

        String label = song.getTitle() + " - " + song.getArtist();
        holder.titleText.setText(label);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(android.R.id.text1);
        }
    }
}
