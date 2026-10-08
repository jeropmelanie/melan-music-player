package com.example.melanmusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.melanmusicplayer.model.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class QueueStateStore {
    private static final String PREF_NAME = "queue_state";
    private static final String KEY_SONGS = "songs";
    private static final String KEY_CURRENT_INDEX = "current_index";
    private static final String KEY_LAST_POSITION = "last_position";

    private final SharedPreferences prefs;
    private final Gson gson;

    public QueueStateStore(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveQueue(ArrayList<Song> songs, int currentIndex, long lastPosition) {
        String json = gson.toJson(songs);
        prefs.edit()
                .putString(KEY_SONGS, json)
                .putInt(KEY_CURRENT_INDEX, currentIndex)
                .putLong(KEY_LAST_POSITION, lastPosition)
                .apply();
    }

    public ArrayList<Song> loadSongs() {
        String json = prefs.getString(KEY_SONGS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Song>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public int loadCurrentIndex() {
        return prefs.getInt(KEY_CURRENT_INDEX, 0);
    }

    public long loadLastPosition() {
        return prefs.getLong(KEY_LAST_POSITION, 0L);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
