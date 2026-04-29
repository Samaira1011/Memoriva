package com.example.memoriva;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.CalendarAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.CalendarDay;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.ShakeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarActivity extends BaseActivity {

    private static final String PREFS_NAME = "memoriva_prefs";
    private static final String KEY_USER_ID = "user_id";

    private TextView tvMonthYear;
    private RecyclerView rvCalendar;
    private CalendarAdapter calendarAdapter;
    private Calendar currentCalendar;
    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private int userId = -1;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userIdStr = prefs.getString(KEY_USER_ID, null);
        // userId stored as firebase uid string; for SQLite we use a local int id
        // We'll use -1 as fallback (no memories shown) if not resolved
        userId = -1;

        currentCalendar = Calendar.getInstance();

        tvMonthYear = findViewById(R.id.tvMonthYear);
        rvCalendar = findViewById(R.id.rvCalendar);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 7);
        rvCalendar.setLayoutManager(gridLayoutManager);

        calendarAdapter = new CalendarAdapter(this, new ArrayList<>());
        calendarAdapter.setOnDayClickListener(day -> {
            if (!day.isPadding() && day.getDateString() != null && !day.getDateString().isEmpty()) {
                Intent intent = new Intent(this, MemoryListActivity.class);
                intent.putExtra("date", day.getDateString());
                startActivity(intent);
            }
        });
        rvCalendar.setAdapter(calendarAdapter);

        ImageButton btnPrev = findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = findViewById(R.id.btnNextMonth);

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadCalendar();
        });

        FloatingActionButton fabAddMemory = findViewById(R.id.fabAddMemory);
        fabAddMemory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCreateEditActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_memories);

        // Shake detector setup
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(() -> openRandomMemory());

        loadCalendar();
    }

    private void loadCalendar() {
        SimpleDateFormat monthYearFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthYearFmt.format(currentCalendar.getTime()));

        // Get dates that have memories
        Set<String> datesWithMemories = new HashSet<>();
        if (userId > 0) {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                List<Memory> memories = memoryDao.getMemoriesByUserId(db, userId);
                SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String currentMonth = dateFmt.format(currentCalendar.getTime());
                for (Memory m : memories) {
                    if (m.getDate() != null && m.getDate().startsWith(currentMonth)) {
                        datesWithMemories.add(m.getDate());
                    }
                }
            }
        }

        List<CalendarDay> days = buildCalendarDays(datesWithMemories);
        calendarAdapter.updateDays(days);
    }

    private List<CalendarDay> buildCalendarDays(Set<String> datesWithMemories) {
        List<CalendarDay> days = new ArrayList<>();

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = dateFmt.format(today.getTime());

        // Padding cells before the 1st
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay("", 0, false, false, false, true));
        }

        // Actual day cells
        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = dateFmt.format(cal.getTime());
            boolean hasMemory = datesWithMemories.contains(dateStr);
            boolean isToday = dateStr.equals(todayStr);
            days.add(new CalendarDay(dateStr, day, hasMemory, false, isToday, false));
        }

        return days;
    }

    private void openRandomMemory() {
        if (userId <= 0) return;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Memory memory = memoryDao.getRandomMemory(db, userId);
            if (memory != null) {
                Intent intent = new Intent(this, MemoryDetailActivity.class);
                intent.putExtra("memory_id", memory.getMemoryId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No memories found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null && shakeDetector != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
