package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.PhotoAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.ShareHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryDetailActivity shows the full details of a single memory.
 * It supports sharing via text, email, or SMS, and editing/deleting.
 */
public class MemoryDetailActivity extends BaseActivity {

    private int memoryId;
    private Memory memory;
    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    private TextView tvTitle, tvDate, tvLocation, tvNotes;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> photoPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_detail);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        memoryId = getIntent().getIntExtra("memory_id", -1);

        // Set up toolbar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvNotes = findViewById(R.id.tvNotes);
        rvPhotos = findViewById(R.id.rvPhotos);

        photoAdapter = new PhotoAdapter(this, photoPaths);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        // Share button → share as plain text
        MaterialButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> shareMemory());

        // Time Capsule button
        MaterialButton btnTimeCapsule = findViewById(R.id.btnTimeCapsule);
        btnTimeCapsule.setOnClickListener(v -> {
            Intent intent = new Intent(this, TimeCapsuleActivity.class);
            intent.putExtra("memory_id", memoryId);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_memories);

        loadMemory();
    }

    private void loadMemory() {
        if (memoryId == -1) {
            finish();
            return;
        }
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            memory = memoryDao.getMemoryById(db, memoryId);
        }
        if (memory == null) {
            finish();
            return;
        }

        tvTitle.setText(memory.getTitle());
        tvDate.setText(memory.getDate() != null ? memory.getDate() : "");
        tvNotes.setText(memory.getNotes() != null ? memory.getNotes() : "");
        tvLocation.setText("");

        photoPaths.clear();
        photoPaths.addAll(memory.getPhotoPathList());
        photoAdapter.notifyDataSetChanged();
    }

    /** Share memory as plain text using the system share sheet. */
    private void shareMemory() {
        if (memory == null) return;
        ShareHelper.shareText(
                this,
                "Share Memory",
                "📸 " + memory.getTitle()
                        + "\n📅 " + memory.getDate()
                        + (memory.getNotes() != null && !memory.getNotes().isEmpty()
                            ? "\n" + memory.getNotes() : "")
                        + "\n\nShared from Memoriva"
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_memory_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;

        } else if (id == R.id.action_edit) {
            Intent editIntent = new Intent(this, MemoryCreateEditActivity.class);
            editIntent.putExtra("memory_id", memoryId);
            startActivity(editIntent);
            return true;

        } else if (id == R.id.action_delete) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Memory")
                    .setMessage("Are you sure you want to delete this memory?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                            memoryDao.deleteMemory(db, memoryId);
                        }
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;

        } else if (id == R.id.action_share_email) {
            if (memory != null) {
                ShareHelper.shareViaEmail(
                        this,
                        "My Memory: " + memory.getTitle(),
                        "Check out my memory: " + memory.getTitle()
                                + " on " + memory.getDate()
                                + "\n\nSent from Memoriva"
                );
            }
            return true;

        } else if (id == R.id.action_share_sms) {
            if (memory != null) {
                ShareHelper.shareViaSMS(
                        this,
                        "My memory: " + memory.getTitle()
                                + " on " + memory.getDate()
                                + " - Memoriva"
                );
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
