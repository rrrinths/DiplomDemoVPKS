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

public class YogaAdminActivity extends AppCompatActivity {

    private RecyclerView yogaRecyclerView;
    private YogaAdapter yogaAdapter;
    private ArrayList<Map<String, String>> yogaList;
    private EditText editTextCategoryId, editTextId, editTextTitle, editTextDescription, editTextImageUrl;
    private EditText editTextUpr1, editTextUpr2, editTextUpr3, editTextUpr4, editTextUpr5, editTextUpr6, editTextUpr7, editTextUpr8;
    private ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6, imageView7, imageView8;

    private static final int PICK_IMAGE_REQUEST = 1;
    private int currentImageIndex = 0;
    private String[] base64Images = new String[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_admin);

        initViews();

        yogaList = new ArrayList<>();
        yogaAdapter = new YogaAdapter(this, yogaList);
        yogaRecyclerView.setAdapter(yogaAdapter);

        getYogaFromFirebase();
        setupListeners();
        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = YogaAdminActivity.this.getCurrentFocus();
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
        yogaRecyclerView = findViewById(R.id.yogaRecyclerView);
        yogaRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextCategoryId = findViewById(R.id.editTextCategoryId);
        editTextId = findViewById(R.id.editTextId);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        imageView6 = findViewById(R.id.imageView6);
        imageView7 = findViewById(R.id.imageView7);
        imageView8 = findViewById(R.id.imageView8);

        imageView1.setTag(1);
        imageView2.setTag(2);
        imageView3.setTag(3);
        imageView4.setTag(4);
        imageView5.setTag(5);
        imageView6.setTag(6);
        imageView7.setTag(7);
        imageView8.setTag(8);

