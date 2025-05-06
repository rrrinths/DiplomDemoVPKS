package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MotivationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motivation);

        LinearLayout quotesContainer = findViewById(R.id.quotesContainer);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(this, CategoryActivity.class));
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, AccountActivity.class));
            }
            return true;
        });

        DatabaseReference motivationRef = FirebaseDatabase.getInstance().getReference("Motivation");
        motivationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                quotesContainer.removeAllViews();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String imageSource = snapshot.child("image").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String id = snapshot.child("id").getValue(String.class);

                    addMotivationItem(quotesContainer, imageSource, name, id);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                TextView errorView = new TextView(MotivationActivity.this);
                errorView.setText("Ошибка загрузки данных");
                errorView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                quotesContainer.addView(errorView);
            }
        });
    }

    private void addMotivationItem(LinearLayout container, String imageSource, String name, String description) {
        // Создаем контейнер для элемента
        LinearLayout itemContainer = new LinearLayout(this);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(16));
        itemContainer.setLayoutParams(containerParams);
        itemContainer.setBackgroundResource(R.drawable.card_background);
        itemContainer.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(imageParams);

        imageView.setMaxHeight(dpToPx(500));

        loadImage(imageView, imageSource);
        itemContainer.addView(imageView);

        if (name != null && !name.isEmpty()) {
            TextView nameView = new TextView(this);
            nameView.setText(name);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            nameView.setTextColor(getResources().getColor(android.R.color.white));
            nameView.setGravity(Gravity.CENTER);
            nameView.setPadding(0, dpToPx(12), 0, 0);
            itemContainer.addView(nameView);
        }

        container.addView(itemContainer);
    }

    private void loadImage(ImageView imageView, String imageSource) {
        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(R.drawable.card_background);
            return;
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.card_background)
                .error(R.drawable.card_background)
                .fitCenter();

        if (imageSource.startsWith("http")) {
            Glide.with(this)
                    .load(imageSource)
                    .apply(requestOptions)
                    .into(imageView);
        } else {
            int resId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this)
                        .load(resId)
                        .apply(requestOptions)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.card_background);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}