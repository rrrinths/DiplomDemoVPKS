package com.example.diplomdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;  // Правильный импорт ViewGroup
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

public class MeditationAdminActivity extends AppCompatActivity {
    private RecyclerView meditationRecyclerView;
    private MeditationAdapter meditationAdapter;
    private ArrayList<Map<String, String>> meditationList;
    private EditText editTextId, editTextTitle, editTextDescription, editTextMeditationText, editTextImageUrl, editTextCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meditation_admin);

        meditationRecyclerView = findViewById(R.id.meditationRecyclerView);
        meditationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextId = findViewById(R.id.editTextId);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextMeditationText = findViewById(R.id.editTextMeditationText);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextCategoryId = findViewById(R.id.editTextCategoryId);

        meditationList = new ArrayList<>();
        meditationAdapter = new MeditationAdapter(this, meditationList);
        meditationRecyclerView.setAdapter(meditationAdapter);

        getMeditationsFromFirebase();

        findViewById(R.id.buttonAdd).setOnClickListener(v -> addMeditation());
        findViewById(R.id.buttonUpdate).setOnClickListener(v -> updateMeditation());
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteMeditation());

        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = MeditationAdminActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        Button button = findViewById(R.id.nav_home);
        button.setOnClickListener(v -> startActivity(new Intent(MeditationAdminActivity.this, AdminMainActivity.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getMeditationsFromFirebase() {
        DatabaseReference meditationRef = FirebaseDatabase.getInstance().getReference("meditation");

        meditationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                meditationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> meditation = new HashMap<>();
                    meditation.put("id", snapshot.child("id").getValue(String.class));
                    meditation.put("categoryId", snapshot.child("categoryId").getValue(String.class));
                    meditation.put("description", snapshot.child("description").getValue(String.class));
                    meditation.put("imageUrl", snapshot.child("imageUrl").getValue(String.class));
                    meditation.put("meditationtext", snapshot.child("meditationtext").getValue(String.class));
                    meditation.put("title", snapshot.child("title").getValue(String.class));

                    meditationList.add(meditation);
                }
                meditationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MeditationAdminActivity.this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMeditation() {
        String id = editTextId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String meditationText = editTextMeditationText.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        if (!id.isEmpty() && !title.isEmpty() && !description.isEmpty() && !meditationText.isEmpty() && !imageUrl.isEmpty()) {
            DatabaseReference meditationRef = FirebaseDatabase.getInstance().getReference("meditation").child(id);
            Map<String, String> meditation = new HashMap<>();
            meditation.put("id", id);
            meditation.put("categoryId", "meditation");
            meditation.put("description", description);
            meditation.put("imageUrl", imageUrl);
            meditation.put("meditationtext", meditationText);
            meditation.put("title", title);

            meditationRef.setValue(meditation);
            Toast.makeText(this, "Медитация добавлена", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMeditation() {
        String id = editTextId.getText().toString();
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String meditationText = editTextMeditationText.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();

        if (!id.isEmpty()) {
            DatabaseReference meditationRef = FirebaseDatabase.getInstance().getReference("meditation").child(id);
            Map<String, Object> meditation = new HashMap<>();
            meditation.put("title", title);
            meditation.put("description", description);
            meditation.put("meditationtext", meditationText);
            meditation.put("imageUrl", imageUrl);

            meditationRef.updateChildren(meditation);
            Toast.makeText(this, "Медитация обновлена", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMeditation() {
        String id = editTextId.getText().toString();
        if (!id.isEmpty()) {
            DatabaseReference meditationRef = FirebaseDatabase.getInstance().getReference("meditation").child(id);
            meditationRef.removeValue();
            Toast.makeText(this, "Медитация удалена", Toast.LENGTH_SHORT).show();
        }
    }


    // Адаптер для RecyclerView
    public class MeditationAdapter extends RecyclerView.Adapter<MeditationAdapter.MeditationViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> meditationList;

        public MeditationAdapter(Context context, ArrayList<Map<String, String>> meditationList) {
            this.context = context;
            this.meditationList = meditationList;
        }

        @NonNull
        @Override
        public MeditationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            return new MeditationViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull MeditationViewHolder holder, int position) {
            Map<String, String> meditation = meditationList.get(position);
            String displayText = "Title: " + meditation.get("title") + "\nDescription: " + meditation.get("description");
            holder.textView.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                editTextId.setText(meditation.get("id"));
                editTextTitle.setText(meditation.get("title"));
                editTextDescription.setText(meditation.get("description"));
                editTextMeditationText.setText(meditation.get("meditationtext"));
                editTextImageUrl.setText(meditation.get("imageUrl"));
                editTextCategoryId.setText(meditation.get("categoryId"));
            });
        }

        @Override
        public int getItemCount() {
            return meditationList.size();
        }

        public class MeditationViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MeditationViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
