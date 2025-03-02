package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity {
    private EditText editEmail;
    private Button btnResetPassword, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // Инициализация Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Найти элементы на экране
        editEmail = findViewById(R.id.editEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.nav_home);

        // Обработчик кнопки "Сбросить пароль"
        btnResetPassword.setOnClickListener(view -> resetPassword());

        // Обработчик кнопки "Назад"
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(PasswordActivity.this, activity_avtorization.class));
            finish();
        });
    }

    private void resetPassword() {
        String email = editEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отправка письма для сброса пароля
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Письмо для сброса пароля отправлено", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Ошибка! Проверьте email", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
