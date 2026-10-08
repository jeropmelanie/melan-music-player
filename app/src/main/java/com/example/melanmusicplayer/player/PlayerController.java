package com.example.melanmusicplayer.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.melanmusicplayer.repository.SongRepository;
import com.example.melanmusicplayer.service.MusicService;

public class PlayerController {
    private final Context context;
    private MusicService musicService;
    private final SongRepository songRepository;
    private boolean bound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public PlayerController(Context context) {
        this.context = context;
        this.songRepository = new SongRepository(context);
        bind();
    }

    private void bind() {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void loadTrack(SongRepository.Song song) {
        if (bound && musicService != null) {
            musicService.loadTrack(song.path, song.title, song.artist, song.albumArt);
        }
    }

    public void play() {
        if (bound && musicService != null) {
            musicService.play();
        }
    }

    public void pause() {
        if (bound && musicService != null) {
            musicService.pause();
        }
    }

    public void seek(int progress) {
        if (bound && musicService != null) {
            musicService.seek(progress);
        }
    }

    public void setVolume(float volume) {
        if (bound && musicService != null) {
            musicService.setVolume(volume);
        }
    }

    public int getCurrentPosition() {
        return bound && musicService != null ? musicService.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return bound && musicService != null ? musicService.getDuration() : 0;
    }

    public boolean isPlaying() {
        return bound && musicService != null && musicService.isPlaying();
    }

    public void setPlaybackListener(MusicService.PlaybackListener listener) {
        if (bound && musicService != null) {
            musicService.setPlaybackListener(listener);
        }
    }

    public void unbind() {
        if (bound) {
            context.unbindService(serviceConnection);
            bound = false;
        }
    }

    public SongRepository getSongRepository() {
        return songRepository;
    }
}
