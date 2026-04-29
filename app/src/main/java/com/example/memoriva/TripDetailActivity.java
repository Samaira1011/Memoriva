package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.PhotoAdapter;
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
    private TextView tvTripName, tvDateRange, tvNotes;
    private RecyclerView rvGallery;
    private PhotoAdapter galleryAdapter;
    private List<String> galleryPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        dbHelper = new MemorivaDbHelper(this);
        tripDao = new TripDao();
        memoryDao = new MemoryDao();

        tripId = getIntent().getIntExtra("trip_id", -1);

        ivHero = findViewById(R.id.ivHero);
        tvTripName = findViewById(R.id.tvTripName);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvNotes = findViewById(R.id.tvNotes);
        rvGallery = findViewById(R.id.rvGallery);

        galleryAdapter = new PhotoAdapter(this, galleryPhotos);
        rvGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvGallery.setAdapter(galleryAdapter);

        MaterialButton btnShareMemory = findViewById(R.id.btnShareMemory);
        btnShareMemory.setOnClickListener(v -> shareTrip());

        MaterialButton btnAddToStory = findViewById(R.id.btnAddToStory);
        btnAddToStory.setOnClickListener(v -> {
            if (trip == null) return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "My trip to " + trip.getDestination() + ": " + trip.getName());
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
                Bitmap bitmap = BitmapFactory.decodeFile(trip.getCoverPhotoPath());
                if (bitmap != null) ivHero.setImageBitmap(bitmap);
            }

            // Collect gallery photos from all memories in this trip
            galleryPhotos.clear();
            List<Memory> memories = memoryDao.getMemoriesByTripId(db, tripId);
            for (Memory memory : memories) {
                galleryPhotos.addAll(memory.getPhotoPathList());
            }
        }
        galleryAdapter.notifyDataSetChanged();
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
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
