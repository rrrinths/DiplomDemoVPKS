package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

public class MeditationDetailActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView, meditationTextView;
    private ImageView meditationImageView;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_detail);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        meditationTextView = findViewById(R.id.meditationTextView); // Новый TextView
        meditationImageView = findViewById(R.id.meditationImageView);

        String meditationId = getIntent().getStringExtra("meditationId");
        if (meditationId == null) {
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("meditation").child(meditationId);
        loadMeditationData();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_category) {
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
                String meditationText = dataSnapshot.child("meditationtext").getValue(String.class); // Получаем текст медитации
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                titleTextView.setText(title);
                descriptionTextView.setText(description);
                meditationTextView.setText(meditationText); // Устанавливаем текст медитации

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
}
