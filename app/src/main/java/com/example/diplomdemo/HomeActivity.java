package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting, tvMotivation, tvMeditation, tvYoga;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivation = findViewById(R.id.tvMotivation);
        tvMeditation = findViewById(R.id.Meditation);
        tvYoga = findViewById(R.id.Yoga);

        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId);

            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    if (userName == null || userName.isEmpty()) {
                        userName = "Пользователь";
                    }
                    tvGreeting.setText("Доброе время суток, " + userName + "!");
                } else {
                    tvGreeting.setText("Доброе время суток!");
                }
            }).addOnFailureListener(e -> {
                tvGreeting.setText("Доброе время суток!");
            });
        } else {
            tvGreeting.setText("Доброе время суток!");
        }

        tvMotivation.setOnClickListener(view -> startActivity(new Intent(this, MotivationActivity.class)));
        tvMeditation.setOnClickListener(view -> startActivity(new Intent(this, MeditationActivity.class)));
        tvYoga.setOnClickListener(view -> startActivity(new Intent(this, YogaActivity.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
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
