package com.example.melanmusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoritesStore {
    private static final String PREFS_NAME = "melan_music_favorites";
    private static final String KEY_FAVORITES = "favorite_paths";

    private final SharedPreferences prefs;

    public FavoritesStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(String path) {
        return prefs.getStringSet(KEY_FAVORITES, new HashSet<>()).contains(path);
    }

    public void toggleFavorite(String path) {
        Set<String> favorites = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
        if (favorites.contains(path)) {
            favorites.remove(path);
        } else {
            favorites.add(path);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }
}
