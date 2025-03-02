package com.example.diplomdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
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
    private TextView currentTime, totalTime;
    private Handler handler = new Handler();
    private boolean isFavorite = false;
    private String contentId;

    private FirebaseAuth auth;
    private DatabaseReference favoritesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        playPauseButton = findViewById(R.id.playPauseButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        // Настройка нижнего бара
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_category) {
                    startActivity(new Intent(this, CategoryActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    startActivity(new Intent(this, AccountActivity.class));
                    return true;
                }
                return false;
            });
        }

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");

        contentId = getIntent().getStringExtra("AUDIO_FILE");
        if (contentId == null) {
            Toast.makeText(this, "Ошибка загрузки контента", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initializeMediaPlayer();

        checkIfFavorite();

        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                initializeMediaPlayer();
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                updateSeekBar();
            }
        });

        favoriteButton.setOnClickListener(v -> toggleFavorite());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mediaPlayer.seekTo(progress);
                currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        int resId = getAudioResId(contentId);
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
        });
    }

    private int getAudioResId(String audioFileName) {
        return getResources().getIdentifier(audioFileName, "raw", getPackageName());
    }

    private void playPauseAudio() {
        if (mediaPlayer == null) {
            initializeMediaPlayer();
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        }
    }


    private void checkIfFavorite() {
        favoritesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                    String favContentId = favSnapshot.child("contentId").getValue(String.class);
                    if (favContentId != null && favContentId.equals(contentId)) {
                        favSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    isFavorite = false;
                                    favoriteButton.setImageResource(R.drawable.ic_favorite_outline);
                                    Toast.makeText(AudioPlayerActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AudioPlayerActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
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
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                    Toast.makeText(AudioPlayerActivity.this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(AudioPlayerActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show());
    }

    private void removeFromFavorites() {
        favoritesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                    String favContentId = favSnapshot.child("contentId").getValue(String.class);
                    if (favContentId != null && favContentId.equals(contentId)) {
                        favSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            isFavorite = false;
                            favoriteButton.setImageResource(R.drawable.ic_favorite_outline);
                            Toast.makeText(AudioPlayerActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AudioPlayerActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSeekBar() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) return;

        handler.postDelayed(() -> {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            updateSeekBar();
        }, 1000);
    }


    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