        editTextUpr1 = findViewById(R.id.editTextUpr1);
        editTextUpr2 = findViewById(R.id.editTextUpr2);
        editTextUpr3 = findViewById(R.id.editTextUpr3);
        editTextUpr4 = findViewById(R.id.editTextUpr4);
        editTextUpr5 = findViewById(R.id.editTextUpr5);
        editTextUpr6 = findViewById(R.id.editTextUpr6);
        editTextUpr7 = findViewById(R.id.editTextUpr7);
        editTextUpr8 = findViewById(R.id.editTextUpr8);
    }

    private void setupListeners() {
        imageView1.setOnClickListener(v -> openImagePicker(0));
        imageView2.setOnClickListener(v -> openImagePicker(1));
        imageView3.setOnClickListener(v -> openImagePicker(2));
        imageView4.setOnClickListener(v -> openImagePicker(3));
        imageView5.setOnClickListener(v -> openImagePicker(4));
        imageView6.setOnClickListener(v -> openImagePicker(5));
        imageView7.setOnClickListener(v -> openImagePicker(6));
        imageView8.setOnClickListener(v -> openImagePicker(7));

        findViewById(R.id.buttonAdd).setOnClickListener(v -> addYoga());
        findViewById(R.id.buttonUpdate).setOnClickListener(v -> updateYoga());
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteYoga());

        Button button = findViewById(R.id.nav_home);
        button.setOnClickListener(v -> startActivity(new Intent(YogaAdminActivity.this, AdminMainActivity.class)));
    }

    private void openImagePicker(int imageIndex) {
        currentImageIndex = imageIndex;
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
                setImageToView(bitmap, currentImageIndex);
                base64Images[currentImageIndex] = convertToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImageToView(Bitmap bitmap, int index) {
        switch (index) {
            case 0: imageView1.setImageBitmap(bitmap); break;
            case 1: imageView2.setImageBitmap(bitmap); break;
            case 2: imageView3.setImageBitmap(bitmap); break;
            case 3: imageView4.setImageBitmap(bitmap); break;
            case 4: imageView5.setImageBitmap(bitmap); break;
            case 5: imageView6.setImageBitmap(bitmap); break;
            case 6: imageView7.setImageBitmap(bitmap); break;
            case 7: imageView8.setImageBitmap(bitmap); break;
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

    private void getYogaFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Yoga");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                yogaList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> yoga = new HashMap<>();
                    yoga.put("categoryId", snapshot.child("categoryId").getValue(String.class));
                    yoga.put("id", snapshot.child("id").getValue(String.class));
                    yoga.put("title", snapshot.child("title").getValue(String.class));
                    yoga.put("description", snapshot.child("description").getValue(String.class));
                    yoga.put("imageUrl", snapshot.child("imageUrl").getValue(String.class));

                    // Изображения
                    yoga.put("img1", snapshot.child("img1").getValue(String.class));
                    yoga.put("img2", snapshot.child("img2").getValue(String.class));
                    yoga.put("img3", snapshot.child("img3").getValue(String.class));
                    yoga.put("img4", snapshot.child("img4").getValue(String.class));
                    yoga.put("img5", snapshot.child("img5").getValue(String.class));
                    yoga.put("img6", snapshot.child("img6").getValue(String.class));
                    yoga.put("img7", snapshot.child("img7").getValue(String.class));
                    yoga.put("img8", snapshot.child("img8").getValue(String.class));

                    // Упражнения
                    yoga.put("upr1", snapshot.child("upr1").getValue(String.class));
                    yoga.put("upr2", snapshot.child("upr2").getValue(String.class));
                    yoga.put("upr3", snapshot.child("upr3").getValue(String.class));
                    yoga.put("upr4", snapshot.child("upr4").getValue(String.class));
                    yoga.put("upr5", snapshot.child("upr5").getValue(String.class));
                    yoga.put("upr6", snapshot.child("upr6").getValue(String.class));
                    yoga.put("upr7", snapshot.child("upr7").getValue(String.class));
                    yoga.put("upr8", snapshot.child("upr8").getValue(String.class));

                    yogaList.add(yoga);
                }
                yogaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(YogaAdminActivity.this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addYoga() {
        String categoryId = editTextCategoryId.getText().toString();
        String id = editTextId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        String upr1 = editTextUpr1.getText().toString();
        String upr2 = editTextUpr2.getText().toString();
        String upr3 = editTextUpr3.getText().toString();
        String upr4 = editTextUpr4.getText().toString();
        String upr5 = editTextUpr5.getText().toString();
        String upr6 = editTextUpr6.getText().toString();
        String upr7 = editTextUpr7.getText().toString();
        String upr8 = editTextUpr8.getText().toString();

        if (!categoryId.isEmpty() && !id.isEmpty() && !title.isEmpty() && !description.isEmpty() && !imageUrl.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Yoga").child(id);
            Map<String, Object> yoga = new HashMap<>();
            yoga.put("categoryId", categoryId);
            yoga.put("id", id);
            yoga.put("title", title);
            yoga.put("description", description);
            yoga.put("imageUrl", imageUrl);

            for (int i = 0; i < base64Images.length; i++) {
                if (base64Images[i] != null) {
                    yoga.put("img" + (i + 1), base64Images[i]);
                } else {
                    yoga.put("img" + (i + 1), "");
                }
            }

            yoga.put("upr1", upr1);
            yoga.put("upr2", upr2);
            yoga.put("upr3", upr3);
            yoga.put("upr4", upr4);
            yoga.put("upr5", upr5);
            yoga.put("upr6", upr6);
            yoga.put("upr7", upr7);
            yoga.put("upr8", upr8);

            databaseReference.setValue(yoga)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(YogaAdminActivity.this, "Практика йоги добавлена", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(YogaAdminActivity.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateYoga() {
        String id = editTextId.getText().toString();
        String categoryId = editTextCategoryId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        String upr1 = editTextUpr1.getText().toString();
        String upr2 = editTextUpr2.getText().toString();
        String upr3 = editTextUpr3.getText().toString();
        String upr4 = editTextUpr4.getText().toString();
        String upr5 = editTextUpr5.getText().toString();
        String upr6 = editTextUpr6.getText().toString();
        String upr7 = editTextUpr7.getText().toString();
        String upr8 = editTextUpr8.getText().toString();

        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Yoga").child(id);
            Map<String, Object> updates = new HashMap<>();
            updates.put("categoryId", categoryId);
            updates.put("title", title);
            updates.put("description", description);
            updates.put("imageUrl", imageUrl);

            for (int i = 0; i < base64Images.length; i++) {
                if (base64Images[i] != null) {
                    updates.put("img" + (i + 1), base64Images[i]);
                }
            }

            updates.put("upr1", upr1);
            updates.put("upr2", upr2);
            updates.put("upr3", upr3);
            updates.put("upr4", upr4);
            updates.put("upr5", upr5);
            updates.put("upr6", upr6);
            updates.put("upr7", upr7);
            updates.put("upr8", upr8);

            databaseReference.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(YogaAdminActivity.this, "Практика йоги обновлена", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(YogaAdminActivity.this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Укажите ID практики", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteYoga() {
        String id = editTextId.getText().toString();
        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Yoga").child(id);
            databaseReference.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(YogaAdminActivity.this, "Практика йоги удалена", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(YogaAdminActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Укажите ID практики", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        editTextCategoryId.setText("");
        editTextId.setText("");
        editTextTitle.setText("");
        editTextDescription.setText("");
        editTextImageUrl.setText("");

        editTextUpr1.setText("");
        editTextUpr2.setText("");
        editTextUpr3.setText("");
        editTextUpr4.setText("");
        editTextUpr5.setText("");
        editTextUpr6.setText("");
        editTextUpr7.setText("");
        editTextUpr8.setText("");

        imageView1.setImageResource(R.drawable.defaultupr);
        imageView2.setImageResource(R.drawable.defaultupr);
        imageView3.setImageResource(R.drawable.defaultupr);
        imageView4.setImageResource(R.drawable.defaultupr);
        imageView5.setImageResource(R.drawable.defaultupr);
        imageView6.setImageResource(R.drawable.defaultupr);
        imageView7.setImageResource(R.drawable.defaultupr);
        imageView8.setImageResource(R.drawable.defaultupr);

        base64Images = new String[8];
    }

    public class YogaAdapter extends RecyclerView.Adapter<YogaAdapter.YogaViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> yogaList;

        public YogaAdapter(Context context, ArrayList<Map<String, String>> yogaList) {
            this.context = context;
            this.yogaList = yogaList;
        }

        @NonNull
        @Override
        public YogaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            return new YogaViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull YogaViewHolder holder, int position) {
            Map<String, String> yoga = yogaList.get(position);
            String displayText = "Название: " + yoga.get("title") + "\nОписание: " + yoga.get("description");
            holder.textView.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                editTextCategoryId.setText(yoga.get("categoryId"));
                editTextId.setText(yoga.get("id"));
                editTextTitle.setText(yoga.get("title"));
                editTextDescription.setText(yoga.get("description"));
                editTextImageUrl.setText(yoga.get("imageUrl"));

                editTextUpr1.setText(yoga.get("upr1"));
                editTextUpr2.setText(yoga.get("upr2"));
                editTextUpr3.setText(yoga.get("upr3"));
                editTextUpr4.setText(yoga.get("upr4"));
                editTextUpr5.setText(yoga.get("upr5"));
                editTextUpr6.setText(yoga.get("upr6"));
                editTextUpr7.setText(yoga.get("upr7"));
                editTextUpr8.setText(yoga.get("upr8"));

                setImageFromBase64(yoga.get("img1"), imageView1, 0);
                setImageFromBase64(yoga.get("img2"), imageView2, 1);
                setImageFromBase64(yoga.get("img3"), imageView3, 2);
                setImageFromBase64(yoga.get("img4"), imageView4, 3);
                setImageFromBase64(yoga.get("img5"), imageView5, 4);
                setImageFromBase64(yoga.get("img6"), imageView6, 5);
                setImageFromBase64(yoga.get("img7"), imageView7, 6);
                setImageFromBase64(yoga.get("img8"), imageView8, 7);
            });
        }

        private void setImageFromBase64(String base64, ImageView imageView, int index) {
            if (base64 != null && !base64.isEmpty()) {
                Bitmap bitmap = decodeBase64(base64);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    base64Images[index] = base64;
                }
            } else {
                imageView.setImageResource(R.drawable.defaultupr);
                base64Images[index] = null;
            }
        }

        @Override
        public int getItemCount() {
            return yogaList.size();
        }

        public class YogaViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public YogaViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}