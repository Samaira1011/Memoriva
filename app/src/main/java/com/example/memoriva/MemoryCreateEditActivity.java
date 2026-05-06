package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.location.Address;
import android.location.Geocoder;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

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
    private AutoCompleteTextView spinnerCountry;
    private MaterialButton btnSelectDate, btnSaveMemory;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> photoPaths = new ArrayList<>();
    private String selectedMood = "😊";

    private static final String[] COUNTRIES = {
        "Afghanistan","Albania","Algeria","Argentina","Armenia","Australia","Austria",
        "Azerbaijan","Bahrain","Bangladesh","Belarus","Belgium","Bolivia","Brazil",
        "Bulgaria","Cambodia","Canada","Chile","China","Colombia","Croatia","Cuba",
        "Cyprus","Czech Republic","Denmark","Ecuador","Egypt","Estonia","Ethiopia",
        "Finland","France","Georgia","Germany","Ghana","Greece","Guatemala","Hungary",
        "Iceland","India","Indonesia","Iran","Iraq","Ireland","Israel","Italy",
        "Jamaica","Japan","Jordan","Kazakhstan","Kenya","Kuwait","Latvia","Lebanon",
        "Lithuania","Luxembourg","Malaysia","Maldives","Malta","Mexico","Morocco",
        "Myanmar","Nepal","Netherlands","New Zealand","Nigeria","Norway","Oman",
        "Pakistan","Panama","Peru","Philippines","Poland","Portugal","Qatar",
        "Romania","Russia","Saudi Arabia","Serbia","Singapore","South Africa",
        "South Korea","Spain","Sri Lanka","Sweden","Switzerland","Taiwan","Tanzania",
        "Thailand","Tunisia","Turkey","Ukraine","United Arab Emirates",
        "United Kingdom","United States","Uruguay","Uzbekistan","Venezuela",
        "Vietnam","Yemen","Zimbabwe"
    };

    private String selectedDate = "";
    private int editMemoryId = -1;
    private Uri cameraImageUri;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_memory_create_edit);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

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
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSaveMemory = findViewById(R.id.btnSaveMemory);
        rvPhotos = findViewById(R.id.rvPhotos);

        // Country autocomplete
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setThreshold(1);

        // Mood selector
        setupMoodSelector();

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

    private void setupMoodSelector() {
        int[] moodIds = {R.id.moodHappy, R.id.moodExcited, R.id.moodPeaceful,
                R.id.moodNostalgic, R.id.moodAdventurous};
        String[] moods = {"😊", "🤩", "😌", "🥺", "🏔️"};

        for (int i = 0; i < moodIds.length; i++) {
            TextView moodView = findViewById(moodIds[i]);
            final String mood = moods[i];
            if (moodView != null) {
                moodView.setOnClickListener(v -> {
                    selectedMood = mood;
                    // Highlight selected
                    for (int id : moodIds) {
                        TextView mv = findViewById(id);
                        if (mv != null) mv.setAlpha(0.4f);
                    }
                    moodView.setAlpha(1.0f);
                });
            }
        }
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
                    // Persist permission and store URI string directly
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    photoPaths.add(uri.toString());
                }
                photoAdapter.notifyDataSetChanged();
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                photoPaths.add(uri.toString());
                photoAdapter.notifyItemInserted(photoPaths.size() - 1);
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
        String city = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String country = spinnerCountry.getText() != null ? spinnerCountry.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        String location = "";
        if (!city.isEmpty() && !country.isEmpty()) location = city + ", " + country;
        else if (!city.isEmpty()) location = city;
        else if (!country.isEmpty()) location = country;

        String fullNotes = notes;
        if (!selectedMood.isEmpty()) fullNotes = selectedMood + " " + notes;

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

        JSONArray jsonArray = new JSONArray();
        for (String path : photoPaths) jsonArray.put(path);
        String photoPathsJson = jsonArray.toString();

        int userId = com.example.memoriva.utils.UserManager.getLocalUserId(
                this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser());

        Memory memory = new Memory();
        memory.setUserId(userId);
        memory.setTitle(title);
        memory.setDate(selectedDate);
        memory.setNotes(fullNotes);
        memory.setPhotoPaths(photoPathsJson);
        memory.setCreatedAt(System.currentTimeMillis());
        memory.setUpdatedAt(System.currentTimeMillis());

        btnSaveMemory.setEnabled(false);
        btnSaveMemory.setText("Saving...");

        final String finalLocation = location;
        final String finalCity = city;
        final String finalCountry = country;

        Executors.newSingleThreadExecutor().execute(() -> {
            int placeId = -1;
            if (!finalLocation.isEmpty()) {
                double lat = 0.0, lng = 0.0;
                try {
                    if (Geocoder.isPresent()) {
                        Geocoder geocoder = new Geocoder(MemoryCreateEditActivity.this, Locale.getDefault());
                        java.util.List<Address> addresses = geocoder.getFromLocationName(finalLocation, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            lat = addresses.get(0).getLatitude();
                            lng = addresses.get(0).getLongitude();
                        }
                    }
                } catch (Exception e) {
                    // Geocoding failed — save place without coordinates
                    e.printStackTrace();
                }

                com.example.memoriva.models.Place place = new com.example.memoriva.models.Place();
                place.setName(finalLocation);
                place.setCity(finalCity);
                place.setCountry(finalCountry);
                place.setLatitude(lat);
                place.setLongitude(lng);
                place.setCreatedAt(System.currentTimeMillis());

                try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                    com.example.memoriva.database.PlaceDao placeDao = new com.example.memoriva.database.PlaceDao();
                    placeId = (int) placeDao.insertPlace(db, place);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final int finalPlaceId = placeId;
            new Handler(Looper.getMainLooper()).post(() -> {
                memory.setPlaceId(finalPlaceId != -1 ? finalPlaceId : 0);
                try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                    if (editMemoryId != -1) {
                        memory.setMemoryId(editMemoryId);
                        int rows = memoryDao.updateMemory(db, memory);
                        if (rows > 0) {
                            Snackbar.make(btnSaveMemory, "Memory updated!", Snackbar.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Snackbar.make(btnSaveMemory, "Failed to update memory", Snackbar.LENGTH_SHORT).show();
                            btnSaveMemory.setEnabled(true);
                            btnSaveMemory.setText("Save Memory");
                        }
                    } else {
                        long id = memoryDao.insertMemory(db, memory);
                        if (id > 0) {
                            finish();
                        } else {
                            Snackbar.make(btnSaveMemory, "Failed to save memory", Snackbar.LENGTH_SHORT).show();
                            btnSaveMemory.setEnabled(true);
                            btnSaveMemory.setText("Save Memory");
                        }
                    }
                }
            });
        });
    }

    private void loadMemoryForEdit(int memoryId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Memory memory = memoryDao.getMemoryById(db, memoryId);
            if (memory != null) {
                etTitle.setText(memory.getTitle());
                etNotes.setText(memory.getNotes());
                selectedDate = memory.getDate();
                btnSelectDate.setText("📅 " + selectedDate);

                // Restore location fields from linked place
                if (memory.getPlaceId() > 0) {
                    com.example.memoriva.database.PlaceDao placeDao =
                            new com.example.memoriva.database.PlaceDao();
                    com.example.memoriva.models.Place place =
                            placeDao.getPlaceById(db, memory.getPlaceId());
                    if (place != null) {
                        if (place.getCity() != null) etLocation.setText(place.getCity());
                        if (place.getCountry() != null) spinnerCountry.setText(place.getCountry(), false);
                    }
                }

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
