package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
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

    private TextInputEditText etPlaceName, etNotes, etBudget, etCity;
    private AutoCompleteTextView spinnerCountry;
    private RadioGroup rgStatus;
    private RadioButton rbPlanned, rbWishlist;
    private MaterialButton btnExpectedDate, btnSave;
    private ImageView ivCover;

    private String expectedDate = "";
    private String coverImagePath = "";
    private int editDreamId = -1;

    private MemorivaDbHelper dbHelper;
    private DreamDestinationDao dreamDao;

    // Countries list
    private static final String[] COUNTRIES = {
        "Afghanistan", "Albania", "Algeria", "Argentina", "Armenia", "Australia", "Austria",
        "Azerbaijan", "Bahrain", "Bangladesh", "Belarus", "Belgium", "Bolivia", "Bosnia",
        "Brazil", "Bulgaria", "Cambodia", "Canada", "Chile", "China", "Colombia", "Croatia",
        "Cuba", "Cyprus", "Czech Republic", "Denmark", "Ecuador", "Egypt", "Estonia",
        "Ethiopia", "Finland", "France", "Georgia", "Germany", "Ghana", "Greece", "Guatemala",
        "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel",
        "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kuwait", "Kyrgyzstan",
        "Latvia", "Lebanon", "Libya", "Lithuania", "Luxembourg", "Malaysia", "Maldives",
        "Malta", "Mexico", "Moldova", "Mongolia", "Morocco", "Myanmar", "Nepal", "Netherlands",
        "New Zealand", "Nigeria", "North Korea", "Norway", "Oman", "Pakistan", "Palestine",
        "Panama", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania",
        "Russia", "Saudi Arabia", "Serbia", "Singapore", "Slovakia", "Slovenia", "South Africa",
        "South Korea", "Spain", "Sri Lanka", "Sudan", "Sweden", "Switzerland", "Syria",
        "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Tunisia", "Turkey", "Turkmenistan",
        "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States",
        "Uruguay", "Uzbekistan", "Venezuela", "Vietnam", "Yemen", "Zimbabwe"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dream_destination_create_edit);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

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
        etCity = findViewById(R.id.etCity);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        rgStatus = findViewById(R.id.rgStatus);
        rbPlanned = findViewById(R.id.rbPlanned);
        rbWishlist = findViewById(R.id.rbWishlist);
        btnExpectedDate = findViewById(R.id.btnExpectedDate);
        btnSave = findViewById(R.id.btnSave);
        ivCover = findViewById(R.id.ivCover);

        // Set up country autocomplete
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setThreshold(1);

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
        if (resultCode == RESULT_OK && requestCode == REQUEST_COVER_PHOTO
                && data != null && data.getData() != null) {
            Uri uri = data.getData();
            // Persist permission
            try {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            coverImagePath = uri.toString();
            // Load with Glide
            Glide.with(this).load(uri).centerCrop().into(ivCover);
        }
    }

    private void saveDream() {
        String placeName = etPlaceName.getText() != null ? etPlaceName.getText().toString().trim() : "";
        String country = spinnerCountry.getText() != null ? spinnerCountry.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";

        // Auto-fill place name from country/city if empty
        if (TextUtils.isEmpty(placeName)) {
            if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(country)) {
                placeName = city + ", " + country;
            } else if (!TextUtils.isEmpty(country)) {
                placeName = country;
            } else if (!TextUtils.isEmpty(city)) {
                placeName = city;
            }
        }

        if (TextUtils.isEmpty(placeName)) {
            Snackbar.make(btnSave, "Place name or country is required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String status = rbPlanned.isChecked() ? DreamDestination.STATUS_PLANNED : DreamDestination.STATUS_WISHLIST;
        double budget = 0;
        if (!TextUtils.isEmpty(budgetStr)) {
            try { budget = Double.parseDouble(budgetStr); } catch (NumberFormatException ignored) {}
        }

        if (editDreamId == -1) {
            int localUserId = com.example.memoriva.utils.UserManager.getLocalUserId(
                    this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser());
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                if (dreamDao.isDuplicateDestination(db, localUserId, placeName)) {
                    Snackbar.make(btnSave, "You already have a dream destination for \"" + placeName + "\"",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
        }

        DreamDestination dream = new DreamDestination();
        dream.setUserId(com.example.memoriva.utils.UserManager.getLocalUserId(
                this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser()));
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
                    // Load with Glide — handles both content:// URIs and file paths
                    Object source = coverImagePath.startsWith("content://")
                            ? Uri.parse(coverImagePath) : new java.io.File(coverImagePath);
                    Glide.with(this).load(source).centerCrop().into(ivCover);
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
