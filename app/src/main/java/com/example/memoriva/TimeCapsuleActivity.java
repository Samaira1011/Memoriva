package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.TimeCapsuleDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.models.TimeCapsule;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeCapsuleActivity extends BaseActivity {

    public static final String EXTRA_MEMORY_ID = "memory_id";

    private int memoryId = -1;
    private String selectedOpenDate = null;
    private TimeCapsule existingCapsule = null;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private TimeCapsuleDao timeCapsuleDao;

    private ImageView ivMemoryThumbnail;
    private TextView tvMemoryTitle;
    private TextView tvMemoryLocation;
    private Switch switchLockMemory;
    private LinearLayout layoutCapsuleOptions;
    private Button btnOpenDate;
    private TextInputEditText etMessage;
    private MaterialButton btnSealCapsule;

    // Sealed state views
    private LinearLayout layoutSealedState;
    private TextView tvSealedOpenDate;
    private TextView tvCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_capsule);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        memoryId = getIntent().getIntExtra(EXTRA_MEMORY_ID, -1);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();
        timeCapsuleDao = new TimeCapsuleDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Time Capsule");
        }

        ivMemoryThumbnail = findViewById(R.id.ivMemoryThumbnail);
        tvMemoryTitle = findViewById(R.id.tvMemoryTitle);
        tvMemoryLocation = findViewById(R.id.tvMemoryLocation);
        switchLockMemory = findViewById(R.id.switchLockMemory);
        layoutCapsuleOptions = findViewById(R.id.layoutCapsuleOptions);
        btnOpenDate = findViewById(R.id.btnOpenDate);
        etMessage = findViewById(R.id.etMessage);
        btnSealCapsule = findViewById(R.id.btnSealCapsule);
        layoutSealedState = findViewById(R.id.layoutSealedState);
        tvSealedOpenDate = findViewById(R.id.tvSealedOpenDate);
        tvCountdown = findViewById(R.id.tvCountdown);

        switchLockMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutCapsuleOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnOpenDate.setOnClickListener(v -> showDatePicker());

        btnSealCapsule.setOnClickListener(v -> sealCapsule());

        loadData();
    }

    private void loadData() {
        if (memoryId < 0) return;

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Memory memory = memoryDao.getMemoryById(db, memoryId);
            if (memory != null) {
                tvMemoryTitle.setText(memory.getTitle());
                tvMemoryLocation.setText(memory.getDate() != null ? memory.getDate() : "");

                List<String> photos = memory.getPhotoPathList();
                if (!photos.isEmpty()) {
                    String path = photos.get(0);
                    Object source = path.startsWith("content://")
                            ? android.net.Uri.parse(path) : new java.io.File(path);
                    com.bumptech.glide.Glide.with(this)
                            .load(source)
                            .centerCrop()
                            .placeholder(R.drawable.ic_photo)
                            .into(ivMemoryThumbnail);
                }
            }

            existingCapsule = timeCapsuleDao.getTimeCapsuleByMemoryId(db, memoryId);
        }

        if (existingCapsule != null) {
            showSealedState();
        } else {
            showUnsealedState();
        }
    }

    private void showSealedState() {
        switchLockMemory.setChecked(true);
        switchLockMemory.setEnabled(false);
        layoutCapsuleOptions.setVisibility(View.GONE);
        btnSealCapsule.setVisibility(View.GONE);
        layoutSealedState.setVisibility(View.VISIBLE);

        tvSealedOpenDate.setText("Opens on: " + existingCapsule.getOpenDate());

        // Calculate countdown
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date openDate = sdf.parse(existingCapsule.getOpenDate());
            if (openDate != null) {
                long diff = openDate.getTime() - System.currentTimeMillis();
                if (diff > 0) {
                    long days = TimeUnit.MILLISECONDS.toDays(diff);
                    tvCountdown.setText(days + " days until opening");
                } else {
                    tvCountdown.setText("Ready to open!");
                }
            }
        } catch (Exception e) {
            tvCountdown.setText("");
        }
    }

    private void showUnsealedState() {
        layoutSealedState.setVisibility(View.GONE);
        btnSealCapsule.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1); // default to tomorrow

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (!selected.after(today)) {
                        Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedOpenDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    btnOpenDate.setText(selectedOpenDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        // Set minimum date to tomorrow
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.show();
    }

    private void sealCapsule() {
        if (!switchLockMemory.isChecked()) {
            Snackbar.make(btnSealCapsule, "Please enable the lock switch first", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedOpenDate == null) {
            Snackbar.make(btnSealCapsule, "Please select an open date", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Validate future date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date openDate = sdf.parse(selectedOpenDate);
            Date today = new Date();
            if (openDate != null && !openDate.after(today)) {
                Snackbar.make(btnSealCapsule, "Open date must be in the future", Snackbar.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Snackbar.make(btnSealCapsule, "Invalid date format", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

        TimeCapsule capsule = new TimeCapsule();
        capsule.setMemoryId(memoryId);
        capsule.setUserId(com.example.memoriva.utils.UserManager.getLocalUserId(
                this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser()));
        capsule.setOpenDate(selectedOpenDate);
        capsule.setMessage(message);
        capsule.setOpened(false);
        capsule.setCreatedAt(System.currentTimeMillis());

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            long result = timeCapsuleDao.insertTimeCapsule(db, capsule);
            if (result < 0) {
                Snackbar.make(btnSealCapsule, "Failed to seal capsule", Snackbar.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, getString(R.string.time_capsule_sealed, selectedOpenDate), Toast.LENGTH_LONG).show();
        finish();
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
