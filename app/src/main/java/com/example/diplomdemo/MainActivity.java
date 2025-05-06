package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private HorizontalScrollView benefitsScroll;
    private LinearLayout dotsContainer;
    private int currentPage = 0;
    private final int pageCount = 3;
    private final int scrollDuration = 300;
    private Handler scrollHandler = new Handler();
    private boolean isScrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button1);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        benefitsScroll = findViewById(R.id.benefitsScroll);
        dotsContainer = findViewById(R.id.dotsContainer);

        setupDots();

        benefitsScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!isScrolling) {
                isScrolling = true;
                scrollHandler.postDelayed(() -> {
                    int scrollX = benefitsScroll.getScrollX();
                    int pageWidth = benefitsScroll.getChildAt(0).getWidth() / pageCount;
                    currentPage = (scrollX + pageWidth / 2) / pageWidth;
                    updateDots();
                    isScrolling = false;
                }, 100);
            }
        });
    }

    private void setupDots() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < pageCount; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8),
                    dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots() {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == currentPage ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    public void scrollToPage(int page) {
        if (page < 0 || page >= pageCount) return;

        currentPage = page;
        int pageWidth = benefitsScroll.getChildAt(0).getWidth() / pageCount;
        int targetScrollX = page * pageWidth;

        benefitsScroll.smoothScrollTo(targetScrollX, 0);
        updateDots();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scrollHandler.removeCallbacksAndMessages(null);
    }
}