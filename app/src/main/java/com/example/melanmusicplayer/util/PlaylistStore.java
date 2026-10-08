package com.example.melanmusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.melanmusicplayer.model.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlaylistStore {
    private static final String PREF_NAME = "playlist_store";
    private static final String KEY_PLAYLISTS = "playlists";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PlaylistStore(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void savePlaylists(ArrayList<Playlist> playlists) {
        String json = gson.toJson(playlists);
        prefs.edit().putString(KEY_PLAYLISTS, json).apply();
    }

    public ArrayList<Playlist> loadPlaylists() {
        String json = prefs.getString(KEY_PLAYLISTS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Playlist>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveSinglePlaylist(Playlist playlist) {
        ArrayList<Playlist> playlists = loadPlaylists();
        boolean found = false;

        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().equalsIgnoreCase(playlist.getName())) {
                playlists.set(i, playlist);
                found = true;
                break;
            }
        }

        if (!found) {
            playlists.add(playlist);
        }

        savePlaylists(playlists);
    }

    public void deletePlaylist(String name) {
        ArrayList<Playlist> playlists = loadPlaylists();
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().equalsIgnoreCase(name)) {
                playlists.remove(i);
                break;
            }
        }
        savePlaylists(playlists);
    }

    public List<String> getPlaylistNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Playlist playlist : loadPlaylists()) {
            names.add(playlist.getName());
        }
        return names;
    }
}
