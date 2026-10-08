package com.example.melanmusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

public class QueueStore {
    private static final String PREFS_NAME = "melan_music_queue";
    private static final String KEY_QUEUE = "queue_json";
    private static final String KEY_INDEX = "queue_index";

    private final SharedPreferences prefs;

    public QueueStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(String queueJson, int index) {
        prefs.edit().putString(KEY_QUEUE, queueJson).putInt(KEY_INDEX, index).apply();
    }

    public String getQueueJson() {
        return prefs.getString(KEY_QUEUE, "");
    }

    public int getCurrentIndex() {
        return prefs.getInt(KEY_INDEX, 0);
    }
}
