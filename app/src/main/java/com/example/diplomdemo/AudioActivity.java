package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AudioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_audio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout audio1 = findViewById(R.id.audio1);
        LinearLayout audio2 = findViewById(R.id.audio2);
        LinearLayout audio3 = findViewById(R.id.audio3);
        LinearLayout audio4 = findViewById(R.id.audio4);
        audio1.setOnClickListener(v -> openAudioPlayer("barhatnyy_utrenniy_dozhd"));
        audio2.setOnClickListener(v -> openAudioPlayer("okean_myagkoy_koncetracii"));
        audio3.setOnClickListener(v -> openAudioPlayer("loveandkindness"));
        audio4.setOnClickListener(v -> openAudioPlayer("osoznannoedikhanie"));

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

    private void openAudioPlayer(String audioFileName) {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("AUDIO_FILE", audioFileName);

        String title = "";
        String imageRes = "";

        switch (audioFileName) {
            case "barhatnyy_utrenniy_dozhd":
                title = "Бархатный утренний дождь";
                imageRes = "imageaudio1";
                break;
            case "okean_myagkoy_koncetracii":
                title = "Океан мягкой концентрации";
                imageRes = "imageaudio2";
                break;
            case "loveandkindness":
                title = "Медитация любящей доброты";
                imageRes = "imageaudio3";
                break;
            case "osoznannoedikhanie":
                title = "Медитация осознанного дыхания";
                imageRes = "imageaudio4";
                break;
        }
        intent.putExtra("AUDIO_TITLE", title);
        intent.putExtra("AUDIO_IMAGE", imageRes);
        startActivity(intent);
    }
}
