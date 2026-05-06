package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.TimelineAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.TripDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.models.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TripTimelineActivity extends BaseActivity {

    private int tripId;
    private MemorivaDbHelper dbHelper;
    private TripDao tripDao;
    private MemoryDao memoryDao;
    private TimelineAdapter timelineAdapter;
    private List<Memory> memories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_timeline);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        dbHelper = new MemorivaDbHelper(this);
        tripDao = new TripDao();
        memoryDao = new MemoryDao();

        tripId = getIntent().getIntExtra("trip_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvTimeline = findViewById(R.id.rvTimeline);
        timelineAdapter = new TimelineAdapter(this, memories);
        timelineAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_home);

        loadTripData();
    }

    private void loadTripData() {
        if (tripId == -1) {
            finish();
            return;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Trip trip = tripDao.getTripById(db, tripId);
            if (trip != null) {
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle(trip.getName());

                // Load stats
                int days = tripDao.getTripDaysCount(db, tripId);
                int places = tripDao.getTripPlacesCount(db, tripId);
                int photos = tripDao.getTripPhotosCount(db, tripId);

                ((TextView) findViewById(R.id.tvDaysCount)).setText(String.valueOf(days));
                ((TextView) findViewById(R.id.tvPlacesCount)).setText(String.valueOf(places));
                ((TextView) findViewById(R.id.tvPhotosCount)).setText(String.valueOf(photos));
            }

            // Load memories for this trip
            memories.clear();
            memories.addAll(memoryDao.getMemoriesByTripId(db, tripId));
        }
        timelineAdapter.updateMemories(memories);
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
