package com.example.melanmusicplayer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.melanmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scanner for audio files on device storage.
 * Reads from MediaStore and extracts song metadata.
 */
public class AudioScanner {
    private static final String TAG = "AudioScanner";

    // Audio file MIME types supported
    private static final String[] AUDIO_MIME_TYPES = {
            "audio/mpeg",           // MP3
            "audio/ogg",            // OGG
            "audio/flac",           // FLAC
            "audio/wav",            // WAV
            "audio/aac",            // AAC
            "audio/mp4",            // M4A
            "audio/x-m4a"           // M4A variant
    };

    private Context context;
    private ContentResolver contentResolver;

    public AudioScanner(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    /**
     * Scan device for audio files and return list of songs.
     */
    public List<Song> scanAudio() {
        List<Song> songs = new ArrayList<>();
        Log.d(TAG, "Starting audio scan...");

        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.DATA,           // File path
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATE_MODIFIED,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.SIZE,           // File size in bytes
                    MediaStore.Audio.Media.TRACK
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            Cursor cursor = contentResolver.query(
                    uri,
                    projection,
                    selection,
                    null,
                    sortOrder
            );

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);

                while (cursor.moveToNext()) {
                    try {
                        Song song = new Song();
                        song.setId(cursor.getLong(idColumn));
                        song.setTitle(cursor.getString(titleColumn));
                        song.setArtist(cursor.getString(artistColumn));
                        song.setAlbum(cursor.getString(albumColumn));
                        song.setAlbumId(cursor.getLong(albumIdColumn));
                        song.setArtistId(cursor.getLong(artistIdColumn));
                        song.setFilePath(cursor.getString(dataColumn));
                        song.setDuration(cursor.getLong(durationColumn));
                        song.setDateAdded(cursor.getLong(dateAddedColumn) * 1000); // Convert to ms
                        song.setDateModified(cursor.getLong(dateModifiedColumn) * 1000);
                        song.setMimeType(cursor.getString(mimeTypeColumn));
                        song.setFileSize(cursor.getLong(sizeColumn));
                        song.setTrackNumber(cursor.getInt(trackColumn));

                        songs.add(song);
                        Log.d(TAG, "Added: " + song.getTitle() + " by " + song.getArtist());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing song from cursor", e);
                    }
                }

                cursor.close();
                Log.d(TAG, "Audio scan completed. Found " + songs.size() + " songs");
            } else {
                Log.w(TAG, "Cursor is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning audio", e);
        }

        return songs;
    }

    /**
     * Get all songs sorted by title
     */
    public List<Song> getAllSongs() {
        List<Song> songs = scanAudio();
        Collections.sort(songs);
        return songs;
    }

    /**
     * Get all songs sorted by artist
     */
    public List<Song> getAllSongsByArtist() {
        List<Song> songs = scanAudio();
        Collections.sort(songs, (a, b) -> {
            int artistComparison = a.getArtist().compareToIgnoreCase(b.getArtist());
            if (artistComparison != 0) return artistComparison;
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        });
        return songs;
    }

    /**
     * Get all songs sorted by album
     */
    public List<Song> getAllSongsByAlbum() {
        List<Song> songs = scanAudio();
        Collections.sort(songs, (a, b) -> {
            int albumComparison = a.getAlbum().compareToIgnoreCase(b.getAlbum());
            if (albumComparison != 0) return albumComparison;
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        });
        return songs;
    }

    /**
     * Search songs by query
     */
    public List<Song> search(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        List<Song> allSongs = scanAudio();
        List<Song> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Song song : allSongs) {
            if (song.getTitle().toLowerCase().contains(lowerQuery) ||
                    song.getArtist().toLowerCase().contains(lowerQuery) ||
                    song.getAlbum().toLowerCase().contains(lowerQuery)) {
                results.add(song);
            }
        }

        Log.d(TAG, "Search for '" + query + "' found " + results.size() + " songs");
        return results;
    }

    /**
     * Get songs by artist
     */
    public List<Song> getSongsByArtist(String artist) {
        if (artist == null || artist.isEmpty()) {
            return new ArrayList<>();
        }

        List<Song> allSongs = scanAudio();
        List<Song> results = new ArrayList<>();

        for (Song song : allSongs) {
            if (song.getArtist().equalsIgnoreCase(artist)) {
                results.add(song);
            }
        }

        Log.d(TAG, "Found " + results.size() + " songs by " + artist);
        return results;
    }

    /**
     * Get songs by album
     */
    public List<Song> getSongsByAlbum(String album) {
        if (album == null || album.isEmpty()) {
            return new ArrayList<>();
        }

        List<Song> allSongs = scanAudio();
        List<Song> results = new ArrayList<>();

        for (Song song : allSongs) {
            if (song.getAlbum().equalsIgnoreCase(album)) {
                results.add(song);
            }
        }

        Log.d(TAG, "Found " + results.size() + " songs in album " + album);
        return results;
    }
}
