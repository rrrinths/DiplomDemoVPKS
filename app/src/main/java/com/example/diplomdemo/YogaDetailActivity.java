package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;

public class YogaDetailActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView;
    private ImageButton favoriteButton;
    private boolean isFavorite = false;
    private String yogaId;
    private DatabaseReference favoritesRef;
    private String userId;
    private LinearLayout yogaContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_detail);

        initViews();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToastAndFinish("Пожалуйста, войдите в аккаунт");
            return;
        }
        userId = user.getUid();

        yogaId = getIntent().getStringExtra("yogaId");
        if (yogaId == null) {
            showToastAndFinish("Ошибка загрузки йоги");
            return;
        }

        initFirebase();

        checkIfFavorite();

        favoriteButton.setOnClickListener(v -> toggleFavorite());

        loadYogaData();

        setupBottomNavigation();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        favoriteButton = findViewById(R.id.btnFavorite);
        yogaContainer = findViewById(R.id.yogaContainer);
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initFirebase() {
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");
    }

    private void toggleFavorite() {
        if (isFavorite) {
            removeFromFavorites();
        } else {
            addToFavorites();
        }
    }

    private void checkIfFavorite() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = false;
                        for (DataSnapshot fav : snapshot.getChildren()) {
                            String contentId = fav.child("contentId").getValue(String.class);
                            if (yogaId.equals(contentId)) {
                                isFavorite = true;
                                break;
                            }
                        }
                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(YogaDetailActivity.this, "Ошибка проверки избранного", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToFavorites() {
        String favoriteId = favoritesRef.push().getKey();
        if (favoriteId == null) return;

        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("contentId", yogaId);

        favoritesRef.child(favoriteId).setValue(map)
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show()
                );
    }

    private void removeFromFavorites() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot fav : snapshot.getChildren()) {
                            String contentId = fav.child("contentId").getValue(String.class);
                            if (yogaId.equals(contentId)) {
                                fav.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            isFavorite = false;
                                            updateFavoriteIcon();
                                            Toast.makeText(YogaDetailActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(YogaDetailActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFavoriteIcon() {
        favoriteButton.setImageResource(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
    }

    private void loadYogaData() {
        DatabaseReference yogaRef = FirebaseDatabase.getInstance().getReference("Yoga").child(yogaId);
        yogaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                String title = dataSnapshot.child("title").getValue(String.class);
                String description = dataSnapshot.child("description").getValue(String.class);
                titleTextView.setText(title);
                descriptionTextView.setText(description);

                yogaContainer.removeAllViews();

                for (int i = 1; i <= 8; i++) {
                    String uprKey = "upr" + i;
                    String imgKey = "img" + i;

                    String exerciseText = dataSnapshot.child(uprKey).getValue(String.class);
                    String imageSource = dataSnapshot.child(imgKey).getValue(String.class);

                    if (exerciseText != null && !exerciseText.isEmpty()) {
                        addExerciseCard(exerciseText);
                    }

                    if (imageSource != null && !imageSource.isEmpty()) {
                        addExerciseImage(imageSource);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Ошибка загрузки данных йоги", databaseError.toException());
                Toast.makeText(YogaDetailActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addExerciseCard(String exerciseText) {
        LinearLayout textCard = new LinearLayout(this);
        textCard.setOrientation(LinearLayout.VERTICAL);
        textCard.setBackgroundResource(R.drawable.card_background);
        textCard.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        textCard.setLayoutParams(cardParams);

        TextView exerciseTextView = new TextView(this);
        exerciseTextView.setText(exerciseText);
        exerciseTextView.setTextColor(getResources().getColor(android.R.color.white));
        exerciseTextView.setTextSize(16);
        textCard.addView(exerciseTextView);

        textCard.setAlpha(0f);
        textCard.animate().alpha(1f).setDuration(500).start();

        yogaContainer.addView(textCard);
    }

    private void addExerciseImage(String imageSource) {
        ImageView exerciseImageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
        );
        params.setMargins(0, 0, 0, dpToPx(16));
        exerciseImageView.setLayoutParams(params);
        exerciseImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        exerciseImageView.setBackgroundResource(R.drawable.card_background);

        if (imageSource.startsWith("http")) {

            Glide.with(this)
                    .load(imageSource)
                    .placeholder(R.drawable.card_background)
                    .error(R.drawable.card_background)
                    .into(exerciseImageView);
        } else {

            int resId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
            if (resId != 0) {
                exerciseImageView.setImageResource(resId);
            } else {
                exerciseImageView.setImageResource(R.drawable.card_background);
            }
        }

        exerciseImageView.setAlpha(0f);
        exerciseImageView.animate().alpha(1f).setDuration(500).start();

        yogaContainer.addView(exerciseImageView);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.nav_category) {
                    startActivity(new Intent(this, CategoryActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, AccountActivity.class));
                    return true;
                }
                return false;
            });
        }
    }
}