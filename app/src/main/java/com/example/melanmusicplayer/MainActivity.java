package com.example.melanmusicplayer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.WindowCompat;

import com.example.melanmusicplayer.player.PlayerController;
import com.example.melanmusicplayer.repository.SongRepository;
import com.example.melanmusicplayer.service.MusicService;
import com.example.melanmusicplayer.util.FavoritesStore;
import com.example.melanmusicplayer.util.QueueStore;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MusicService.PlaybackListener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (playerController != null && playerController.isPlaying()) {
                int pos = playerController.getCurrentPosition();
                if (!isUserSeeking) {
                    seekBar.setProgress(pos);
                    currentTimeText.setText(formatTime(pos));
                }
                handler.postDelayed(this, 500);
            }
        }
    };

    private PlayerController playerController;
    private SongRepository songRepository;
    private FavoritesStore favoritesStore;
    private QueueStore queueStore;
    private ArrayList<SongRepository.Song> songs = new ArrayList<>();
    private ArrayList<SongRepository.Song> queue = new ArrayList<>();
    private int currentIndex = 0;

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

    private boolean isUserSeeking = false;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        playerController = new PlayerController(this);
        songRepository = new SongRepository(this);
        favoritesStore = new FavoritesStore(this);
        queueStore = new QueueStore(this);

        bindViews();
        setupListeners();
        playerController.setPlaybackListener(this);

        new Thread(() -> {
            songs = songRepository.getAllSongs();
            runOnUiThread(() -> {
                if (songs.isEmpty()) {
                    Toast.makeText(this, "No songs found in your music library.", Toast.LENGTH_LONG).show();
                    return;
                }
                queue.clear();
                queue.addAll(songs);
                updatePlaylistUI();
                playSong(queue.get(0));
            });
        }).start();
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
                if (fromUser) {
                    playerController.seek(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { isUserSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { isUserSeeking = false; }
        });

        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 15f;
                playerController.setVolume(volume);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playlistView.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = position;
            playSong(queue.get(position));
        });
    }

    private void updatePlaylistUI() {
        ArrayList<String> names = new ArrayList<>();
        for (SongRepository.Song song : queue) {
            names.add(song.title + " - " + song.artist);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, names);
        playlistView.setAdapter(adapter);
    }

    private void playSong(SongRepository.Song song) {
        playerController.loadTrack(song);
        playerController.play();

        currentSongText.setText(song.title);
        artistText.setText(song.artist);
        playPauseButton.setImageResource(R.drawable.ic_pause);

        if (song.albumArt != null) {
            albumArtView.setImageBitmap(getRoundedBitmap(song.albumArt));
        } else {
            albumArtView.setImageResource(R.drawable.ic_music_placeholder);
        }

        song.isFavorite = favoritesStore.isFavorite(song.path);
        favoriteButton.setImageResource(song.isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);

        int duration = playerController.getDuration();
        if (duration > 0) {
            seekBar.setMax(duration);
            totalTimeText.setText(formatTime(duration));
        }
        currentTimeText.setText("00:00");

        handler.removeCallbacks(progressUpdater);
        handler.post(progressUpdater);
    }

    public Bitmap getRoundedBitmap(Bitmap source) {
        Bitmap scaled = Bitmap.createScaledBitmap(source, 280, 280, true);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), scaled);
        drawable.setCornerRadius(30f);
        return scaled;
    }

    private void togglePlayPause() {
        if (playerController.isPlaying()) {
            playerController.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(progressUpdater);
        } else {
            playerController.play();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            handler.post(progressUpdater);
        }
    }

    private void playNext() {
        if (queue.isEmpty()) return;
        if (shuffleEnabled) {
            currentIndex = (int) (Math.random() * queue.size());
        } else {
            currentIndex = (currentIndex + 1) % queue.size();
        }
        playSong(queue.get(currentIndex));
    }

    private void playPrevious() {
        if (queue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + queue.size()) % queue.size();
        playSong(queue.get(currentIndex));
    }

    private void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        shuffleButton.setColorFilter(shuffleEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, shuffleEnabled ? "Shuffle on" : "Shuffle off", Toast.LENGTH_SHORT).show();
    }

    private void toggleRepeat() {
        repeatEnabled = !repeatEnabled;
        repeatButton.setColorFilter(repeatEnabled ? 0xFF6EE7B7 : 0xFFFFFFFF);
        Toast.makeText(this, repeatEnabled ? "Repeat on" : "Repeat off", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        if (queue.isEmpty()) return;
        SongRepository.Song song = queue.get(currentIndex);
        favoritesStore.toggleFavorite(song.path);
        song.isFavorite = favoritesStore.isFavorite(song.path);
        favoriteButton.setImageResource(song.isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        Toast.makeText(this, song.isFavorite ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        if (isPlaying) {
            handler.post(progressUpdater);
        } else {
            handler.removeCallbacks(progressUpdater);
        }
    }

    @Override
    public void onTrackChanged(String title, String artist, Bitmap albumArt) {
        currentSongText.setText(title);
        artistText.setText(artist);
        if (albumArt != null) {
            albumArtView.setImageBitmap(getRoundedBitmap(albumArt));
        }
    }

    @Override
    public void onPositionChanged(int position) {
        if (!isUserSeeking) {
            seekBar.setProgress(position);
            currentTimeText.setText(formatTime(position));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressUpdater);
        if (playerController != null) {
            playerController.unbind();
        }
    }
}
