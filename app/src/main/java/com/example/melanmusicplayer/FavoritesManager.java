package com.example.melanmusicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.melanmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persists favorite songs across app launches.
 * Stores Song IDs rather than raw file paths so the library can be rebuilt safely.
 */
public class FavoritesManager {
    private static final String PREF_NAME = "melan_music_favorites";
    private static final String KEY_FAVORITE_IDS = "favorite_song_ids";

    private final SharedPreferences preferences;

    public FavoritesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Set<String> getFavoriteIds() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITE_IDS, new HashSet<>()));
    }

    public void saveFavoriteIds(Set<String> favoriteIds) {
        preferences.edit().putStringSet(KEY_FAVORITE_IDS, favoriteIds).apply();
    }

    public boolean isFavorite(Song song) {
        if (song == null) {
            return false;
        }
        return getFavoriteIds().contains(String.valueOf(song.getId()));
    }

    public void toggleFavorite(Song song) {
        if (song == null) {
            return;
        }

        Set<String> ids = getFavoriteIds();
        String idKey = String.valueOf(song.getId());

        if (ids.contains(idKey)) {
            ids.remove(idKey);
        } else {
            ids.add(idKey);
        }

        saveFavoriteIds(ids);
        song.setFavorite(ids.contains(idKey));
    }

    public void addFavorite(Song song) {
        if (song == null) {
            return;
        }

        Set<String> ids = getFavoriteIds();
        ids.add(String.valueOf(song.getId()));
        saveFavoriteIds(ids);
        song.setFavorite(true);
    }

    public void removeFavorite(Song song) {
        if (song == null) {
            return;
        }

        Set<String> ids = getFavoriteIds();
        ids.remove(String.valueOf(song.getId()));
        saveFavoriteIds(ids);
        song.setFavorite(false);
    }

    public List<Song> getFavoriteSongs(List<Song> allSongs) {
        if (allSongs == null) {
            return new ArrayList<>();
        }

        Set<String> favoriteIds = getFavoriteIds();
        List<Song> liked = new ArrayList<>();

        for (Song song : allSongs) {
            if (song != null && favoriteIds.contains(String.valueOf(song.getId()))) {
                liked.add(song);
            }
        }

        return liked;
    }

    public void clearFavorites() {
        saveFavoriteIds(new HashSet<>());
    }
}
