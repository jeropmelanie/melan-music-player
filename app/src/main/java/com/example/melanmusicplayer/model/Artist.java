package com.example.melanmusicplayer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a music artist - collection of songs/albums from same artist.
 */
public class Artist implements Serializable, Comparable<Artist> {
    private static final long serialVersionUID = 1L;

    private long artistId;
    private String artistName;
    private String bio;                 // Artist biography
    private String artistImage;         // Path to artist image
    private long dateAdded;
    private List<Song> songs;
    private List<Album> albums;
    private int songCount;
    private int albumCount;
    private boolean isFavorite;

    // Constructors
    public Artist() {
        this.songs = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.isFavorite = false;
        this.songCount = 0;
        this.albumCount = 0;
    }

    public Artist(long artistId, String artistName) {
        this();
        this.artistId = artistId;
        this.artistName = artistName;
    }

    // Getters and Setters
    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getArtistImage() {
        return artistImage;
    }

    public void setArtistImage(String artistImage) {
        this.artistImage = artistImage;
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

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
        this.albumCount = albums != null ? albums.size() : 0;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // Utility methods
    /**
     * Add a song to artist's collection
     */
    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
            songCount = songs.size();
        }
    }

    /**
     * Remove a song from artist's collection
     */
    public void removeSong(Song song) {
        if (song != null) {
            songs.remove(song);
            songCount = songs.size();
        }
    }

    /**
     * Add an album to artist's collection
     */
    public void addAlbum(Album album) {
        if (album != null && !albums.contains(album)) {
            albums.add(album);
            albumCount = albums.size();
        }
    }

    /**
     * Remove an album from artist's collection
     */
    public void removeAlbum(Album album) {
        if (album != null) {
            albums.remove(album);
            albumCount = albums.size();
        }
    }

    /**
     * Get total duration of all songs by artist
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
        return artistName + " (" + albumCount + " albums, " + songCount + " songs)";
    }

    @Override
    public int compareTo(Artist other) {
        if (other == null) return 1;
        return this.artistName.compareToIgnoreCase(other.artistName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Artist artist = (Artist) obj;
        return artistId == artist.artistId && artistName.equals(artist.artistName);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(artistId);
    }

    @Override
    public String toString() {
        return "Artist{" +
                "artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", songCount=" + songCount +
                ", albumCount=" + albumCount +
                '}';
    }
}
