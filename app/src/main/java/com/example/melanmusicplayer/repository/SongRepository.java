package com.example.melanmusicplayer.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class SongRepository {
    private static final String TAG = "SongRepository";
    private final ContentResolver contentResolver;

    public SongRepository(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    Song song = new Song(title, path, artist, album, duration);
                    extractAlbumArt(song);
                    songs.add(song);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading songs", e);
        }

        return songs;
    }

    private void extractAlbumArt(Song song) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(song.path);
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            if (albumArtBytes != null) {
                song.albumArt = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
            }
            retriever.release();
        } catch (Exception e) {
            Log.e(TAG, "Error extracting album art", e);
        }
    }

    public static class Song {
        public String title;
        public String path;
        public String artist;
        public String album;
        public long duration;
        public Bitmap albumArt;
        public boolean isFavorite;

        public Song(String title, String path, String artist, String album, long duration) {
            this.title = title;
            this.path = path;
            this.artist = artist;
            this.album = album;
            this.duration = duration;
            this.isFavorite = false;
        }
    }
}
