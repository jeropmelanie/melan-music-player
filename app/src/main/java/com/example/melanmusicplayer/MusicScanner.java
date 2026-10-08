package com.example.melanmusicplayer;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MusicScanner {
    private final Context context;
    private final ArrayList<Song> songs = new ArrayList<>();

    public MusicScanner(Context context) {
        this.context = context;
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
                    String title = getFileTitle(file.getName());
                    songs.add(new Song(title, file.getAbsolutePath(), file.getName()));
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

    public static class Song {
        public final String title;
        public final String path;
        public final String filename;
        public boolean isPlaying;

        public Song(String title, String path, String filename) {
            this.title = title;
            this.path = path;
            this.filename = filename;
            this.isPlaying = false;
        }
    }
}
