package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.MemoryAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MemoryListActivity shows all memories for a given date (or all memories).
 * Supports sorting newest-first or oldest-first via the options menu.
 */
public class MemoryListActivity extends BaseActivity {

    private RecyclerView rvMemories;
    private TextView tvEmpty;
    private MemoryAdapter memoryAdapter;
    private List<Memory> memories = new ArrayList<>();
    private String date;
    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    // true = newest first (default), false = oldest first
    private boolean sortNewest = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_list);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        date = getIntent().getStringExtra("date");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(date != null ? date : "Memories");
        }

        rvMemories = findViewById(R.id.rvMemories);
        tvEmpty = findViewById(R.id.tvEmpty);

        memoryAdapter = new MemoryAdapter(this, memories);
        memoryAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        memoryAdapter.setOnMemoryLongClickListener(new MemoryAdapter.OnMemoryLongClickListener() {
            @Override
            public void onMemoryEdit(Memory memory) {
                Intent intent = new Intent(MemoryListActivity.this, MemoryCreateEditActivity.class);
                intent.putExtra("memory_id", memory.getMemoryId());
                startActivity(intent);
            }

            @Override
            public void onMemoryDelete(Memory memory) {
                new androidx.appcompat.app.AlertDialog.Builder(MemoryListActivity.this)
                        .setTitle("Delete Memory")
                        .setMessage("Are you sure you want to delete this memory?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                                memoryDao.deleteMemory(db, memory.getMemoryId());
                            }
                            loadMemories();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onMemoryShare(Memory memory) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Check out my memory: " + memory.getTitle() + " on " + memory.getDate());
                startActivity(Intent.createChooser(shareIntent, "Share Memory"));
            }
        });

        rvMemories.setLayoutManager(new LinearLayoutManager(this));
        rvMemories.setAdapter(memoryAdapter);

        FloatingActionButton fabAddMemory = findViewById(R.id.fabAddMemory);
        fabAddMemory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCreateEditActivity.class);
            if (date != null) intent.putExtra("date", date);
            startActivity(intent);
        });

        loadMemories();
    }

    private void loadMemories() {
        memories.clear();
        int userId = 1; // placeholder
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> loaded;
            if (date != null && !date.isEmpty()) {
                loaded = memoryDao.getMemoriesByDate(db, userId, date);
            } else {
                loaded = memoryDao.getMemoriesByUserId(db, userId);
            }
            memories.addAll(loaded);
        }

        // Apply sort order
        if (!sortNewest) {
            // Reverse to get oldest first (list is already newest-first from DAO)
            Collections.reverse(memories);
        }

        memoryAdapter.updateMemories(memories);
        tvEmpty.setVisibility(memories.isEmpty() ? View.VISIBLE : View.GONE);
        rvMemories.setVisibility(memories.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_memory_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_sort_newest) {
            sortNewest = true;
            loadMemories();
            return true;
        } else if (id == R.id.action_sort_oldest) {
            sortNewest = false;
            loadMemories();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMemories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
