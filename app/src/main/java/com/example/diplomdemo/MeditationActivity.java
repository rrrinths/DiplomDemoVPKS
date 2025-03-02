package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

public class MeditationActivity extends AppCompatActivity {

    private LinearLayout meditationContainer;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        meditationContainer = findViewById(R.id.meditationContainer);
        databaseRef = FirebaseDatabase.getInstance().getReference("meditation");

        loadMeditations();
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

    private void loadMeditations() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "Нет данных в Realtime Database");
                    return;
                }

                Log.d("Firebase", "Медитации загружены: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot meditationSnapshot : dataSnapshot.getChildren()) {
                    String id = meditationSnapshot.getKey();
                    String title = meditationSnapshot.child("title").getValue(String.class);
                    String imageUrl = meditationSnapshot.child("imageUrl").getValue(String.class);

                    if (id == null || title == null || imageUrl == null) {
                        Log.e("Firebase", "Ошибка загрузки медитации: отсутствуют данные");
                        continue;
                    }

                    addMeditationView(id, title, imageUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Ошибка загрузки медитаций", databaseError.toException());
            }
        });
    }

    private void addMeditationView(String id, String title, String imageRes) {
        LinearLayout meditationLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        meditationLayout.setLayoutParams(layoutParams);
        meditationLayout.setOrientation(LinearLayout.HORIZONTAL);
        meditationLayout.setPadding(24, 24, 24, 24);
        meditationLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView imageView = new ImageView(this);
        int imageId = getResources().getIdentifier(imageRes, "drawable", getPackageName());
        if (imageId != 0) {
            imageView.setImageResource(imageId);
        } else {
            Log.e("Firebase", "Ошибка загрузки изображения: " + imageRes);
        }
        imageView.setLayoutParams(new LinearLayout.LayoutParams(160, 150));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setPadding(24, 0, 0, 0);

        meditationLayout.addView(imageView);
        meditationLayout.addView(textView);

        meditationLayout.setOnClickListener(view -> openMeditationDetail(id));

        meditationContainer.addView(meditationLayout);
    }

    private void openMeditationDetail(String meditationId) {
        Intent intent = new Intent(this, MeditationDetailActivity.class);
        intent.putExtra("meditationId", meditationId);
        startActivity(intent);
    }
}
