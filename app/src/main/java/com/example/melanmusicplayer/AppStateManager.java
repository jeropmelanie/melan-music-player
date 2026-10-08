package com.example.melanmusicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.melanmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Final app state manager for user sessions.
 * Persists last played song, queue, and user-created playlists.
 */
public class AppStateManager {
    private static final String PREF_NAME = "melan_music_state";

    private static final String KEY_LAST_PLAYED_ID = "last_played_id";
    private static final String KEY_LAST_PLAYED_POSITION = "last_played_position";
    private static final String KEY_LAST_QUEUE = "last_queue_ids";
    private static final String KEY_PLAYLISTS = "saved_playlists";

    private final SharedPreferences preferences;

    public AppStateManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLastPlayed(Song song) {
        if (song == null) return;
        preferences.edit()
                .putLong(KEY_LAST_PLAYED_ID, song.getId())
                .putLong(KEY_LAST_PLAYED_POSITION, song.getPlaybackPosition())
                .apply();
    }

    public long getLastPlayedId() {
        return preferences.getLong(KEY_LAST_PLAYED_ID, -1L);
    }

    public long getLastPlayedPosition() {
        return preferences.getLong(KEY_LAST_PLAYED_POSITION, 0L);
    }

    public void saveQueue(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            preferences.edit().remove(KEY_LAST_QUEUE).apply();
            return;
        }

        Set<String> ids = new LinkedHashSet<>();
        for (Song song : songs) {
            if (song != null) {
                ids.add(String.valueOf(song.getId()));
            }
        }
        preferences.edit().putStringSet(KEY_LAST_QUEUE, ids).apply();
    }

    public List<Long> getSavedQueueIds() {
        Set<String> values = preferences.getStringSet(KEY_LAST_QUEUE, new LinkedHashSet<>());
        List<Long> ids = new ArrayList<>();
        for (String value : values) {
            try {
                ids.add(Long.parseLong(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    public void savePlaylist(String playlistName, List<Song> songs) {
        if (playlistName == null || playlistName.trim().isEmpty()) {
            return;
        }

        String raw = preferences.getString(KEY_PLAYLISTS, "");
        Set<String> playlists = new LinkedHashSet<>();
        if (!raw.isEmpty()) {
            for (String item : raw.split(";;")) {
                if (!item.trim().isEmpty()) {
                    playlists.add(item);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (songs != null) {
            for (Song song : songs) {
                if (song != null) {
                    if (builder.length() > 0) builder.append(",");
                    builder.append(song.getId());
                }
            }
        }

        playlists.add(playlistName + "=" + builder);
        preferences.edit().putString(KEY_PLAYLISTS, String.join(";;", playlists)).apply();
    }

    public List<String> getSavedPlaylistNames() {
        String raw = preferences.getString(KEY_PLAYLISTS, "");
        List<String> names = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return names;
        }

        for (String item : raw.split(";;")) {
            if (item.contains("=")) {
                names.add(item.substring(0, item.indexOf('=')));
            }
        }
        return names;
    }

    public List<Long> getPlaylistSongIds(String playlistName) {
        String raw = preferences.getString(KEY_PLAYLISTS, "");
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }

        for (String item : raw.split(";;")) {
            if (item.startsWith(playlistName + "=")) {
                List<Long> ids = new ArrayList<>();
                String values = item.substring(item.indexOf('=') + 1);
                if (values.trim().isEmpty()) {
                    return ids;
                }
                for (String token : values.split(",")) {
                    try {
                        ids.add(Long.parseLong(token));
                    } catch (NumberFormatException ignored) {
                    }
                }
                return ids;
            }
        }
        return new ArrayList<>();
    }

    public void removePlaylist(String playlistName) {
        String raw = preferences.getString(KEY_PLAYLISTS, "");
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }

        List<String> filtered = new ArrayList<>();
        for (String item : raw.split(";;")) {
            if (!item.startsWith(playlistName + "=")) {
                filtered.add(item);
            }
        }
        preferences.edit().putString(KEY_PLAYLISTS, String.join(";;", filtered)).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
