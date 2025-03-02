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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

public class CategoryActivity extends AppCompatActivity {

    private LinearLayout categoryContainer;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryContainer = findViewById(R.id.categoryContainer);
        databaseRef = FirebaseDatabase.getInstance().getReference("Category"); // вместо "categories"

        loadCategories();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

    private void loadCategories() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "Нет данных в Realtime Database");
                    return;
                }

                Log.d("Firebase", "Категории загружены: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String id = categorySnapshot.child("id").getValue(String.class);
                    String title = categorySnapshot.child("title").getValue(String.class);
                    String imageUrl = categorySnapshot.child("imageUrl").getValue(String.class);

                    if (id == null || title == null || imageUrl == null) {
                        Log.e("Firebase", "Ошибка загрузки категории: отсутствуют данные");
                        continue; // Пропускаем, если есть проблемы
                    }

                    Log.d("Firebase", "Категория: ID=" + id + ", Title=" + title + ", ImageURL=" + imageUrl);
                    addCategoryView(id, title, imageUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Ошибка загрузки категорий", databaseError.toException());
            }
        });
    }

    private void addCategoryView(String id, String title, String imageRes) {
        LinearLayout categoryLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16); // Отступы вокруг блока категории
        categoryLayout.setLayoutParams(layoutParams);
        categoryLayout.setOrientation(LinearLayout.VERTICAL);
        categoryLayout.setPadding(24, 24, 24, 24);
        categoryLayout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(this);
        int imageId = getResources().getIdentifier(imageRes, "drawable", getPackageName());
        if (imageId != 0) {
            imageView.setImageResource(imageId);
        } else {
            Log.e("Firebase", "Ошибка загрузки изображения: " + imageRes);
        }
        imageView.setLayoutParams(new LinearLayout.LayoutParams(500, 180)); // Увеличенные размеры изображения
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 12, 0, 0); // Увеличенный отступ сверху

        categoryLayout.addView(imageView);
        categoryLayout.addView(textView);
        categoryLayout.setOnClickListener(view -> openCategory(id));

        categoryContainer.addView(categoryLayout);
    }

    private void openCategory(String categoryId) {
        Intent intent;
        switch (categoryId) {
            case "beginner":
                intent = new Intent(this, BeginnerActivity.class);
                break;
            case "meditation":
                intent = new Intent(this, MeditationActivity.class);
                break;
            case "motivation":
                intent = new Intent(this, MotivationActivity.class);
                break;
            case "audio_sessions":
                intent = new Intent(this, AudioActivity.class);
                break;
            case "yoga":
                intent = new Intent(this, YogaActivity.class);
                break;
            default:
                Log.e("Firebase", "Неизвестная категория: " + categoryId);
                return;
        }
        startActivity(intent);
    }
}
