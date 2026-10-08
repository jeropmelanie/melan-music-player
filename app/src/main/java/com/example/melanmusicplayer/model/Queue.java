package com.example.melanmusicplayer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the playback queue - ordered list of songs to play.
 */
public class Queue implements Serializable {
    private static final long serialVersionUID = 1L;

    private long queueId;
    private String queueName;
    private List<Song> songs;
    private int currentIndex;
    private List<Integer> shuffledIndices;    // For shuffle mode
    private long dateCreated;
    private long dateModified;

    // Constructors
    public Queue() {
        this.songs = new ArrayList<>();
        this.currentIndex = 0;
        this.shuffledIndices = new ArrayList<>();
        this.dateCreated = System.currentTimeMillis();
        this.dateModified = System.currentTimeMillis();
        this.queueId = System.currentTimeMillis();  // Generate ID from timestamp
    }

    public Queue(String queueName) {
        this();
        this.queueName = queueName;
    }

    public Queue(List<Song> songs) {
        this();
        setSongs(songs);
    }

    // Getters and Setters
    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs != null ? songs : new ArrayList<>();
        this.currentIndex = 0;
        this.dateModified = System.currentTimeMillis();
        generateShuffledIndices();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        if (currentIndex >= 0 && currentIndex < songs.size()) {
            this.currentIndex = currentIndex;
            this.dateModified = System.currentTimeMillis();
        }
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    // Queue management methods
    /**
     * Get current song in queue
     */
    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < songs.size()) {
            return songs.get(currentIndex);
        }
        return null;
    }

    /**
     * Get next song
     */
    public Song getNextSong() {
        if (currentIndex + 1 < songs.size()) {
            return songs.get(currentIndex + 1);
        }
        return null;
    }

    /**
     * Get previous song
     */
    public Song getPreviousSong() {
        if (currentIndex > 0) {
            return songs.get(currentIndex - 1);
        }
        return null;
    }

    /**
     * Get song at specific index
     */
    public Song getSongAt(int index) {
        if (index >= 0 && index < songs.size()) {
            return songs.get(index);
        }
        return null;
    }

    /**
     * Move to next song
     */
    public Song moveNext() {
        if (currentIndex + 1 < songs.size()) {
            currentIndex++;
            this.dateModified = System.currentTimeMillis();
            return getCurrentSong();
        }
        return null;
    }

    /**
     * Move to previous song
     */
    public Song movePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            this.dateModified = System.currentTimeMillis();
            return getCurrentSong();
        }
        return null;
    }

    /**
     * Add song to end of queue
     */
    public void addSong(Song song) {
        if (song != null) {
            songs.add(song);
            this.dateModified = System.currentTimeMillis();
        }
    }

    /**
     * Add songs to end of queue
     */
    public void addSongs(List<Song> songsToAdd) {
        if (songsToAdd != null && !songsToAdd.isEmpty()) {
            songs.addAll(songsToAdd);
            this.dateModified = System.currentTimeMillis();
        }
    }

    /**
     * Insert song at specific position
     */
    public void insertSong(int index, Song song) {
        if (song != null && index >= 0 && index <= songs.size()) {
            songs.add(index, song);
            if (index <= currentIndex) {
                currentIndex++;
            }
            this.dateModified = System.currentTimeMillis();
        }
    }

    /**
     * Remove song from queue
     */
    public boolean removeSong(Song song) {
        int index = songs.indexOf(song);
        if (index >= 0) {
            songs.remove(index);
            if (index < currentIndex) {
                currentIndex--;
            } else if (index == currentIndex && currentIndex >= songs.size() && currentIndex > 0) {
                currentIndex--;
            }
            this.dateModified = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Remove song at index
     */
    public boolean removeSongAt(int index) {
        if (index >= 0 && index < songs.size()) {
            songs.remove(index);
            if (index < currentIndex) {
                currentIndex--;
            } else if (index == currentIndex && currentIndex >= songs.size() && currentIndex > 0) {
                currentIndex--;
            }
            this.dateModified = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Move song to new position
     */
    public boolean moveSong(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= songs.size() || toIndex >= songs.size()) {
            return false;
        }
        Song song = songs.remove(fromIndex);
        songs.add(toIndex, song);
        
        // Adjust current index if needed
        if (fromIndex == currentIndex) {
            currentIndex = toIndex;
        } else if (fromIndex < currentIndex && toIndex >= currentIndex) {
            currentIndex--;
        } else if (fromIndex > currentIndex && toIndex <= currentIndex) {
            currentIndex++;
        }
        
        this.dateModified = System.currentTimeMillis();
        return true;
    }

    /**
     * Clear entire queue
     */
    public void clear() {
        songs.clear();
        currentIndex = 0;
        shuffledIndices.clear();
        this.dateModified = System.currentTimeMillis();
    }

    /**
     * Get total size of queue
     */
    public int size() {
        return songs.size();
    }

    /**
     * Check if queue is empty
     */
    public boolean isEmpty() {
        return songs.isEmpty();
    }

    /**
     * Check if there is a next song
     */
    public boolean hasNext() {
        return currentIndex + 1 < songs.size();
    }

    /**
     * Check if there is a previous song
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Get total duration of all songs in queue
     */
    public long getTotalDuration() {
        long total = 0;
        for (Song song : songs) {
            total += song.getDuration();
        }
        return total;
    }

    /**
     * Get remaining duration from current song
     */
    public long getRemainingDuration() {
        long total = 0;
        for (int i = currentIndex; i < songs.size(); i++) {
            total += songs.get(i).getDuration();
        }
        return total;
    }

    // Shuffle support
    /**
     * Generate shuffled indices for shuffle mode
     */
    private void generateShuffledIndices() {
        shuffledIndices.clear();
        for (int i = 0; i < songs.size(); i++) {
            shuffledIndices.add(i);
        }
        Collections.shuffle(shuffledIndices);
    }

    /**
     * Get current song index in shuffle mode
     */
    public int getShuffledIndex(int position) {
        if (position >= 0 && position < shuffledIndices.size()) {
            return shuffledIndices.get(position);
        }
        return position;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "queueId=" + queueId +
                ", queueName='" + queueName + '\'' +
                ", size=" + songs.size() +
                ", currentIndex=" + currentIndex +
                '}';
    }
}
