package com.example.melanmusicplayer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user-created or system playlist.
 */
public class Playlist implements Serializable, Comparable<Playlist> {
    private static final long serialVersionUID = 1L;

    public enum PlaylistType {
        USER_CREATED,
        SYSTEM,           // All Songs, Recently Played, etc
        SMART,            // Auto-generated based on criteria
        FAVORITE
    }

    private long playlistId;
    private String playlistName;
    private String description;
    private PlaylistType playlistType;
    private long dateCreated;
    private long dateModified;
    private String thumbnailPath;      // Thumbnail for playlist
    private List<Song> songs;
    private boolean isEditable;        // Can user modify this playlist

    // Constructors
    public Playlist() {
        this.songs = new ArrayList<>();
        this.isEditable = true;
        this.playlistType = PlaylistType.USER_CREATED;
        this.dateCreated = System.currentTimeMillis();
        this.dateModified = System.currentTimeMillis();
    }

    public Playlist(long playlistId, String playlistName, PlaylistType type) {
        this();
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistType = type;
    }

    // Getters and Setters
    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlaylistType getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(PlaylistType playlistType) {
        this.playlistType = playlistType;
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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        this.dateModified = System.currentTimeMillis();
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    // Utility methods
    /**
     * Get number of songs in playlist
     */
    public int getSongCount() {
        return songs != null ? songs.size() : 0;
    }

    /**
     * Add a song to playlist
     */
    public boolean addSong(Song song) {
        if (!isEditable || song == null || songs.contains(song)) {
            return false;
        }
        songs.add(song);
        this.dateModified = System.currentTimeMillis();
        return true;
    }

    /**
     * Remove a song from playlist
     */
    public boolean removeSong(Song song) {
        if (!isEditable || song == null) {
            return false;
        }
        boolean removed = songs.remove(song);
        if (removed) {
            this.dateModified = System.currentTimeMillis();
        }
        return removed;
    }

    /**
     * Remove song at specific index
     */
    public boolean removeSongAt(int index) {
        if (!isEditable || index < 0 || index >= songs.size()) {
            return false;
        }
        songs.remove(index);
        this.dateModified = System.currentTimeMillis();
        return true;
    }

    /**
     * Move song to new position
     */
    public boolean moveSong(int fromIndex, int toIndex) {
        if (!isEditable || fromIndex < 0 || toIndex < 0 || 
            fromIndex >= songs.size() || toIndex >= songs.size()) {
            return false;
        }
        Song song = songs.remove(fromIndex);
        songs.add(toIndex, song);
        this.dateModified = System.currentTimeMillis();
        return true;
    }

    /**
     * Clear all songs from playlist
     */
    public boolean clear() {
        if (!isEditable) {
            return false;
        }
        songs.clear();
        this.dateModified = System.currentTimeMillis();
        return true;
    }

    /**
     * Get total duration of all songs
     */
    public long getTotalDuration() {
        long total = 0;
        for (Song song : songs) {
            total += song.getDuration();
        }
        return total;
    }

    /**
     * Get formatted total duration
     */
    public String getFormattedTotalDuration() {
        long milliseconds = getTotalDuration();
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Check if playlist is empty
     */
    public boolean isEmpty() {
        return songs == null || songs.isEmpty();
    }

    /**
     * Get display info
     */
    public String getDisplayInfo() {
        return playlistName + " (" + getSongCount() + " songs)";
    }

    @Override
    public int compareTo(Playlist other) {
        if (other == null) return 1;
        // Sort by modification date (newest first)
        return Long.compare(other.dateModified, this.dateModified);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Playlist playlist = (Playlist) obj;
        return playlistId == playlist.playlistId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(playlistId);
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "playlistId=" + playlistId +
                ", playlistName='" + playlistName + '\'' +
                ", type=" + playlistType +
                ", songCount=" + getSongCount() +
                '}';
    }
}
