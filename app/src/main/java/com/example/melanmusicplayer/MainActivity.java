package com.example.melanmusicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MusicPlayerCore.PlayerListener {

    private static final int REQ_STORAGE = 101;
    private static final String TAG = "MelanMusicPlayer";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (playerCore.isPlaying()) {
                int pos = playerCore.getCurrentPosition();
                if (!isUserSeeking) {
                    seekBar.setProgress(pos);
                    currentTimeText.setText(formatTime(pos));
                }
                handler.postDelayed(this, 500);
            }
        }
    };

    private MusicPlayerCore playerCore;
    private PlaylistManager playlistManager;
    private MusicScanner scanner;

    private ListView playlistView;
    private SeekBar seekBar;
    private TextView currentSongText;
    private TextView artistText;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private ImageButton prevButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;

    private ArrayAdapter<String> playlistAdapter;
    private boolean isUserSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        playerCore = new MusicPlayerCore();
        playerCore.setListener(this);
        playlistManager = new PlaylistManager();
        scanner = new MusicScanner(this);

        bindViews();
        setupListeners();

        if (hasStoragePermission()) {
            loadMusicLibrary();
        } else {
            requestStoragePermission();
        }
    }

    private void bindViews() {
        playlistView = findViewById(R.id.playlistView);
        seekBar = findViewById(R.id.seekBar);
        currentSongText = findViewById(R.id.currentSongText);
        artistText = findViewById(R.id.artistText);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        prevButton = findViewById(R.id.prevButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        shuffleButton = findViewById(R.id.shuffleButton);
        repeatButton = findViewById(R.id.repeatButton);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNext());
        prevButton.setOnClickListener(v -> playPrevious());
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        repeatButton.setOnClickListener(v -> toggleRepeat());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerCore.seek(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { isUserSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { isUserSeeking = false; }
        });

        playlistView.setOnItemClickListener((parent, view, position, id) -> {
            playlistManager.setSongAt(position);
            playCurrentSong();
        });
    }

    private void loadMusicLibrary() {
        new Thread(() -> {
            ArrayList<MusicScanner.Song> songs = scanner.scanMusicFiles();
            runOnUiThread(() -> {
                if (songs.isEmpty()) {
                    Toast.makeText(this, "No music files found in Music folder.", Toast.LENGTH_LONG).show();
                    return;
                }

                playlistManager.setPlaylist(songs);
                updatePlaylistList();
                playCurrentSong();
            });
        }).start();
    }

    private void updatePlaylistList() {
        ArrayList<String> names = new ArrayList<>();
        for (MusicScanner.Song song : playlistManager.getPlaylist()) {
            names.add(song.title);
        }
        playlistAdapter = new ArrayAdapter<>(this, R.layout.list_item_song, names);
        playlistView.setAdapter(playlistAdapter);
    }

    private void playCurrentSong() {
        MusicScanner.Song song = playlistManager.getCurrentSong();
        if (song == null) return;

        playerCore.loadTrack(song.path);
        playerCore.play();

        currentSongText.setText(song.title);
        artistText.setText("Melan Music");
        playPauseButton.setImageResource(R.drawable.ic_pause);

        seekBar.setMax(playerCore.getDuration());
        totalTimeText.setText(formatTime(playerCore.getDuration()));
        currentTimeText.setText("00:00");

        handler.removeCallbacks(progressUpdater);
        handler.post(progressUpdater);
    }

    private void togglePlayPause() {
        if (playerCore.isPlaying()) {
            playerCore.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(progressUpdater);
        } else {
            if (playlistManager.getCurrentSong() != null) {
                playerCore.play();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                handler.post(progressUpdater);
            }
        }
    }

    private void playNext() {
        MusicScanner.Song nextSong = playlistManager.getNext();
        if (nextSong != null) {
            playCurrentSong();
        }
    }

    private void playPrevious() {
        MusicScanner.Song previousSong = playlistManager.getPrevious();
        if (previousSong != null) {
            playCurrentSong();
        }
    }

    private void toggleShuffle() {
        boolean enabled = !playlistManager.isShuffleEnabled();
        playlistManager.setShuffleEnabled(enabled);
        shuffleButton.setColorFilter(enabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, enabled ? "Shuffle on" : "Shuffle off", Toast.LENGTH_SHORT).show();
    }

    private void toggleRepeat() {
        boolean enabled = !playlistManager.isRepeatEnabled();
        playlistManager.setRepeatEnabled(enabled);
        repeatButton.setColorFilter(enabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, enabled ? "Repeat on" : "Repeat off", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQ_STORAGE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusicLibrary();
            } else {
                Toast.makeText(this, "Storage permission is required to play music.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStateChanged(MusicPlayerCore.PlayerState state) {
        Log.d(TAG, "Player state: " + state);
    }

    @Override
    public void onDurationChanged(int duration) {
        seekBar.setMax(duration);
        totalTimeText.setText(formatTime(duration));
    }

    @Override
    public void onPositionChanged(int position) {
        if (!isUserSeeking) {
            seekBar.setProgress(position);
            currentTimeText.setText(formatTime(position));
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressUpdater);
        playerCore.release();
    }
}
