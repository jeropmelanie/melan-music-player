package com.example.melanmusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.melanmusicplayer.model.PlaybackState;
import com.example.melanmusicplayer.model.Queue;
import com.example.melanmusicplayer.model.Song;

/**
 * Foreground service for music playback.
 * Handles play/pause/skip and maintains playback state.
 */
public class MusicService extends Service {
    private static final String TAG = "MusicService";

    private MediaPlayer mediaPlayer;
    private Queue queue;
    private PlaybackState playbackState;
    private final IBinder binder = new MusicBinder();
    private MusicServiceListener listener;

    public interface MusicServiceListener {
        void onPlaybackStateChanged(PlaybackState state);
        void onSongChanged(Song song);
        void onPositionChanged(long position);
        void onError(String error);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MusicService created");
        mediaPlayer = new MediaPlayer();
        queue = new Queue();
        playbackState = new PlaybackState();
        setupMediaPlayerListeners();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MusicService started");
        return START_STICKY;
    }

    private void setupMediaPlayerListeners() {
        mediaPlayer.setOnCompletionListener(mp -> {
            Log.d(TAG, "Song completed");
            playNextSong();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
            playbackState.setState(PlaybackState.State.ERROR);
            if (listener != null) {
                listener.onError("Playback error: " + what);
            }
            return true;
        });

        mediaPlayer.setOnPreparedListener(mp -> {
            Log.d(TAG, "MediaPlayer prepared");
            playbackState.setDuration(mediaPlayer.getDuration());
        });
    }

    public void loadSong(Song song) {
        if (song == null) {
            Log.w(TAG, "Attempted to load null song");
            return;
        }

        try {
            Log.d(TAG, "Loading song: " + song.getTitle());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepareAsync();

            playbackState.setCurrentSongId(song.getId());
            playbackState.setCurrentPosition(0);
            playbackState.setState(PlaybackState.State.LOADING);

            if (listener != null) {
                listener.onSongChanged(song);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading song", e);
            playbackState.setState(PlaybackState.State.ERROR);
            if (listener != null) {
                listener.onError("Failed to load: " + song.getTitle());
            }
        }
    }

    public void play() {
        if (mediaPlayer == null) return;

        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                playbackState.setState(PlaybackState.State.PLAYING);
                Log.d(TAG, "Playback started");

                if (listener != null) {
                    listener.onPlaybackStateChanged(playbackState);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing", e);
            playbackState.setState(PlaybackState.State.ERROR);
        }
    }

    public void pause() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playbackState.setState(PlaybackState.State.PAUSED);
                Log.d(TAG, "Playback paused");

                if (listener != null) {
                    listener.onPlaybackStateChanged(playbackState);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pausing", e);
        }
    }

    public void togglePlayPause() {
        if (playbackState.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            playbackState.setState(PlaybackState.State.STOPPED);
            playbackState.setCurrentPosition(0);
            Log.d(TAG, "Playback stopped");

            if (listener != null) {
                listener.onPlaybackStateChanged(playbackState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping", e);
        }
    }

    public void seekTo(long position) {
        if (mediaPlayer == null) return;

        try {
            mediaPlayer.seekTo((int) position);
            playbackState.setCurrentPosition(position);
            Log.d(TAG, "Seeked to: " + position);

            if (listener != null) {
                listener.onPositionChanged(position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error seeking", e);
        }
    }

    public void playNext() {
        Song nextSong = queue.moveNext();
        if (nextSong != null) {
            loadSong(nextSong);
            play();
        } else {
            Log.d(TAG, "No next song in queue");
        }
    }

    public void playPrevious() {
        Song previousSong = queue.movePrevious();
        if (previousSong != null) {
            loadSong(previousSong);
            play();
        } else {
            Log.d(TAG, "No previous song in queue");
        }
    }

    private void playNextSong() {
        switch (playbackState.getRepeatMode()) {
            case ONE:
                loadSong(queue.getCurrentSong());
                play();
                break;
            case ALL:
                if (queue.hasNext()) {
                    playNext();
                } else {
                    queue.setCurrentIndex(0);
                    Song firstSong = queue.getCurrentSong();
                    if (firstSong != null) {
                        loadSong(firstSong);
                        play();
                    }
                }
                break;
            case NONE:
            default:
                if (queue.hasNext()) {
                    playNext();
                } else {
                    stop();
                }
                break;
        }
    }

    public void setQueue(Queue newQueue) {
        this.queue = newQueue;
        Log.d(TAG, "Queue set with " + queue.size() + " songs");
    }

    public Queue getQueue() {
        return queue;
    }

    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    public Song getCurrentSong() {
        return queue.getCurrentSong();
    }

    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                Log.e(TAG, "Error getting current position", e);
            }
        }
        return 0;
    }

    public long getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (Exception e) {
                Log.e(TAG, "Error getting duration", e);
            }
        }
        return 0;
    }

    public void setListener(MusicServiceListener listener) {
        this.listener = listener;
    }

    public void setRepeatMode(PlaybackState.RepeatMode mode) {
        playbackState.setRepeatMode(mode);
        Log.d(TAG, "Repeat mode set to: " + mode);
    }

    public void toggleShuffle() {
        playbackState.toggleShuffle();
        Log.d(TAG, "Shuffle toggled to: " + playbackState.getShuffleMode());
    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            float clamped = Math.max(0.0f, Math.min(1.0f, volume));
            mediaPlayer.setVolume(clamped, clamped);
            playbackState.setVolume(clamped);
            Log.d(TAG, "Volume set to: " + clamped);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MusicService destroyed");

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}


