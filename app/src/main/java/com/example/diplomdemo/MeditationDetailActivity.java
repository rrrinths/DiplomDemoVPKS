package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;

public class MeditationDetailActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView, meditationTextView;
    private ImageView meditationImageView;
    private ImageButton favoriteButton;

    private DatabaseReference databaseRef, favoritesRef;
    private String meditationId, userId;
    private boolean isFavorite = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_detail);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        meditationTextView = findViewById(R.id.meditationTextView);
        meditationImageView = findViewById(R.id.meditationImageView);
        favoriteButton = findViewById(R.id.btnFavorite);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пожалуйста, войдите в аккаунт", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();
        meditationId = getIntent().getStringExtra("meditationId");

        if (meditationId == null) {
            Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("meditation").child(meditationId);
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");

        loadMeditationData();
        checkIfFavorite();

        favoriteButton.setOnClickListener(v -> {
            if (isFavorite) {
                removeFromFavorites();
            } else {
                addToFavorites();
            }
        });

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

    private void loadMeditationData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                String title = dataSnapshot.child("title").getValue(String.class);
                String description = dataSnapshot.child("description").getValue(String.class);
                String meditationText = dataSnapshot.child("meditationtext").getValue(String.class);
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                titleTextView.setText(title);
                descriptionTextView.setText(description);
                meditationTextView.setText(meditationText);

                int imageId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                if (imageId != 0) {
                    meditationImageView.setImageResource(imageId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Ошибка загрузки медитации", databaseError.toException());
            }
        });
    }

    private void checkIfFavorite() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = false;

                        for (DataSnapshot fav : snapshot.getChildren()) {
                            String contentId = fav.child("contentId").getValue(String.class);
                            if (meditationId.equals(contentId)) {
                                isFavorite = true;
                                break;
                            }
                        }

                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MeditationDetailActivity.this, "Ошибка проверки избранного", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToFavorites() {
        String favoriteId = favoritesRef.push().getKey();
        if (favoriteId == null) return;

        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("contentId", meditationId);

        favoritesRef.child(favoriteId).setValue(map)
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка добавления в избранное", Toast.LENGTH_SHORT).show()
                );
    }

    private void removeFromFavorites() {
        favoritesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot fav : snapshot.getChildren()) {
                            String contentId = fav.child("contentId").getValue(String.class);
                            if (meditationId.equals(contentId)) {
                                fav.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            isFavorite = false;
                                            updateFavoriteIcon();
                                            Toast.makeText(MeditationDetailActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MeditationDetailActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFavoriteIcon() {
        favoriteButton.setImageResource(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
    }
}

