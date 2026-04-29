package com.example.memoriva;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.PhotoAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.PhotoImporter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MemoryCreateEditActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private static final String PREFS_NAME = "memoriva_prefs";
    private static final String KEY_USER_ID = "user_id";

    private TextInputEditText etTitle, etLocation, etNotes;
    private MaterialButton btnSelectDate, btnSaveMemory;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> photoPaths = new ArrayList<>();

    private String selectedDate = "";
    private int editMemoryId = -1;
    private Uri cameraImageUri;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_create_edit);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSaveMemory = findViewById(R.id.btnSaveMemory);
        rvPhotos = findViewById(R.id.rvPhotos);

        // Setup photo RecyclerView
        photoAdapter = new PhotoAdapter(this, photoPaths);
        photoAdapter.setOnPhotoRemoveListener(position -> {
            photoPaths.remove(position);
            photoAdapter.notifyItemRemoved(position);
        });
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        // Check for edit mode
        editMemoryId = getIntent().getIntExtra("memory_id", -1);
        if (editMemoryId != -1) {
            toolbar.setTitle("Edit Memory");
            loadMemoryForEdit(editMemoryId);
        } else {
            // Default date to today
            Calendar cal = Calendar.getInstance();
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            btnSelectDate.setText("📅 " + selectedDate);

            // Pre-fill date if passed via intent
            String dateExtra = getIntent().getStringExtra("date");
            if (dateExtra != null && !dateExtra.isEmpty()) {
                selectedDate = dateExtra;
                btnSelectDate.setText("📅 " + selectedDate);
            }
        }

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        MaterialButton btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddPhoto.setOnClickListener(v -> showPhotoSourceDialog());

        btnSaveMemory.setOnClickListener(v -> saveMemory());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (!selectedDate.isEmpty()) {
            try {
                String[] parts = selectedDate.split("-");
                cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            btnSelectDate.setText("📅 " + selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showPhotoSourceDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                })
                .show();
    }

    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        String filePath = PhotoImporter.createImageFile(this);
        if (filePath == null) {
            Snackbar.make(btnSaveMemory, "Could not create image file", Snackbar.LENGTH_SHORT).show();
            return;
        }
        cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                this, "com.example.memoriva.fileprovider",
                new java.io.File(filePath));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Photos"), REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (cameraImageUri != null) {
                String path = cameraImageUri.getPath();
                if (path != null) {
                    photoPaths.add(path);
                    photoAdapter.notifyItemInserted(photoPaths.size() - 1);
                }
            }
        } else if (requestCode == REQUEST_GALLERY && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    String path = getRealPathFromUri(uri);
                    if (path != null) {
                        photoPaths.add(path);
                    }
                }
                photoAdapter.notifyDataSetChanged();
            } else if (data.getData() != null) {
                String path = getRealPathFromUri(data.getData());
                if (path != null) {
                    photoPaths.add(path);
                    photoAdapter.notifyItemInserted(photoPaths.size() - 1);
                }
            }
        }
    }

    private String getRealPathFromUri(Uri uri) {
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(columnIndex);
                cursor.close();
                return path;
            }
        } catch (Exception e) {
            // Fall back to uri path
            return uri.getPath();
        }
        return uri.getPath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Camera Permission Required")
                        .setMessage("Camera permission is needed to take photos for your memories.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    private void saveMemory() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            Snackbar.make(btnSaveMemory, "Title is required", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (photoPaths.isEmpty()) {
            Snackbar.make(btnSaveMemory, "At least one photo is required", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            Snackbar.make(btnSaveMemory, "Please select a date", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Build photo paths JSON
        JSONArray jsonArray = new JSONArray();
        for (String path : photoPaths) {
            jsonArray.put(path);
        }
        String photoPathsJson = jsonArray.toString();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // userId is stored as firebase uid string; for SQLite we use 1 as placeholder
        int userId = 1;

        Memory memory = new Memory();
        memory.setUserId(userId);
        memory.setTitle(title);
        memory.setDate(selectedDate);
        memory.setNotes(notes);
        memory.setPhotoPaths(photoPathsJson);
        memory.setCreatedAt(System.currentTimeMillis());
        memory.setUpdatedAt(System.currentTimeMillis());

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            if (editMemoryId != -1) {
                memory.setMemoryId(editMemoryId);
                int rows = memoryDao.updateMemory(db, memory);
                if (rows > 0) {
                    Snackbar.make(btnSaveMemory, "Memory updated!", Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    Snackbar.make(btnSaveMemory, "Failed to update memory", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                long id = memoryDao.insertMemory(db, memory);
                if (id > 0) {
                    finish();
                } else {
                    Snackbar.make(btnSaveMemory, "Failed to save memory", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadMemoryForEdit(int memoryId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Memory memory = memoryDao.getMemoryById(db, memoryId);
            if (memory != null) {
                etTitle.setText(memory.getTitle());
                etNotes.setText(memory.getNotes());
                selectedDate = memory.getDate();
                btnSelectDate.setText("📅 " + selectedDate);

                List<String> paths = memory.getPhotoPathList();
                photoPaths.addAll(paths);
                photoAdapter.notifyDataSetChanged();
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
