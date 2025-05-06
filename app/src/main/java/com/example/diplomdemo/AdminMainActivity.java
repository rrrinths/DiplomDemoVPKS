package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminMainActivity extends AppCompatActivity {

    private Button btnMeditation, btnYoga, btnCategory, btnExit, btnMotivation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnMeditation = findViewById(R.id.btnMeditation);
        btnYoga = findViewById(R.id.btnYoga);
        btnMotivation = findViewById(R.id.btnMotivation);
        btnCategory = findViewById(R.id.btnCategory);
        btnExit = findViewById(R.id.btnExit);
        btnMeditation.setOnClickListener(v -> startActivity(new Intent(this, MeditationAdminActivity.class)));

        btnYoga.setOnClickListener(v -> startActivity(new Intent(this, YogaAdminActivity.class)));

        btnCategory.setOnClickListener(v -> startActivity(new Intent(this, CategoryAdminActivity.class)));

        btnMotivation.setOnClickListener(v -> startActivity(new Intent(this, MotivationAdminActivity.class)));
        btnExit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }
}
