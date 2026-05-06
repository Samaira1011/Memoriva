package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.GridPhotoAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.TripDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.models.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TripDetailActivity extends BaseActivity {

    private int tripId;
    private Trip trip;
    private MemorivaDbHelper dbHelper;
    private TripDao tripDao;
    private MemoryDao memoryDao;

    private ImageView ivHero;
    private TextView tvTripName, tvDateRange, tvNotes, tvGalleryEmpty;
    private RecyclerView rvGallery;
    private GridPhotoAdapter galleryAdapter;
    private List<String> galleryPhotos = new ArrayList<>();

    // Parallel list to track which memory each photo belongs to
    private List<Integer> photoMemoryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_detail);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
                ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                (v, insets) -> {
                    androidx.core.graphics.Insets systemBars =
                            insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    boolean changed = v.getPaddingLeft() != systemBars.left
                            || v.getPaddingTop() != systemBars.top
                            || v.getPaddingRight() != systemBars.right
                            || v.getPaddingBottom() != systemBars.bottom;
                    if (changed) {
                        v.setPadding(systemBars.left, systemBars.top,
                                systemBars.right, systemBars.bottom);
                    }
                    return insets;
                });

        dbHelper = new MemorivaDbHelper(this);
        tripDao = new TripDao();
        memoryDao = new MemoryDao();

        tripId = getIntent().getIntExtra("trip_id", -1);

        ivHero = findViewById(R.id.ivHero);
        tvTripName = findViewById(R.id.tvTripName);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvNotes = findViewById(R.id.tvNotes);
        tvGalleryEmpty = findViewById(R.id.tvGalleryEmpty);
        rvGallery = findViewById(R.id.rvGallery);

        // 3-column grid
        galleryAdapter = new GridPhotoAdapter(this, galleryPhotos);
        galleryAdapter.setOnPhotoClickListener(position -> {
            if (position < photoMemoryIds.size()) {
                int memId = photoMemoryIds.get(position);
                if (memId != -1) {
                    Intent intent = new Intent(this, MemoryDetailActivity.class);
                    intent.putExtra("memory_id", memId);
                    startActivity(intent);
                }
            }
        });
        rvGallery.setLayoutManager(new GridLayoutManager(this, 3));
        rvGallery.setAdapter(galleryAdapter);

        // Add Memory to Trip button
        MaterialButton btnAddMemory = findViewById(R.id.btnAddMemoryToTrip);
        btnAddMemory.setOnClickListener(v -> showAddMemoryDialog());

        MaterialButton btnShareMemory = findViewById(R.id.btnShareMemory);
        btnShareMemory.setOnClickListener(v -> shareTrip());

        MaterialButton btnAddToStory = findViewById(R.id.btnAddToStory);
        btnAddToStory.setOnClickListener(v -> {
            if (trip == null) return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    "My trip to " + trip.getDestination() + ": " + trip.getName());
            startActivity(Intent.createChooser(intent, "Add to Story"));
        });

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
            trip = tripDao.getTripById(db, tripId);
            if (trip == null) {
                finish();
                return;
            }

            tvTripName.setText(trip.getName());
            String dateRange = "";
            if (trip.getStartDate() != null && trip.getEndDate() != null) {
                dateRange = trip.getStartDate() + " – " + trip.getEndDate();
            }
            tvDateRange.setText(dateRange);
            tvNotes.setText(trip.getDescription() != null ? trip.getDescription() : "");

            // Load cover photo
            if (trip.getCoverPhotoPath() != null && !trip.getCoverPhotoPath().isEmpty()) {
                String path = trip.getCoverPhotoPath();
                Object source = path.startsWith("content://")
                        ? android.net.Uri.parse(path) : new java.io.File(path);
                com.bumptech.glide.Glide.with(this)
                        .load(source)
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo)
                        .into(ivHero);
            }

            // Collect gallery photos from all memories in this trip
            galleryPhotos.clear();
            photoMemoryIds.clear();
            List<Memory> memories = memoryDao.getMemoriesByTripId(db, tripId);
            for (Memory memory : memories) {
                List<String> paths = memory.getPhotoPathList();
                for (String path : paths) {
                    galleryPhotos.add(path);
                    photoMemoryIds.add(memory.getMemoryId());
                }
            }
        }

        galleryAdapter.notifyDataSetChanged();
        tvGalleryEmpty.setVisibility(galleryPhotos.isEmpty() ? View.VISIBLE : View.GONE);
        rvGallery.setVisibility(galleryPhotos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showAddMemoryDialog() {
        int userId = com.example.memoriva.utils.UserManager.getLocalUserId(
                this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser());

        List<Memory> allMemories;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            allMemories = memoryDao.getMemoriesByUserId(db, userId);
        }

        if (allMemories.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("No Memories")
                    .setMessage("You don't have any memories yet. Create a memory first!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Build display list
        String[] memoryTitles = new String[allMemories.size()];
        boolean[] checked = new boolean[allMemories.size()];

        // Pre-check memories already linked to this trip
        List<Integer> alreadyLinked = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> linked = memoryDao.getMemoriesByTripId(db, tripId);
            for (Memory m : linked) alreadyLinked.add(m.getMemoryId());
        }

        for (int i = 0; i < allMemories.size(); i++) {
            Memory m = allMemories.get(i);
            String date = m.getDate() != null ? " (" + m.getDate() + ")" : "";
            memoryTitles[i] = m.getTitle() + date;
            checked[i] = alreadyLinked.contains(m.getMemoryId());
        }

        final boolean[] selection = checked.clone();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Memories to Trip")
                .setMultiChoiceItems(memoryTitles, checked,
                        (dialog, which, isChecked) -> selection[which] = isChecked)
                .setPositiveButton("Save", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                        for (int i = 0; i < allMemories.size(); i++) {
                            int memId = allMemories.get(i).getMemoryId();
                            if (selection[i]) {
                                tripDao.addMemoryToTrip(db, memId, tripId);
                            } else {
                                tripDao.removeMemoryFromTrip(db, memId, tripId);
                            }
                        }
                    }
                    loadTripData(); // Refresh gallery
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareTrip() {
        if (trip == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Trip: " + trip.getName() + "\nDestination: " + trip.getDestination()
                        + "\n" + trip.getStartDate() + " – " + trip.getEndDate());
        startActivity(Intent.createChooser(shareIntent, "Share Trip"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Edit");
        menu.add(0, 2, 1, "Delete");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 1: // Edit
                Intent editIntent = new Intent(this, TripCreateEditActivity.class);
                editIntent.putExtra("trip_id", tripId);
                startActivity(editIntent);
                return true;
            case 2: // Delete
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete Trip")
                        .setMessage("Are you sure you want to delete this trip?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                                tripDao.deleteTrip(db, tripId);
                            }
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
