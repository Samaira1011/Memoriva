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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.DreamDestinationDao;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.models.DreamDestination;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class DreamDestinationCreateEditActivity extends BaseActivity {

    private static final int REQUEST_COVER_PHOTO = 20;

    private TextInputEditText etPlaceName, etNotes, etBudget;
    private RadioGroup rgStatus;
    private RadioButton rbPlanned, rbWishlist;
    private MaterialButton btnExpectedDate, btnSave;
    private ImageView ivCover;

    private String expectedDate = "";
    private String coverImagePath = "";
    private int editDreamId = -1;

    private MemorivaDbHelper dbHelper;
    private DreamDestinationDao dreamDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dream_destination_create_edit);

        dbHelper = new MemorivaDbHelper(this);
        dreamDao = new DreamDestinationDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etPlaceName = findViewById(R.id.etPlaceName);
        etNotes = findViewById(R.id.etNotes);
        etBudget = findViewById(R.id.etBudget);
        rgStatus = findViewById(R.id.rgStatus);
        rbPlanned = findViewById(R.id.rbPlanned);
        rbWishlist = findViewById(R.id.rbWishlist);
        btnExpectedDate = findViewById(R.id.btnExpectedDate);
        btnSave = findViewById(R.id.btnSave);
        ivCover = findViewById(R.id.ivCover);

        editDreamId = getIntent().getIntExtra("dream_id", -1);
        if (editDreamId != -1) {
            toolbar.setTitle("Edit Dream Destination");
            loadDreamForEdit(editDreamId);
        }

        btnExpectedDate.setOnClickListener(v -> showDatePicker());

        MaterialButton btnAddCoverPhoto = findViewById(R.id.btnAddCoverPhoto);
        btnAddCoverPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_COVER_PHOTO);
        });

        btnSave.setOnClickListener(v -> saveDream());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            expectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            btnExpectedDate.setText("📅 " + expectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_COVER_PHOTO && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                String[] projection = {MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    coverImagePath = cursor.getString(col);
                    cursor.close();
                }
                Bitmap bitmap = BitmapFactory.decodeFile(coverImagePath);
                if (bitmap != null) ivCover.setImageBitmap(bitmap);
            } catch (Exception e) {
                coverImagePath = uri.getPath() != null ? uri.getPath() : "";
            }
        }
    }

    private void saveDream() {
        String placeName = etPlaceName.getText() != null ? etPlaceName.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";

        if (TextUtils.isEmpty(placeName)) {
            Snackbar.make(btnSave, "Place name is required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String status = rbPlanned.isChecked() ? DreamDestination.STATUS_PLANNED : DreamDestination.STATUS_WISHLIST;
        double budget = 0;
        if (!TextUtils.isEmpty(budgetStr)) {
            try { budget = Double.parseDouble(budgetStr); } catch (NumberFormatException ignored) {}
        }

        // Check for duplicate (only for new entries)
        if (editDreamId == -1) {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                if (dreamDao.isDuplicateDestination(db, 1, placeName)) {
                    Snackbar.make(btnSave, "You already have a dream destination for \"" + placeName + "\"",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
        }

        DreamDestination dream = new DreamDestination();
        dream.setUserId(1); // placeholder
        dream.setPlaceName(placeName);
        dream.setStatus(status);
        dream.setExpectedDate(expectedDate.isEmpty() ? null : expectedDate);
        dream.setNotes(notes);
        dream.setBudgetEstimate(budget);
        dream.setCoverImagePath(coverImagePath.isEmpty() ? null : coverImagePath);
        dream.setCreatedAt(System.currentTimeMillis());

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            if (editDreamId != -1) {
                dream.setDreamId(editDreamId);
                dreamDao.updateDreamDestination(db, dream);
            } else {
                dreamDao.insertDreamDestination(db, dream);
            }
        }
        finish();
    }

    private void loadDreamForEdit(int dreamId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            DreamDestination dream = dreamDao.getDreamDestinationById(db, dreamId);
            if (dream != null) {
                etPlaceName.setText(dream.getPlaceName());
                etNotes.setText(dream.getNotes());
                if (dream.getBudgetEstimate() > 0) {
                    etBudget.setText(String.valueOf(dream.getBudgetEstimate()));
                }
                if (DreamDestination.STATUS_PLANNED.equals(dream.getStatus())) {
                    rbPlanned.setChecked(true);
                } else {
                    rbWishlist.setChecked(true);
                }
                expectedDate = dream.getExpectedDate() != null ? dream.getExpectedDate() : "";
                if (!expectedDate.isEmpty()) btnExpectedDate.setText("📅 " + expectedDate);
                coverImagePath = dream.getCoverImagePath() != null ? dream.getCoverImagePath() : "";
                if (!coverImagePath.isEmpty()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(coverImagePath);
                    if (bitmap != null) ivCover.setImageBitmap(bitmap);
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
