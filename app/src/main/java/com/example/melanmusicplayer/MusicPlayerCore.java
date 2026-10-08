package com.example.melanmusicplayer;

import android.media.MediaPlayer;
import android.util.Log;

public class MusicPlayerCore {
    private static final String TAG = "MusicPlayerCore";

    private final MediaPlayer mediaPlayer;
    private PlayerListener listener;
    private int currentDuration = 0;

    public interface PlayerListener {
        void onStateChanged(PlayerState state);
        void onDurationChanged(int duration);
        void onPositionChanged(int position);
        void onError(String message);
    }

    public enum PlayerState {
        IDLE, PLAYING, PAUSED, STOPPED
    }

    public MusicPlayerCore() {
        mediaPlayer = new MediaPlayer();
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        mediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void loadTrack(String path) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            currentDuration = mediaPlayer.getDuration();
            if (listener != null) {
                listener.onDurationChanged(currentDuration);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load track: " + path, e);
            if (listener != null) {
                listener.onError("Unable to load track");
            }
        }
    }

    public void play() {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                if (listener != null) {
                    listener.onStateChanged(PlayerState.PLAYING);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Play error", e);
            if (listener != null) {
                listener.onError("Playback error");
            }
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (listener != null) {
                listener.onStateChanged(PlayerState.PAUSED);
            }
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (listener != null) {
            listener.onStateChanged(PlayerState.STOPPED);
        }
    }

    public void seek(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return currentDuration;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
