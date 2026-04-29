package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.MemoryAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.ShakeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * MapActivity is the home screen of Memoriva.
 * It shows the 5 most recent memories and supports shake-to-random-memory.
 */
public class MapActivity extends BaseActivity {

    private static final int USER_ID = 1; // placeholder

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    private RecyclerView rvRecentMemories;
    private TextView tvEmpty;
    private MemoryAdapter memoryAdapter;
    private List<Memory> memories = new ArrayList<>();

    // Shake detection
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up RecyclerView
        rvRecentMemories = findViewById(R.id.rvRecentMemories);
        tvEmpty = findViewById(R.id.tvEmpty);

        memoryAdapter = new MemoryAdapter(this, memories);
        memoryAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        rvRecentMemories.setLayoutManager(new LinearLayoutManager(this));
        rvRecentMemories.setAdapter(memoryAdapter);

        // FAB → add new memory
        FloatingActionButton fabAddMemory = findViewById(R.id.fabAddMemory);
        fabAddMemory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCreateEditActivity.class);
            startActivity(intent);
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_home);

        // Set up shake detector
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(this::openRandomMemory);

        loadRecentMemories();
    }

    /** Loads the last 5 memories from SQLite and shows them in the list. */
    private void loadRecentMemories() {
        memories.clear();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> all = memoryDao.getMemoriesByUserId(db, USER_ID);
            // Take only the first 5 (already ordered by date DESC)
            int limit = Math.min(5, all.size());
            for (int i = 0; i < limit; i++) {
                memories.add(all.get(i));
            }
        }
        memoryAdapter.updateMemories(memories);

        if (memories.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvRecentMemories.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvRecentMemories.setVisibility(View.VISIBLE);
        }
    }

    /** Opens a random memory from SQLite. Called when the user shakes the phone. */
    private void openRandomMemory() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Memory random = memoryDao.getRandomMemory(db, USER_ID);
            if (random != null) {
                Intent intent = new Intent(this, MemoryDetailActivity.class);
                intent.putExtra("memory_id", random.getMemoryId());
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentMemories();
        // Register shake detector
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister shake detector to save battery
        sensorManager.unregisterListener(shakeDetector);
    }

    @Override
    public void onBackPressed() {
        // On home screen, pressing back exits the app
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
