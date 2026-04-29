package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.MemoryAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnThisDayActivity extends AppCompatActivity {

    private static final int USER_ID = 1; // placeholder

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private MemoryAdapter memoryAdapter;
    private List<Memory> memories = new ArrayList<>();
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_this_day);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("On This Day \uD83D\uDDD3\uFE0F");
        }

        TextView tvDate = findViewById(R.id.tvDate);
        SimpleDateFormat dateFmt = new SimpleDateFormat("MMMM d", Locale.getDefault());
        tvDate.setText(dateFmt.format(new Date()));

        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rvMemories = findViewById(R.id.rvMemories);
        rvMemories.setLayoutManager(new LinearLayoutManager(this));

        memoryAdapter = new MemoryAdapter(this, memories);
        memoryAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        rvMemories.setAdapter(memoryAdapter);

        loadMemories();
    }

    private void loadMemories() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String monthDay = sdf.format(new Date());

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            memories.clear();
            memories.addAll(memoryDao.getMemoriesOnThisDay(db, USER_ID, monthDay));
            memoryAdapter.notifyDataSetChanged();
        }

        tvEmpty.setVisibility(memories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
