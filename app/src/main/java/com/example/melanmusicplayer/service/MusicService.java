package com.example.melanmusicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.melanmusicplayer.MainActivity;
import com.example.melanmusicplayer.R;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private static final String CHANNEL_ID = "melan_music_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaPlayer mediaPlayer;
    private MediaSession mediaSession;
    private NotificationManager notificationManager;
    private PlaybackListener playbackListener;

    private String currentTitle = "No Track";
    private String currentArtist = "Unknown Artist";
    private Bitmap currentAlbumArt;
    private boolean isPlaying = false;

    public interface PlaybackListener {
        void onPlaybackStateChanged(boolean isPlaying);
        void onTrackChanged(String title, String artist, Bitmap albumArt);
        void onPositionChanged(int position);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlayer();
        notificationManager = getSystemService(NotificationManager.class);
        createChannel();
        setupMediaSession();
    }

    private void initializePlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            if (playbackListener != null) {
                playbackListener.onPlaybackStateChanged(false);
            }
            updateNotification();
        });
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Melan Music",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupMediaSession() {
        mediaSession = new MediaSession(this, "MelanMusicSession");
        mediaSession.setActive(true);
    }

    public void loadTrack(String path, String title, String artist, Bitmap albumArt) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();

            currentTitle = title;
            currentArtist = artist;
            currentAlbumArt = albumArt;

            if (playbackListener != null) {
                playbackListener.onTrackChanged(title, artist, albumArt);
            }
            updateNotification();
        } catch (Exception e) {
            Log.e(TAG, "loadTrack error", e);
        }
    }

    public void play() {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                isPlaying = true;
                if (playbackListener != null) {
                    playbackListener.onPlaybackStateChanged(true);
                }
                startForeground(NOTIFICATION_ID, buildNotification());
            }
        } catch (Exception e) {
            Log.e(TAG, "play error", e);
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            if (playbackListener != null) {
                playbackListener.onPlaybackStateChanged(false);
            }
            stopForeground(false);
            updateNotification();
        }
    }

    public void seek(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
    }

    private void updateNotification() {
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_placeholder)
                .setContentTitle(currentTitle)
                .setContentText(currentArtist)
                .setContentIntent(openPending)
                .setLargeIcon(currentAlbumArt)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying)
                .setStyle(new MediaStyle().setMediaSession(mediaSession.getSessionToken()));

        builder.addAction(android.R.drawable.ic_media_previous, "Previous", null);
        builder.addAction(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                isPlaying ? "Pause" : "Play", null);
        builder.addAction(android.R.drawable.ic_media_next, "Next", null);

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}
