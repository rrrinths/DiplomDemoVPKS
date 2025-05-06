package com.example.diplomdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AudioPlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private ImageButton playPauseButton, favoriteButton;
    private TextView currentTime, totalTime, audioTitleView;
    private ImageView audioCoverView;
    private Handler handler = new Handler();
    private boolean isFavorite = false;
    private String contentId;
    private int lastPlaybackPosition = 0;

    private FirebaseAuth auth;
    private DatabaseReference favoritesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        initViews();
        getIntentData();
        initializeMediaPlayer();
        setupBottomNavigation();
        checkAuth();
        checkIfFavorite();
        setupEventListeners();
    }

    private void initViews() {
        playPauseButton = findViewById(R.id.playPauseButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        audioTitleView = findViewById(R.id.audioTitle);
        audioCoverView = findViewById(R.id.audioCover);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        contentId = intent.getStringExtra("AUDIO_FILE");
        String title = intent.getStringExtra("AUDIO_TITLE");
        String image = intent.getStringExtra("AUDIO_IMAGE");

        if (contentId == null || title == null || image == null) {
            Toast.makeText(this, "Ошибка загрузки контента", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        audioTitleView.setText(title);
        int imageResId = getResources().getIdentifier(image, "drawable", getPackageName());
        audioCoverView.setImageResource(imageResId != 0 ? imageResId : R.drawable.sample_audio_image);
    }

    private void initializeMediaPlayer() {
        int resId = getResources().getIdentifier(contentId, "raw", getPackageName());
        if (resId == 0) {
            Toast.makeText(this, "Аудиофайл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            totalTime.setText(formatTime(mp.getDuration()));
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            playPauseButton.setImageResource(R.drawable.ic_play);
            seekBar.setProgress(0);
            currentTime.setText("0:00");
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                releaseMediaPlayer();
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                } else if (itemId == R.id.nav_category) {
                    startActivity(new Intent(this, CategoryActivity.class));
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, AccountActivity.class));
                }
                finish();
                return true;
            });
        }
    }

    private void checkAuth() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");
    }

    private void setupEventListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        favoriteButton.setOnClickListener(v -> toggleFavorite());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacksAndMessages(null);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) return;

        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
        handler.postDelayed(this::updateSeekBar, 1000);
    }

    private void checkIfFavorite() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = false;
                        for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                            String favContentId = favSnapshot.child("contentId").getValue(String.class);
                            if (contentId.equals(favContentId)) {
                                isFavorite = true;
                                break;
                            }
                        }
                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AudioPlayerActivity.this,
                                "Ошибка проверки избранного",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleFavorite() {
        if (isFavorite) {
            removeFromFavorites();
        } else {
            addToFavorites();
        }
    }

    private void addToFavorites() {
        String favoriteId = favoritesRef.push().getKey();
        if (favoriteId == null) return;

        favoritesRef.child(favoriteId).setValue(new Favorite(userId, contentId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFromFavorites() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                            String favContentId = favSnapshot.child("contentId").getValue(String.class);
                            if (contentId.equals(favContentId)) {
                                favSnapshot.getRef().removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                isFavorite = false;
                                                updateFavoriteIcon();
                                                Toast.makeText(AudioPlayerActivity.this,
                                                        "Удалено из избранного",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AudioPlayerActivity.this,
                                "Ошибка удаления",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFavoriteIcon() {
        favoriteButton.setImageResource(isFavorite ?
                R.drawable.ic_favorite_filled :
                R.drawable.ic_favorite_outline);
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            lastPlaybackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseMediaPlayer();
    }

    public static class Favorite {
        public String userId;
        public String contentId;

        public Favorite() {}

        public Favorite(String userId, String contentId) {
            this.userId = userId;
            this.contentId = contentId;
        }
    }
}