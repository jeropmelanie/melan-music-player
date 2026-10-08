package com.example.melanmusicplayer.model;

import java.io.Serializable;

/**
 * Represents a single audio track/song in the music library.
 * This is a core domain model for the music player.
 */
public class Song implements Serializable, Comparable<Song> {
    private static final long serialVersionUID = 1L;

    private long id;                    // Unique identifier from MediaStore
    private String title;
    private String artist;
    private String album;
    private long albumId;
    private long artistId;
    private String filePath;            // Full file path on device
    private long duration;              // Duration in milliseconds
    private long dateAdded;             // When added to library
    private long dateModified;
    private int bitrate;                // Audio bitrate
    private String mimeType;            // File format (mp3, flac, etc)
    private long fileSize;              // File size in bytes
    private int trackNumber;            // Track number in album
    private int year;                   // Release year
    private boolean isFavorite;         // User favorite flag
    private long lastPlayedTime;        // Last played timestamp
    private int playCount;              // Total play count
    private long playbackPosition;      // Resume position in ms

    // Constructors
    public Song() {
        this.isFavorite = false;
        this.playCount = 0;
        this.playbackPosition = 0;
        this.lastPlayedTime = 0;
    }

    public Song(long id, String title, String artist, String album, String filePath, long duration) {
        this();
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public long getLastPlayedTime() {
        return lastPlayedTime;
    }

    public void setLastPlayedTime(long lastPlayedTime) {
        this.lastPlayedTime = lastPlayedTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }

    public long getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(long playbackPosition) {
        this.playbackPosition = playbackPosition;
    }

    // Utility methods
    /**
     * Get formatted duration string (HH:mm:ss or mm:ss)
     */
    public String getFormattedDuration() {
        long seconds = duration / 1000;
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
     * Get display name (title - artist)
     */
    public String getDisplayName() {
        return title + " - " + artist;
    }

    /**
     * Check if file exists on device
     */
    public boolean fileExists() {
        return filePath != null && !filePath.isEmpty();
    }

    @Override
    public int compareTo(Song other) {
        if (other == null) return 1;
        // Sort by title, then artist
        int titleComparison = this.title.compareToIgnoreCase(other.title);
        if (titleComparison != 0) {
            return titleComparison;
        }
        return this.artist.compareToIgnoreCase(other.artist);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Song song = (Song) obj;
        return id == song.id && filePath.equals(song.filePath);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", isFavorite=" + isFavorite +
                ", playCount=" + playCount +
                '}';
    }
}
