package com.example.melanmusicplayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private final Set<String> favorites = new HashSet<>();

    public void toggleFavorite(MusicScanner.Song song) {
        if (favorites.contains(song.path)) {
            favorites.remove(song.path);
            song.isFavorite = false;
        } else {
            favorites.add(song.path);
            song.isFavorite = true;
        }
    }

    public boolean isFavorite(MusicScanner.Song song) {
        return favorites.contains(song.path);
    }

    public ArrayList<MusicScanner.Song> getFavorites(ArrayList<MusicScanner.Song> allSongs) {
        ArrayList<MusicScanner.Song> favoritesList = new ArrayList<>();
        for (MusicScanner.Song song : allSongs) {
            if (isFavorite(song)) {
                favoritesList.add(song);
            }
        }
        return favoritesList;
    }
}
