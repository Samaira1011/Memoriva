package com.example.memoriva;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.MemoryAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.ShakeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapActivity extends BaseActivity {

    private static final int USER_ID = 1;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    private RecyclerView rvRecentMemories;
    private RecyclerView rvOnThisDay;
    private TextView tvEmpty;
    private TextView tvGreeting;
    private TextView tvOnThisDayDate;
    private TextView tvOnThisDayEmpty;
    private TextView btnToggleHome;
    private TextView btnToggleOnThisDay;
    private View scrollHome;
    private View panelOnThisDay;
    private MemoryAdapter memoryAdapter;
    private MemoryAdapter onThisDayAdapter;
    private List<Memory> memories = new ArrayList<>();
    private List<Memory> onThisDayMemories = new ArrayList<>();

    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        tvGreeting = findViewById(R.id.tvGreeting);
        setGreeting();

        btnToggleHome = findViewById(R.id.btnToggleList);
        btnToggleOnThisDay = findViewById(R.id.btnToggleMap);
        btnToggleHome.setOnClickListener(v -> showListView());
        btnToggleOnThisDay.setOnClickListener(v -> showOnThisDay());

        rvRecentMemories = findViewById(R.id.rvRecentMemories);
        tvEmpty = findViewById(R.id.tvEmpty);
        scrollHome = findViewById(R.id.scrollHome);
        panelOnThisDay = findViewById(R.id.osmMapView);
        rvOnThisDay = findViewById(R.id.rvOnThisDay);
        tvOnThisDayDate = findViewById(R.id.tvOnThisDayDate);
        tvOnThisDayEmpty = findViewById(R.id.tvOnThisDayEmpty);

        // Quick action buttons
        findViewById(R.id.btnQuickTrip).setOnClickListener(v ->
                startActivity(new Intent(this, MyTripsActivity.class)));
        findViewById(R.id.btnQuickCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class)));
        findViewById(R.id.btnQuickDream).setOnClickListener(v ->
                startActivity(new Intent(this, DreamBoardActivity.class)));
        findViewById(R.id.btnQuickStats).setOnClickListener(v ->
                startActivity(new Intent(this, TravelStatsActivity.class)));

        // See all
        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        tvSeeAll.setOnClickListener(v ->
                startActivity(new Intent(this, MemoryListActivity.class)));

        // Recent memories
        memoryAdapter = new MemoryAdapter(this, memories);
        memoryAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        rvRecentMemories.setLayoutManager(new LinearLayoutManager(this));
        rvRecentMemories.setAdapter(memoryAdapter);

        // On This Day memories
        onThisDayAdapter = new MemoryAdapter(this, onThisDayMemories);
        onThisDayAdapter.setOnMemoryClickListener(memory -> {
            Intent intent = new Intent(this, MemoryDetailActivity.class);
            intent.putExtra("memory_id", memory.getMemoryId());
            startActivity(intent);
        });
        rvOnThisDay.setLayoutManager(new LinearLayoutManager(this));
        rvOnThisDay.setAdapter(onThisDayAdapter);

        // FAB
        FloatingActionButton fabAddMemory = findViewById(R.id.fabAddMemory);
        fabAddMemory.setOnClickListener(v ->
                startActivity(new Intent(this, MemoryCreateEditActivity.class)));

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_home);

        // Shake
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(this::openRandomMemory);

        loadRecentMemories();
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Good morning ☀️";
        else if (hour < 17) greeting = "Good afternoon 🌤";
        else greeting = "Good evening 🌙";

        SharedPreferences prefs = getSharedPreferences("memoriva_prefs", MODE_PRIVATE);
        String name = prefs.getString("display_name", "");
        if (name != null && !name.isEmpty()) {
            greeting += ", " + name.split(" ")[0] + "!";
        }
        tvGreeting.setText(greeting);
    }

    private void loadOnThisDayMemories() {
        onThisDayMemories.clear();

        // Get today's month-day
        String today = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date());
        String todayFull = new SimpleDateFormat("MMMM d", Locale.getDefault()).format(new Date());
        tvOnThisDayDate.setText("Memories from " + todayFull + " in past years");

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> all = memoryDao.getMemoriesByUserId(db, USER_ID);
            String currentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
            for (Memory m : all) {
                if (m.getDate() != null && m.getDate().length() >= 10) {
                    String monthDay = m.getDate().substring(5); // MM-dd
                    String year = m.getDate().substring(0, 4);
                    if (monthDay.equals(today) && !year.equals(currentYear)) {
                        onThisDayMemories.add(m);
                    }
                }
            }
        }

        onThisDayAdapter.updateMemories(onThisDayMemories);
        if (onThisDayMemories.isEmpty()) {
            tvOnThisDayEmpty.setVisibility(View.VISIBLE);
            rvOnThisDay.setVisibility(View.GONE);
        } else {
            tvOnThisDayEmpty.setVisibility(View.GONE);
            rvOnThisDay.setVisibility(View.VISIBLE);
        }
    }

    public void showOnThisDay() {
        panelOnThisDay.setVisibility(View.VISIBLE);
        scrollHome.setVisibility(View.GONE);

        btnToggleOnThisDay.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        btnToggleOnThisDay.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnToggleHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        btnToggleHome.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));

        loadOnThisDayMemories();
    }

    public void showListView() {
        panelOnThisDay.setVisibility(View.GONE);
        scrollHome.setVisibility(View.VISIBLE);

        btnToggleHome.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        btnToggleHome.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnToggleOnThisDay.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        btnToggleOnThisDay.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));

        if (memories.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvRecentMemories.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvRecentMemories.setVisibility(View.VISIBLE);
        }
    }

    // Keep showMapView as alias for BaseActivity compatibility
    public void showMapView() {
        showOnThisDay();
    }

    private void loadRecentMemories() {
        memories.clear();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> all = memoryDao.getMemoriesByUserId(db, USER_ID);
            int limit = Math.min(5, all.size());
            for (int i = 0; i < limit; i++) memories.add(all.get(i));
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
        setGreeting();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
