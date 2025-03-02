package com.example.diplomdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class activity_avtorization extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avtorization);

        // Инициализация Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Инициализация элементов интерфейса
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnVoiti);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvPassword);

        // Обработчик кнопки входа
        btnLogin.setOnClickListener(view -> loginUser());

        // Обработчик перехода к регистрации
        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(activity_avtorization.this, RegistrationActivity.class));
            finish();
        });

        // Обработчик восстановления пароля
        tvForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(activity_avtorization.this, PasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Проверка заполнения полей
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Вход через Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(activity_avtorization.this, AccountActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Ошибка авторизации. Проверьте данные!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
