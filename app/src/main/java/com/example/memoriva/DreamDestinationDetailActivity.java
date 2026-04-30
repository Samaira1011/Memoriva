package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.DreamDestinationDao;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.models.DreamDestination;
import com.google.android.material.button.MaterialButton;

public class DreamDestinationDetailActivity extends BaseActivity {

    private int dreamId;
    private DreamDestination dream;
    private MemorivaDbHelper dbHelper;
    private DreamDestinationDao dreamDao;

    private ImageView ivCover;
    private TextView tvStatus, tvPlaceName, tvExpectedDate, tvNotes, tvBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dream_destination_detail);

        dbHelper = new MemorivaDbHelper(this);
        dreamDao = new DreamDestinationDao();

        dreamId = getIntent().getIntExtra("dream_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivCover = findViewById(R.id.ivCover);
        tvStatus = findViewById(R.id.tvStatus);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvExpectedDate = findViewById(R.id.tvExpectedDate);
        tvNotes = findViewById(R.id.tvNotes);
        tvBudget = findViewById(R.id.tvBudget);

        MaterialButton btnMarkVisited = findViewById(R.id.btnMarkVisited);
        btnMarkVisited.setOnClickListener(v -> confirmMarkAsVisited());

        MaterialButton btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, DreamDestinationCreateEditActivity.class);
            intent.putExtra("dream_id", dreamId);
            startActivity(intent);
        });

        MaterialButton btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> confirmDelete());

        loadDream();
    }

    private void loadDream() {
        if (dreamId == -1) {
            finish();
            return;
        }
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            dream = dreamDao.getDreamDestinationById(db, dreamId);
        }
        if (dream == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(dream.getPlaceName());

        tvPlaceName.setText(dream.getPlaceName());
        tvStatus.setText(dream.getStatus());

        // Status badge color
        if (DreamDestination.STATUS_PLANNED.equals(dream.getStatus())) {
            tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.colorPlanned, null)));
        } else {
            tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.colorWishlist, null)));
        }

        tvExpectedDate.setText(dream.getExpectedDate() != null
                ? "Expected: " + dream.getExpectedDate() : "No expected date set");
        tvNotes.setText(dream.getNotes() != null ? dream.getNotes() : "");
        tvBudget.setText(dream.getBudgetEstimate() > 0
                ? "Budget: ₹" + String.format("%.2f", dream.getBudgetEstimate()) : "");

        // Load cover image using Glide — handles both content:// URIs and file paths
        if (dream.getCoverImagePath() != null && !dream.getCoverImagePath().isEmpty()) {
            String path = dream.getCoverImagePath();
            Object source = path.startsWith("content://")
                    ? android.net.Uri.parse(path) : new java.io.File(path);
            com.bumptech.glide.Glide.with(this)
                    .load(source)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(ivCover);
        }
    }

    private void confirmMarkAsVisited() {
        if (dream == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mark as Visited")
                .setMessage("Mark \"" + dream.getPlaceName() + "\" as visited? You'll be taken to create a new trip.")
                .setPositiveButton("Yes, I visited!", (dialog, which) -> {
                    Intent intent = new Intent(this, TripCreateEditActivity.class);
                    intent.putExtra("destination", dream.getPlaceName());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete() {
        if (dream == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Destination")
                .setMessage("Are you sure you want to delete \"" + dream.getPlaceName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                        dreamDao.deleteDreamDestination(db, dreamId);
                    }
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDream();
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
