package com.example.melanmusicplayer;

import java.util.ArrayList;

public class QueueManager {
    private final ArrayList<MusicScanner.Song> queue = new ArrayList<>();
    private int currentIndex = -1;

    public void setQueue(ArrayList<MusicScanner.Song> songs) {
        queue.clear();
        if (songs != null) {
            queue.addAll(songs);
        }
        if (!queue.isEmpty()) {
            currentIndex = 0;
        }
    }

    public void addToQueue(MusicScanner.Song song) {
        queue.add(song);
    }

    public ArrayList<MusicScanner.Song> getQueue() {
        return queue;
    }

    public MusicScanner.Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < queue.size()) {
            return queue.get(currentIndex);
        }
        return null;
    }

    public void setCurrentIndex(int index) {
        if (index >= 0 && index < queue.size()) {
            currentIndex = index;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}
