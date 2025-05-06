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

public class YogaActivity extends AppCompatActivity {

    private LinearLayout yogaContainer;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga);

        yogaContainer = findViewById(R.id.YogaContainer);
        databaseRef = FirebaseDatabase.getInstance().getReference("Yoga");

        loadYoga();
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

    private void loadYoga() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "Нет данных в Realtime Database");
                    return;
                }

                Log.d("Firebase", "Йоги загружены: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot yogaSnapshot : dataSnapshot.getChildren()) {
                    String id = yogaSnapshot.getKey();
                    String title = yogaSnapshot.child("title").getValue(String.class);
                    String imageUrl = yogaSnapshot.child("imageUrl").getValue(String.class);

                    if (id == null || title == null || imageUrl == null) {
                        Log.e("Firebase", "Ошибка загрузки йоги: отсутствуют данные");
                        continue;
                    }

                    addYogaView(id, title, imageUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Ошибка загрузки медитаций", databaseError.toException());
            }
        });
    }

    private void addYogaView(String id, String title, String imageRes) {
        LinearLayout yogaLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        yogaLayout.setLayoutParams(layoutParams);
        yogaLayout.setOrientation(LinearLayout.HORIZONTAL);
        yogaLayout.setPadding(24, 24, 24, 24);
        yogaLayout.setGravity(Gravity.CENTER_VERTICAL);

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

        yogaLayout.addView(imageView);
        yogaLayout.addView(textView);

        yogaLayout.setOnClickListener(view -> openMeditationDetail(id));

        yogaContainer.addView(yogaLayout);
    }

    private void openMeditationDetail(String yogaId) {
        Intent intent = new Intent(this, YogaDetailActivity.class);
        intent.putExtra("yogaId", yogaId);
        startActivity(intent);
    }
}
