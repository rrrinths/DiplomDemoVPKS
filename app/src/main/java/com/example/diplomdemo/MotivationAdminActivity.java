package com.example.diplomdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MotivationAdminActivity extends AppCompatActivity {

    private RecyclerView motivationRecyclerView;
    private MotivationAdapter motivationAdapter;
    private ArrayList<Map<String, String>> motivationList;
    private EditText editTextCategoryId, editTextId, editTextName, editTextImage;
    private ImageView motivationImageView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String base64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motivation_admin);

        initViews();

        motivationList = new ArrayList<>();
        motivationAdapter = new MotivationAdapter(this, motivationList);
        motivationRecyclerView.setAdapter(motivationAdapter);

        getMotivationFromFirebase();

        setupListeners();

        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = MotivationAdminActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
    }

    private void initViews() {
        motivationRecyclerView = findViewById(R.id.motivationRecyclerView);
        motivationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextCategoryId = findViewById(R.id.editTextCategoryId);
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextImage = findViewById(R.id.editTextImage);
        motivationImageView = findViewById(R.id.motivationImageView);
    }

    private void setupListeners() {
        motivationImageView.setOnClickListener(v -> openImagePicker());

        findViewById(R.id.buttonAdd).setOnClickListener(v -> addMotivation());
        findViewById(R.id.buttonUpdate).setOnClickListener(v -> updateMotivation());
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteMotivation());

        Button button = findViewById(R.id.nav_home);
        button.setOnClickListener(v -> startActivity(new Intent(MotivationAdminActivity.this, AdminMainActivity.class)));
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                motivationImageView.setImageBitmap(bitmap);
                base64Image = convertToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void getMotivationFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Motivation");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                motivationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> motivation = new HashMap<>();
                    motivation.put("categoryId", snapshot.child("categoryId").getValue(String.class));
                    motivation.put("id", snapshot.child("id").getValue(String.class));
                    motivation.put("name", snapshot.child("name").getValue(String.class));
                    motivation.put("image", snapshot.child("image").getValue(String.class));

                    motivationList.add(motivation);
                }
                motivationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MotivationAdminActivity.this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMotivation() {
        String categoryId = editTextCategoryId.getText().toString();
        String id = editTextId.getText().toString();
        String name = editTextName.getText().toString();
        String image = editTextImage.getText().toString();

        if (!categoryId.isEmpty() && !id.isEmpty() && !name.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Motivation").child(id);
            Map<String, Object> motivation = new HashMap<>();
            motivation.put("categoryId", categoryId);
            motivation.put("id", id);
            motivation.put("name", name);

            if (!base64Image.isEmpty()) {
                motivation.put("image", base64Image);
            } else if (!image.isEmpty()) {
                motivation.put("image", image);
            } else {
                motivation.put("image", "");
            }

            databaseReference.setValue(motivation)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MotivationAdminActivity.this, "Мотивация добавлена", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MotivationAdminActivity.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMotivation() {
        String id = editTextId.getText().toString();
        String categoryId = editTextCategoryId.getText().toString();
        String name = editTextName.getText().toString();
        String image = editTextImage.getText().toString();

        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Motivation").child(id);
            Map<String, Object> updates = new HashMap<>();
            updates.put("categoryId", categoryId);
            updates.put("name", name);

            if (!base64Image.isEmpty()) {
                updates.put("image", base64Image);
            } else if (!image.isEmpty()) {
                updates.put("image", image);
            }

            databaseReference.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MotivationAdminActivity.this, "Мотивация обновлена", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(MotivationAdminActivity.this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Укажите ID мотивации", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMotivation() {
        String id = editTextId.getText().toString();
        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Motivation").child(id);
            databaseReference.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MotivationAdminActivity.this, "Мотивация удалена", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MotivationAdminActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Укажите ID мотивации", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        editTextCategoryId.setText("");
        editTextId.setText("");
        editTextName.setText("");
        editTextImage.setText("");
        motivationImageView.setImageResource(R.drawable.card_background);
        base64Image = "";
    }

    // Адаптер для RecyclerView
    public class MotivationAdapter extends RecyclerView.Adapter<MotivationAdapter.MotivationViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> motivationList;

        public MotivationAdapter(Context context, ArrayList<Map<String, String>> motivationList) {
            this.context = context;
            this.motivationList = motivationList;
        }

        @NonNull
        @Override
        public MotivationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            return new MotivationViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull MotivationViewHolder holder, int position) {
            Map<String, String> motivation = motivationList.get(position);
            String displayText = "Название: " + motivation.get("name") + "\nID: " + motivation.get("id");
            holder.textView.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                editTextCategoryId.setText(motivation.get("categoryId"));
                editTextId.setText(motivation.get("id"));
                editTextName.setText(motivation.get("name"));

                String image = motivation.get("image");
                if (image != null && !image.isEmpty()) {
                    if (image.startsWith("http")) {
                        editTextImage.setText(image);
                        motivationImageView.setImageResource(R.drawable.card_background);
                    } else if (image.startsWith("image_")) {
                        editTextImage.setText(image);
                        int resId = context.getResources().getIdentifier(image, "drawable", context.getPackageName());
                        if (resId != 0) {
                            motivationImageView.setImageResource(resId);
                        } else {
                            motivationImageView.setImageResource(R.drawable.card_background);
                        }
                    } else {
                        Bitmap bitmap = decodeBase64(image);
                        if (bitmap != null) {
                            motivationImageView.setImageBitmap(bitmap);
                            editTextImage.setText("");
                        }
                    }
                } else {
                    motivationImageView.setImageResource(R.drawable.card_background);
                    editTextImage.setText("");
                }
            });
        }

        @Override
        public int getItemCount() {
            return motivationList.size();
        }

        public class MotivationViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MotivationViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}