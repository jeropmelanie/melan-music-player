package com.example.melanmusicplayer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a music album - collection of songs by same artist.
 */
public class Album implements Serializable, Comparable<Album> {
    private static final long serialVersionUID = 1L;

    private long albumId;
    private String albumName;
    private String artist;
    private long artistId;
    private String albumArt;            // Path to album artwork/thumbnail
    private int year;
    private long dateAdded;
    private List<Song> songs;
    private int songCount;              // Total songs in album
    private boolean isFavorite;

    // Constructors
    public Album() {
        this.songs = new ArrayList<>();
        this.isFavorite = false;
        this.songCount = 0;
    }

    public Album(long albumId, String albumName, String artist, int year) {
        this();
        this.albumId = albumId;
        this.albumName = albumName;
        this.artist = artist;
        this.year = year;
    }

    // Getters and Setters
    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        this.songCount = songs != null ? songs.size() : 0;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // Utility methods
    /**
     * Add a song to the album
     */
    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
            songCount = songs.size();
        }
    }

    /**
     * Remove a song from the album
     */
    public void removeSong(Song song) {
        if (song != null) {
            songs.remove(song);
            songCount = songs.size();
        }
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
     * Get display info
     */
    public String getDisplayInfo() {
        return albumName + " - " + artist + " (" + songCount + " songs)";
    }

    @Override
    public int compareTo(Album other) {
        if (other == null) return 1;
        // Sort by album name, then artist
        int albumComparison = this.albumName.compareToIgnoreCase(other.albumName);
        if (albumComparison != 0) {
            return albumComparison;
        }
        return this.artist.compareToIgnoreCase(other.artist);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Album album = (Album) obj;
        return albumId == album.albumId && albumName.equals(album.albumName);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(albumId);
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                ", songCount=" + songCount +
                '}';
    }
}
