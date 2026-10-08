package com.example.melanmusicplayer.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.support.v4.media.session.MediaSessionCompat;

import com.example.melanmusicplayer.MainActivity;
import com.example.melanmusicplayer.R;
import com.example.melanmusicplayer.model.Song;
import com.example.melanmusicplayer.service.MusicService;

public class NotificationManager {
    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 42;

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final MediaSessionCompat.Token sessionToken;

    public NotificationManager(Context context, MediaSessionCompat.Token sessionToken) {
        this.context = context;
        this.sessionToken = sessionToken;
        this.notificationManager = NotificationManagerCompat.from(context);

        createNotificationChannel();
    }

    /**
     * Create notification channel (required for Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Now playing notifications");
            android.app.NotificationManager nm = 
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Build the now-playing notification
     */
    public Notification buildNotification(Song song, boolean isPlaying, Bitmap albumArt) {
        // Intent to open app when notification tapped
        Intent intent = new Intent(context, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        // Create notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_placeholder)
                .setLargeIcon(albumArt)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(isPlaying)
                .setStyle(new MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0, 1, 2));

        // Add playback controls
        builder.addAction(R.drawable.ic_prev, "Previous",
                getPendingIntent(context, "com.example.melanmusicplayer.PREV"));
        builder.addAction(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                isPlaying ? "Pause" : "Play",
                getPendingIntent(context, "com.example.melanmusicplayer.PLAY_PAUSE"));
        builder.addAction(R.drawable.ic_next, "Next",
                getPendingIntent(context, "com.example.melanmusicplayer.NEXT"));

        return builder.build();
    }

    /**
     * Helper to create action pending intents
     */
    private PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(action);
        intent.setClass(context, MusicService.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getService(context, action.hashCode(), intent, flags);
    }

    /**
     * Show the notification
     */
    public void showNotification(Notification notification) {
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Cancel the notification
     */
    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }

    public static String getChannelId() {
        return CHANNEL_ID;
    }
}
