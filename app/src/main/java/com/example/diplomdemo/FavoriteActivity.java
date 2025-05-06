package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FavoriteActivity extends AppCompatActivity {

    private DatabaseReference database;
    private FirebaseAuth auth;
    private LinearLayout favoriteContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favoriteContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        favoriteContainer = findViewById(R.id.favoriteContainer);

        loadFavorites();
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
    }

    private void loadFavorites() {
        String userId = auth.getCurrentUser().getUid();

        database.child("Favorites").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        favoriteContainer.removeAllViews();

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                                String contentId = favoriteSnapshot.child("contentId").getValue(String.class);
                                if (contentId != null) {
                                    loadAudioSessionDetails(contentId);
                                    loadYogaDetails(contentId);
                                    loadMeditationDetails(contentId);
                                }
                            }
                        } else {
                            Log.d("FAVORITES", "Избранное пусто для userId=" + userId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(FavoriteActivity.this, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadYogaDetails(String contentId) {
        database.child("Yoga").orderByChild("id").equalTo(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String title = snapshot.child("title").getValue(String.class);
                                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                                if (title != null && imageUrl != null) {
                                    addFavoriteItem(title, imageUrl, contentId, "yoga");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(FavoriteActivity.this, "Ошибка загрузки йоги", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadMeditationDetails(String contentId) {
        database.child("meditation").orderByChild("id").equalTo(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String title = snapshot.child("title").getValue(String.class);
                                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                                if (title != null && imageUrl != null) {
                                    addFavoriteItem(title, imageUrl, contentId, "meditation");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(FavoriteActivity.this, "Ошибка загрузки медитации", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAudioSessionDetails(String contentId) {
        database.child("AudioSessions").orderByChild("id").equalTo(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot sessionSnapshot : dataSnapshot.getChildren()) {
                                String title = sessionSnapshot.child("title").getValue(String.class);
                                String imageUrl = sessionSnapshot.child("image").getValue(String.class);

                                if (title != null && imageUrl != null) {
                                    addFavoriteItem(title, imageUrl, contentId, "audio");
                                }
                                else {
                                    Log.d("AUDIO_SESSION", "Данные некорректны: " + contentId);
                                }
                            }
                        } else {
                            Log.d("AUDIO_SESSION", "Аудио-сессия не найдена: " + contentId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(FavoriteActivity.this, "Ошибка загрузки аудио-сессии", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addFavoriteItem(String title, String imageUrl, String contentId, String contentType) {
        // Создание макета для элемента
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(20, 20, 20, 20);

        itemLayout.setBackgroundResource(R.drawable.card_background);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
        params.setMargins(0, 10, 0, 10);
        itemLayout.setLayoutParams(params);

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 120);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(imageUrl).into(imageView);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, 0);
        titleView.setLayoutParams(textParams);
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setTextSize(18);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        itemLayout.addView(imageView);
        itemLayout.addView(titleView);

        itemLayout.setOnClickListener(v -> {
            Intent intent;
            switch (contentType) {
                case "yoga":
                    intent = new Intent(this, YogaDetailActivity.class);
                    intent.putExtra("yogaId", contentId);
                    break;
                case "meditation":
                    intent = new Intent(this, MeditationDetailActivity.class);
                    intent.putExtra("meditationId", contentId);
                    break;
                case "audio":
                    intent = new Intent(this, AudioPlayerActivity.class);
                    intent.putExtra("AUDIO_FILE", contentId);
                    break;
                default:
                    intent = new Intent(this, AudioPlayerActivity.class);
                    intent.putExtra("CONTENT_ID", contentId);
                    break;
            }
            startActivity(intent);
        });

        favoriteContainer.addView(itemLayout);
    }
}
