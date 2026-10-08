package com.example.melanmusicplayer.model;

import java.io.Serializable;

/**
 * Represents the current playback state of the music player.
 */
public class PlaybackState implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum State {
        IDLE,              // No song loaded
        PLAYING,           // Currently playing
        PAUSED,            // Paused
        STOPPED,           // Stopped (different from paused)
        LOADING,           // Loading audio file
        ERROR              // Error state
    }

    public enum RepeatMode {
        NONE,              // No repeat
        ALL,               // Repeat all songs in queue
        ONE                // Repeat current song
    }

    public enum ShuffleMode {
        OFF,               // Sequential playback
        ON                 // Random order
    }

    private State state;
    private long currentSongId;
    private long currentPosition;      // Current playback position in ms
    private long duration;             // Total song duration in ms
    private int currentQueueIndex;     // Current index in queue
    private RepeatMode repeatMode;
    private ShuffleMode shuffleMode;
    private float volume;              // Volume level 0.0 to 1.0
    private boolean isMuted;
    private long timestamp;            // When this state was created

    // Constructors
    public PlaybackState() {
        this.state = State.IDLE;
        this.currentSongId = -1;
        this.currentPosition = 0;
        this.duration = 0;
        this.currentQueueIndex = 0;
        this.repeatMode = RepeatMode.NONE;
        this.shuffleMode = ShuffleMode.OFF;
        this.volume = 1.0f;
        this.isMuted = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    public long getCurrentSongId() {
        return currentSongId;
    }

    public void setCurrentSongId(long currentSongId) {
        this.currentSongId = currentSongId;
        this.timestamp = System.currentTimeMillis();
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCurrentQueueIndex() {
        return currentQueueIndex;
    }

    public void setCurrentQueueIndex(int currentQueueIndex) {
        this.currentQueueIndex = currentQueueIndex;
        this.timestamp = System.currentTimeMillis();
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        this.timestamp = System.currentTimeMillis();
    }

    public ShuffleMode getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(ShuffleMode shuffleMode) {
        this.shuffleMode = shuffleMode;
        this.timestamp = System.currentTimeMillis();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Utility methods
    /**
     * Check if player is currently playing
     */
    public boolean isPlaying() {
        return state == State.PLAYING;
    }

    /**
     * Check if player is paused
     */
    public boolean isPaused() {
        return state == State.PAUSED;
    }

    /**
     * Check if player is idle/stopped
     */
    public boolean isStopped() {
        return state == State.IDLE || state == State.STOPPED;
    }

    /**
     * Get progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (duration == 0) return 0;
        return (int) ((currentPosition * 100) / duration);
    }

    /**
     * Get formatted current position
     */
    public String getFormattedCurrentPosition() {
        return formatTime(currentPosition);
    }

    /**
     * Get formatted duration
     */
    public String getFormattedDuration() {
        return formatTime(duration);
    }

    /**
     * Format time from milliseconds
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Cycle through repeat modes
     */
    public RepeatMode cycleRepeatMode() {
        switch (repeatMode) {
            case NONE:
                setRepeatMode(RepeatMode.ALL);
                return RepeatMode.ALL;
            case ALL:
                setRepeatMode(RepeatMode.ONE);
                return RepeatMode.ONE;
            case ONE:
                setRepeatMode(RepeatMode.NONE);
                return RepeatMode.NONE;
            default:
                return repeatMode;
        }
    }

    /**
     * Toggle shuffle mode
     */
    public void toggleShuffle() {
        if (shuffleMode == ShuffleMode.OFF) {
            setShuffleMode(ShuffleMode.ON);
        } else {
            setShuffleMode(ShuffleMode.OFF);
        }
    }

    @Override
    public String toString() {
        return "PlaybackState{" +
                "state=" + state +
                ", currentSongId=" + currentSongId +
                ", currentPosition=" + currentPosition +
                ", duration=" + duration +
                ", repeatMode=" + repeatMode +
                ", shuffleMode=" + shuffleMode +
                ", volume=" + volume +
                ", isMuted=" + isMuted +
                '}';
    }
}
