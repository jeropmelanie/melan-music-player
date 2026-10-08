package com.example.melanmusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.example.melanmusicplayer.model.PlaybackState;
import com.example.melanmusicplayer.model.Queue;
import com.example.melanmusicplayer.model.Song;
import com.example.melanmusicplayer.service.MusicService;
import com.example.melanmusicplayer.util.AudioScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MusicService.MusicServiceListener {

    private static final int REQ_AUDIO_PERMISSION = 1001;

    private MusicService musicService;
    private boolean isBound = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.getPlaybackState() != null && musicService.getPlaybackState().isPlaying()) {
                long pos = musicService.getCurrentPosition();
                if (!isUserSeeking) {
                    seekBar.setProgress((int) pos);
                    currentTimeText.setText(formatTime(pos));
                }
                handler.postDelayed(this, 500);
            }
        }
    };

    private ListView playlistView;
    private SeekBar seekBar;
    private SeekBar volumeSlider;
    private TextView currentSongText;
    private TextView artistText;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private ImageView albumArtView;
    private ImageButton prevButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;
    private ImageButton favoriteButton;

    private ArrayAdapter<String> songAdapter;
    private final ArrayList<String> songLabels = new ArrayList<>();
    private final ArrayList<Song> songs = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isUserSeeking = false;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            musicService.setListener(MainActivity.this);

            if (!songs.isEmpty()) {
                musicService.setQueue(new Queue(new ArrayList<>(songs)));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        bindViews();
        setupListeners();
        setupAdapter();

        if (hasAudioPermission()) {
            loadSongs();
        } else {
            requestAudioPermission();
        }

        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    private void bindViews() {
        playlistView = findViewById(R.id.playlistView);
        seekBar = findViewById(R.id.seekBar);
        volumeSlider = findViewById(R.id.volumeSlider);
        currentSongText = findViewById(R.id.currentSongText);
        artistText = findViewById(R.id.artistText);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        albumArtView = findViewById(R.id.albumArtView);
        prevButton = findViewById(R.id.prevButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        shuffleButton = findViewById(R.id.shuffleButton);
        repeatButton = findViewById(R.id.repeatButton);
        favoriteButton = findViewById(R.id.favoriteButton);

        volumeSlider.setMax(15);
        volumeSlider.setProgress(10);
        albumArtView.setImageResource(R.drawable.ic_music_placeholder);
    }

    private void setupAdapter() {
        songAdapter = new ArrayAdapter<>(this, R.layout.list_item_song, android.R.id.text1, songLabels);
        playlistView.setAdapter(songAdapter);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNext());
        prevButton.setOnClickListener(v -> playPrevious());
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        repeatButton.setOnClickListener(v -> toggleRepeat());
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { isUserSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { isUserSeeking = false; }
        });

        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null) {
                    musicService.setVolume(progress / 15f);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        playlistView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < songs.size()) {
                playSongAt(position);
            }
        });
    }

    private boolean hasAudioPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        ActivityCompat.requestPermissions(this, new String[]{permission}, REQ_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Audio permission is required to load songs", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSongs() {
        new Thread(() -> {
            List<Song> scanned = new AudioScanner(this).getAllSongs();
            runOnUiThread(() -> {
                songs.clear();
                songs.addAll(scanned);
                refreshSongList();

                if (!songs.isEmpty()) {
                    Queue queue = new Queue(new ArrayList<>(songs));
                    if (musicService != null) {
                        musicService.setQueue(queue);
                    }
                    playSongAt(0);
                } else {
                    Toast.makeText(this, "No songs found", Toast.LENGTH_LONG).show();
                    currentSongText.setText("No Track");
                    artistText.setText("Unknown Artist");
                }
            });
        }).start();
    }

    private void refreshSongList() {
        songLabels.clear();
        for (Song song : songs) {
            songLabels.add(song.getTitle() + " - " + song.getArtist());
        }
        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }

    private void playSongAt(int position) {
        if (position < 0 || position >= songs.size()) {
            return;
        }

        currentIndex = position;
        Song selected = songs.get(position);

        if (musicService != null) {
            Queue queue = new Queue(new ArrayList<>(songs));
            musicService.setQueue(queue);
            musicService.loadSong(selected);
            musicService.play();
        }

        updateNowPlaying(selected);
    }

    private void updateNowPlaying(Song song) {
        if (song == null) {
            return;
        }

        currentSongText.setText(song.getTitle());
        artistText.setText(song.getArtist());
        seekBar.setMax((int) song.getDuration());
        totalTimeText.setText(formatTime(song.getDuration()));
        currentTimeText.setText("00:00");
        albumArtView.setImageResource(R.drawable.ic_music_placeholder);

        if (musicService != null && musicService.getPlaybackState() != null) {
            playPauseButton.setImageResource(musicService.getPlaybackState().isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void togglePlayPause() {
        if (musicService == null || songs.isEmpty()) {
            Toast.makeText(this, "No audio loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (musicService.getPlaybackState() != null && musicService.getPlaybackState().isPlaying()) {
            musicService.pause();
        } else {
            if (musicService.getCurrentSong() == null) {
                playSongAt(currentIndex);
            } else {
                musicService.play();
            }
        }
    }

    private void playNext() {
        if (songs.isEmpty()) return;
        int nextIndex = shuffleEnabled ? randomIndex() : (currentIndex + 1) % songs.size();
        currentIndex = nextIndex;
        playSongAt(currentIndex);
    }

    private void playPrevious() {
        if (songs.isEmpty()) return;
        int previousIndex = (currentIndex - 1 + songs.size()) % songs.size();
        currentIndex = previousIndex;
        playSongAt(currentIndex);
    }

    private int randomIndex() {
        return (int) (Math.random() * songs.size());
    }

    private void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        shuffleButton.setColorFilter(shuffleEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, shuffleEnabled ? "Shuffle on" : "Shuffle off", Toast.LENGTH_SHORT).show();
    }

    private void toggleRepeat() {
        repeatEnabled = !repeatEnabled;
        repeatButton.setColorFilter(repeatEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);

        if (musicService != null) {
            musicService.setRepeatMode(repeatEnabled ? PlaybackState.RepeatMode.ALL : PlaybackState.RepeatMode.NONE);
        }

        Toast.makeText(this, repeatEnabled ? "Repeat on" : "Repeat off", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        if (currentIndex < 0 || currentIndex >= songs.size()) return;

        Song song = songs.get(currentIndex);
        song.setFavorite(!song.isFavorite());
        favoriteButton.setImageResource(song.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        Toast.makeText(this, song.isFavorite() ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        if (state == null) return;

        playPauseButton.setImageResource(state.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        if (state.isPlaying()) {
            handler.post(progressUpdater);
        } else {
            handler.removeCallbacks(progressUpdater);
        }

        if (state.getDuration() > 0) {
            seekBar.setMax((int) state.getDuration());
            totalTimeText.setText(formatTime(state.getDuration()));
        }
    }

    @Override
    public void onSongChanged(Song song) {
        if (song != null) {
            updateNowPlaying(song);
        }
    }

    @Override
    public void onPositionChanged(long position) {
        if (!isUserSeeking) {
            seekBar.setProgress((int) position);
            currentTimeText.setText(formatTime(position));
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, error != null ? error : "Playback error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressUpdater);

        if (isBound && musicService != null) {
            musicService.setListener(null);
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
