package com.example.memoriva;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.TripDao;
import com.example.memoriva.models.Trip;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class TripCreateEditActivity extends BaseActivity {

    private static final int REQUEST_COVER_PHOTO = 10;

    private TextInputEditText etTripName, etDestination, etDescription;
    private MaterialButton btnStartDate, btnEndDate, btnSaveTrip;
    private ImageView ivCoverPhoto;

    private String startDate = "";
    private String endDate = "";
    private String coverPhotoPath = "";
    private int editTripId = -1;

    private MemorivaDbHelper dbHelper;
    private TripDao tripDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create_edit);

        dbHelper = new MemorivaDbHelper(this);
        tripDao = new TripDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTripName = findViewById(R.id.etTripName);
        etDestination = findViewById(R.id.etDestination);
        etDescription = findViewById(R.id.etDescription);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSaveTrip = findViewById(R.id.btnSaveTrip);
        ivCoverPhoto = findViewById(R.id.ivCoverPhoto);

        editTripId = getIntent().getIntExtra("trip_id", -1);
        if (editTripId != -1) {
            toolbar.setTitle("Edit Trip");
            loadTripForEdit(editTripId);
        }

        // Pre-fill destination if coming from map
        String destinationExtra = getIntent().getStringExtra("destination");
        if (destinationExtra != null && !destinationExtra.isEmpty()) {
            etDestination.setText(destinationExtra);
            if (etTripName.getText() == null || etTripName.getText().toString().isEmpty()) {
                etTripName.setText("Trip to " + destinationExtra);
            }
        }

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        MaterialButton btnChangeCover = findViewById(R.id.btnChangeCover);
        btnChangeCover.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_COVER_PHOTO);
        });

        btnSaveTrip.setOnClickListener(v -> saveTrip());
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStart) {
                startDate = date;
                btnStartDate.setText("Start: " + date);
            } else {
                endDate = date;
                btnEndDate.setText("End: " + date);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_COVER_PHOTO && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            coverPhotoPath = uri.toString();
            com.bumptech.glide.Glide.with(this).load(uri).centerCrop().into(ivCoverPhoto);
        }
    }

    private void saveTrip() {
        String name = etTripName.getText() != null ? etTripName.getText().toString().trim() : "";
        String destination = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(btnSaveTrip, "Trip name is required", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(startDate)) {
            Snackbar.make(btnSaveTrip, "Please select a start date", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(endDate)) {
            Snackbar.make(btnSaveTrip, "Please select an end date", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (endDate.compareTo(startDate) < 0) {
            Snackbar.make(btnSaveTrip, "End date must be on or after start date", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Trip trip = new Trip();
        trip.setUserId(1); // placeholder userId
        trip.setName(name);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setDestination(destination);
        trip.setDescription(description);
        trip.setCoverPhotoPath(coverPhotoPath);
        trip.setCreatedAt(System.currentTimeMillis());

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            if (editTripId != -1) {
                trip.setTripId(editTripId);
                tripDao.updateTrip(db, trip);
            } else {
                tripDao.insertTrip(db, trip);
            }
        }
        finish();
    }

    private void loadTripForEdit(int tripId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Trip trip = tripDao.getTripById(db, tripId);
            if (trip != null) {
                etTripName.setText(trip.getName());
                etDestination.setText(trip.getDestination());
                etDescription.setText(trip.getDescription());
                startDate = trip.getStartDate() != null ? trip.getStartDate() : "";
                endDate = trip.getEndDate() != null ? trip.getEndDate() : "";
                if (!startDate.isEmpty()) btnStartDate.setText("Start: " + startDate);
                if (!endDate.isEmpty()) btnEndDate.setText("End: " + endDate);
                coverPhotoPath = trip.getCoverPhotoPath() != null ? trip.getCoverPhotoPath() : "";
                if (!coverPhotoPath.isEmpty()) {
                    Object source = coverPhotoPath.startsWith("content://")
                            ? android.net.Uri.parse(coverPhotoPath) : new java.io.File(coverPhotoPath);
                    com.bumptech.glide.Glide.with(this).load(source).centerCrop().into(ivCoverPhoto);
                }
            }
        }
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
