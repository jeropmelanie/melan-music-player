package com.example.melanmusicplayer.media;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;

import com.example.melanmusicplayer.MainActivity;
import com.example.melanmusicplayer.model.Song;
import com.example.melanmusicplayer.service.MusicService;

public class MediaSessionManager {
    private static final String TAG = "MediaSessionManager";
    private final MediaSessionCompat mediaSession;
    private final Context context;
    private final MusicService musicService;

    public MediaSessionManager(Context context, MusicService musicService) {
        this.context = context;
        this.musicService = musicService;

        // Create media session
        this.mediaSession = new MediaSessionCompat(context, "MelanMusicPlayer");
        
        // Set callback for media button handling
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                musicService.play();
            }

            @Override
            public void onPause() {
                super.onPause();
                musicService.pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                musicService.playNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                musicService.playPrevious();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                musicService.seekTo(pos);
            }

            @Override
            public void onStop() {
                super.onStop();
                musicService.stop();
            }
        });

        // Create intent to open MainActivity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);
        mediaSession.setSessionActivity(pendingIntent);

        // Enable media button handling
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | 
                              MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    /**
     * Update the media session with current song metadata
     */
    public void updateMetadata(Song song, Bitmap albumArt) {
        if (song == null) return;

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .build();

        mediaSession.setMetadata(metadata);
    }

    /**
     * Update playback state (playing, paused, etc)
     */
    public void updatePlaybackState(boolean isPlaying, long position, long duration) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | 
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SEEK_TO);

        stateBuilder.setState(state, position, 1.0f);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    /**
     * Get the media session token (used for notifications)
     */
    public MediaSessionCompat.Token getSessionToken() {
        return mediaSession.getSessionToken();
    }

    /**
     * Release resources
     */
    public void release() {
        mediaSession.release();
    }
}
