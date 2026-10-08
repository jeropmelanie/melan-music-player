package com.example.melanmusicplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_STORAGE = 101;

    private final ArrayList<String> songPaths = new ArrayList<>();
    private final ArrayList<String> songTitles = new ArrayList<>();
    private final Random random = new Random();

    private MediaPlayer mediaPlayer;
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

    private boolean isShuffleEnabled = false;
    private boolean isRepeatEnabled = false;
    private int currentIndex = -1;
    private boolean isUserSeeking = false;

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPos = mediaPlayer.getCurrentPosition();
                if (!isUserSeeking) {
                    seekBar.setProgress(currentPos);
                    currentTimeText.setText(formatTime(currentPos));
                }
                seekBar.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        bindViews();
        initPlayer();

        if (hasStoragePermission()) {
            loadSongs();
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

        prevButton.setOnClickListener(v -> playPrevious());
        nextButton.setOnClickListener(v -> playNext());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        repeatButton.setOnClickListener(v -> toggleRepeat());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { isUserSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { isUserSeeking = false; }
        });

        playlistView.setOnItemClickListener((parent, view, position, id) -> playSongAt(position));
    }

    private void initPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeatEnabled) {
                playSongAt(currentIndex);
            } else {
                playNext();
            }
        });
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
                loadSongs();
            } else {
                Toast.makeText(this, "Storage permission is required to play local music.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSongs() {
        songPaths.clear();
        songTitles.clear();

        ArrayList<File> musicDirs = new ArrayList<>();
        musicDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        File internalMusic = new File(Environment.getExternalStorageDirectory(), "Music");
        if (internalMusic.exists()) {
            musicDirs.add(internalMusic);
        }

        for (File dir : musicDirs) {
            if (dir != null && dir.exists()) {
                collectMusicFiles(dir);
            }
        }

        if (songPaths.isEmpty()) {
            String fallbackPath = "/storage/emulated/0/Music";
            File fallbackDir = new File(fallbackPath);
            if (fallbackDir.exists()) {
                collectMusicFiles(fallbackDir);
            }
        }

        if (songPaths.isEmpty()) {
            Toast.makeText(this, "No music files found in Music folder.", Toast.LENGTH_LONG).show();
            songTitles.add("No songs found");
            songPaths.add("");
        } else {
            Collections.sort(songTitles);
            ArrayList<String> orderedPaths = new ArrayList<>();
            for (String title : songTitles) {
                String matchingPath = null;
                for (String path : songPaths) {
                    if (path.endsWith(title)) {
                        matchingPath = path;
                        break;
                    }
                }
                if (matchingPath != null) {
                    orderedPaths.add(matchingPath);
                }
            }
            songPaths.clear();
            songPaths.addAll(orderedPaths);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, songTitles);
        playlistView.setAdapter(adapter);

        if (!songPaths.isEmpty() && !songPaths.get(0).isEmpty()) {
            playSongAt(0);
        }
    }

    private void collectMusicFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                collectMusicFiles(file);
            } else {
                String name = file.getName().toLowerCase(Locale.US);
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".aac")) {
                    String title = file.getName();
                    if (title.endsWith(".mp3")) {
                        title = title.substring(0, title.length() - 4);
                    } else if (title.endsWith(".wav")) {
                        title = title.substring(0, title.length() - 4);
                    } else if (title.endsWith(".m4a")) {
                        title = title.substring(0, title.length() - 4);
                    } else if (title.endsWith(".aac")) {
                        title = title.substring(0, title.length() - 4);
                    }

                    songPaths.add(file.getAbsolutePath());
                    songTitles.add(title);
                }
            }
        }
    }

    private void playSongAt(int index) {
        if (index < 0 || index >= songPaths.size() || songPaths.get(index).isEmpty()) {
            return;
        }

        currentIndex = index;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.reset();
            mediaPlayer.setDataSource(songPaths.get(index));
            mediaPlayer.prepare();
            mediaPlayer.start();

            String title = songTitles.get(index);
            currentSongText.setText(title);
            artistText.setText("Local track");
            playPauseButton.setImageResource(R.drawable.ic_pause);

            int duration = mediaPlayer.getDuration();
            seekBar.setMax(duration);
            totalTimeText.setText(formatTime(duration));
            currentTimeText.setText("00:00");

            seekBar.post(progressUpdater);
        } catch (Exception e) {
            Log.e("MelanMusicPlayer", "Failed to play song", e);
            Toast.makeText(this, "Unable to play this track.", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null || songPaths.isEmpty()) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            if (currentIndex >= 0) {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                seekBar.post(progressUpdater);
            } else {
                playSongAt(0);
            }
        }
    }

    private void playNext() {
        if (songPaths.isEmpty()) return;
        int nextIndex;
        if (isShuffleEnabled) {
            nextIndex = random.nextInt(songPaths.size());
        } else {
            nextIndex = (currentIndex + 1) % songPaths.size();
        }
        playSongAt(nextIndex);
    }

    private void playPrevious() {
        if (songPaths.isEmpty()) return;
        int prevIndex = (currentIndex - 1 + songPaths.size()) % songPaths.size();
        playSongAt(prevIndex);
    }

    private void toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled;
        shuffleButton.setColorFilter(isShuffleEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, isShuffleEnabled ? "Shuffle on" : "Shuffle off", Toast.LENGTH_SHORT).show();
    }

    private void toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled;
        repeatButton.setColorFilter(isRepeatEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, isRepeatEnabled ? "Repeat on" : "Repeat off", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
