package com.example.melanmusicplayer;

import java.util.ArrayList;
import java.util.Random;

public class PlaylistManager {
    private final ArrayList<MusicScanner.Song> playlist = new ArrayList<>();
    private final Random random = new Random();
    private int currentIndex = -1;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;

    public void setPlaylist(ArrayList<MusicScanner.Song> songs) {
        playlist.clear();
        if (songs != null) {
            playlist.addAll(songs);
        }
        if (!playlist.isEmpty()) {
            currentIndex = 0;
        }
    }

    public MusicScanner.Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    public MusicScanner.Song getNext() {
        if (playlist.isEmpty()) return null;
        if (shuffleEnabled) {
            currentIndex = random.nextInt(playlist.size());
        } else {
            currentIndex = (currentIndex + 1) % playlist.size();
        }
        return playlist.get(currentIndex);
    }

    public MusicScanner.Song getPrevious() {
        if (playlist.isEmpty()) return null;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        return playlist.get(currentIndex);
    }

    public void setSongAt(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentIndex = index;
        }
    }

    public ArrayList<MusicScanner.Song> getPlaylist() {
        return playlist;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        this.shuffleEnabled = shuffleEnabled;
    }

    public boolean isRepeatEnabled() {
        return repeatEnabled;
    }

    public void setRepeatEnabled(boolean repeatEnabled) {
        this.repeatEnabled = repeatEnabled;
    }
}
