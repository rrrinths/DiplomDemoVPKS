package com.example.diplomdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class CategoryAdminActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Map<String, String>> categoryList;
    private EditText editTextId, editTextTitle, editTextDescription, editTextImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_admin);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextId = findViewById(R.id.editTextId);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);

        getCategoriesFromFirebase();

        findViewById(R.id.buttonAdd).setOnClickListener(v -> addCategory());
        findViewById(R.id.buttonUpdate).setOnClickListener(v -> updateCategory());
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteCategory());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = CategoryAdminActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        Button button = findViewById(R.id.nav_home);
        button.setOnClickListener(v -> startActivity(new Intent(CategoryAdminActivity.this, AdminMainActivity.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getCategoriesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Category");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> category = new HashMap<>();
                    category.put("id", snapshot.child("id").getValue(String.class));
                    category.put("title", snapshot.child("title").getValue(String.class));
                    category.put("description", snapshot.child("description").getValue(String.class));
                    category.put("imageUrl", snapshot.child("imageUrl").getValue(String.class));
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CategoryAdminActivity.this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCategory() {
        String id = editTextId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        if (!id.isEmpty() && !title.isEmpty() && !description.isEmpty() && !imageUrl.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Category").child(id);
            Map<String, String> category = new HashMap<>();
            category.put("id", id);
            category.put("title", title);
            category.put("description", description);
            category.put("imageUrl", imageUrl);

            databaseReference.setValue(category);
            Toast.makeText(this, "Категория добавлена", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCategory() {
        String id = editTextId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Category").child(id);
            Map<String, String> category = new HashMap<>();
            category.put("title", title);
            category.put("description", description);
            category.put("imageUrl", imageUrl);

            databaseReference.updateChildren((Map) category);
            Toast.makeText(this, "Категория обновлена", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCategory() {
        String id = editTextId.getText().toString();
        if (!id.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Category").child(id);
            databaseReference.removeValue();
            Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show();
        }
    }

    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> categoryList;

        public CategoryAdapter(Context context, ArrayList<Map<String, String>> categoryList) {
            this.context = context;
            this.categoryList = categoryList;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            return new CategoryViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Map<String, String> category = categoryList.get(position);
            String displayText = "Title: " + category.get("title") + "\nDescription: " + category.get("description");
            holder.textView.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                editTextId.setText(category.get("id"));
                editTextTitle.setText(category.get("title"));
                editTextDescription.setText(category.get("description"));
                editTextImageUrl.setText(category.get("imageUrl"));
            });
        }

        @Override
        public int getItemCount() {
            return categoryList.size();
        }

        public class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public CategoryViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
