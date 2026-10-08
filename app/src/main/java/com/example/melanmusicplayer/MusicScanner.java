package com.example.melanmusicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MusicScanner {
    private static final String TAG = "MusicScanner";
    private final ArrayList<Song> songs = new ArrayList<>();

    public MusicScanner(Context context) {
    }

    public ArrayList<Song> scanMusicFiles() {
        songs.clear();

        File musicDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_MUSIC);
        if (musicDir != null && musicDir.exists()) {
            scanDirectory(musicDir);
        }

        File fallback = new File("/storage/emulated/0/Music");
        if (fallback.exists()) {
            scanDirectory(fallback);
        }

        Collections.sort(songs, (a, b) -> a.title.compareToIgnoreCase(b.title));
        return songs;
    }

    private void scanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                scanDirectory(file);
            } else if (file.isFile()) {
                String name = file.getName().toLowerCase(Locale.US);
                if (isAudioFile(name)) {
                    Song song = new Song(getFileTitle(file.getName()), file.getAbsolutePath(), file.getName());
                    extractMetadata(song);
                    songs.add(song);
                }
            }
        }
    }

    private boolean isAudioFile(String filename) {
        return filename.endsWith(".mp3") || filename.endsWith(".wav") ||
               filename.endsWith(".m4a") || filename.endsWith(".aac") ||
               filename.endsWith(".flac") || filename.endsWith(".ogg");
    }

    private String getFileTitle(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    private void extractMetadata(Song song) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(song.path);

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            if (title != null && !title.isEmpty()) {
                song.title = title;
            }
            song.artist = (artist != null && !artist.isEmpty()) ? artist : "Unknown Artist";
            song.album = (album != null && !album.isEmpty()) ? album : "Unknown Album";

            if (duration != null) {
                try {
                    song.duration = Long.parseLong(duration);
                } catch (NumberFormatException e) {
                    song.duration = 0;
                }
            }

            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null) {
                song.albumArt = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            }

            retriever.release();
        } catch (Exception e) {
            Log.e(TAG, "Error extracting metadata for " + song.path, e);
            song.artist = "Unknown Artist";
            song.album = "Unknown Album";
        }
    }

    public static class Song {
        public String title;
        public final String path;
        public final String filename;
        public String artist;
        public String album;
        public long duration;
        public Bitmap albumArt;
        public boolean isFavorite;

        public Song(String title, String path, String filename) {
            this.title = title;
            this.path = path;
            this.filename = filename;
            this.artist = "Unknown Artist";
            this.album = "Unknown Album";
            this.duration = 0;
            this.albumArt = null;
            this.isFavorite = false;
        }
    }
}
