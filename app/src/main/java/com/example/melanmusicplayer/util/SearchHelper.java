package com.example.melanmusicplayer.util;

import com.example.melanmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchHelper {

    public static ArrayList<Song> filterSongs(ArrayList<Song> songs, String query) {
        if (query == null || query.trim().isEmpty()) {
            return songs;
        }

        String q = query.toLowerCase().trim();
        ArrayList<Song> results = new ArrayList<>();

        for (Song song : songs) {
            if (song.getTitle() != null && song.getTitle().toLowerCase().contains(q)
                    || song.getArtist() != null && song.getArtist().toLowerCase().contains(q)
                    || song.getAlbum() != null && song.getAlbum().toLowerCase().contains(q)) {
                results.add(song);
            }
        }

        return results;
    }

    public static ArrayList<Song> sortByTitle(ArrayList<Song> songs) {
        ArrayList<Song> sorted = new ArrayList<>(songs);
        Collections.sort(sorted, Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public static ArrayList<Song> sortByArtist(ArrayList<Song> songs) {
        ArrayList<Song> sorted = new ArrayList<>(songs);
        Collections.sort(sorted, Comparator.comparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public static ArrayList<Song> sortByDuration(ArrayList<Song> songs) {
        ArrayList<Song> sorted = new ArrayList<>(songs);
        sorted.sort((a, b) -> Long.compare(b.getDuration(), a.getDuration()));
        return sorted;
    }
}
