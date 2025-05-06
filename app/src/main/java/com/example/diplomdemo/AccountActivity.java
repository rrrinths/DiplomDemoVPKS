package com.example.diplomdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.os.IBinder;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AccountActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText editName;
    private TextView emailText;
    private Button btnSave, btnExit;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String currentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        emailText = findViewById(R.id.emailText);
        btnSave = findViewById(R.id.btnSave);
        btnExit = findViewById(R.id.btnExit);

        btnExit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        Button btnFavorites = findViewById(R.id.btnFavorites);
        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });

        if (user == null) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, activity_avtorization.class));
            finish();
            return;
        }

        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        emailText.setText(user.getEmail());

        if (databaseReference == null) {
            Toast.makeText(this, "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show();
            return;
        }

        loadUserData();

        profileImage.setOnClickListener(v -> selectImage());

        btnSave.setOnClickListener(v -> saveUserData());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_category) {
                startActivity(new Intent(this, CategoryActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        findViewById(R.id.root_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = AccountActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
    }

    private void loadUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String base64Image = snapshot.child("photoBase64").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        currentName = name;
                        editName.setText(name);
                    }

                    if (!TextUtils.isEmpty(base64Image)) {
                        try {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profileImage.setImageBitmap(decodedBitmap);
                        } catch (Exception e) {
                            Toast.makeText(AccountActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(AccountActivity.this, "Профиль не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String newName = editName.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newName.equals(currentName)) {
            databaseReference.child("name").setValue(newName);
        }

        if (imageUri != null) {
            String base64Image = encodeImageToBase64(imageUri);
            if (base64Image != null) {
                databaseReference.child("photoBase64").setValue(base64Image);
                Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!newName.equals(currentName)) {
                Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нет изменений для сохранения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка кодирования изображения", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                profileImage.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "Ошибка выбора изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
