package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.FriendDao;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.PlaceDao;
import com.example.memoriva.database.TripDao;
import com.example.memoriva.models.Place;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class TravelStatsActivity extends BaseActivity {

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private PlaceDao placeDao;
    private TripDao tripDao;
    private FriendDao friendDao;

    private TextView tvCitiesValue;
    private TextView tvCountriesValue;
    private TextView tvMemoriesValue;
    private TextView tvTripsValue;
    private TextView tvMostVisited;
    private TextView tvMostVisitedCount;
    private TextView tvYearStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_travel_stats);
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
        placeDao = new PlaceDao();
        tripDao = new TripDao();
        friendDao = new FriendDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.travel_stats));
        }

        tvCitiesValue = findViewById(R.id.tvCitiesValue);
        tvCountriesValue = findViewById(R.id.tvCountriesValue);
        tvMemoriesValue = findViewById(R.id.tvMemoriesValue);
        tvTripsValue = findViewById(R.id.tvTripsValue);
        tvMostVisited = findViewById(R.id.tvMostVisited);
        tvMostVisitedCount = findViewById(R.id.tvMostVisitedCount);
        tvYearStats = findViewById(R.id.tvYearStats);

        MaterialButton btnShareYear = findViewById(R.id.btnShareYear);
        btnShareYear.setOnClickListener(v -> shareStats());

        loadStats();
    }

    private int getUserId() {
        return com.example.memoriva.utils.UserManager.getLocalUserId(this,
                com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser());
    }

    private void loadStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int userId = getUserId();
            int cities = placeDao.getUniqueCitiesCount(db, userId);
            int countries = placeDao.getUniqueCountriesCount(db, userId);
            int memories = memoryDao.getMemoriesByUserId(db, userId).size();
            int trips = tripDao.getTripsByUserId(db, userId).size();
            Place mostVisited = placeDao.getMostVisitedPlace(db, userId);

            tvCitiesValue.setText(String.valueOf(cities));
            tvCountriesValue.setText(String.valueOf(countries));
            tvMemoriesValue.setText(String.valueOf(memories));
            tvTripsValue.setText(String.valueOf(trips));

            if (mostVisited != null) {
                // Build display name: prefer City, Country over raw name
                StringBuilder placeName = new StringBuilder();
                if (mostVisited.getCity() != null && !mostVisited.getCity().isEmpty()) {
                    placeName.append(mostVisited.getCity());
                }
                if (mostVisited.getCountry() != null && !mostVisited.getCountry().isEmpty()) {
                    if (placeName.length() > 0) placeName.append(", ");
                    placeName.append(mostVisited.getCountry());
                }
                if (placeName.length() == 0 && mostVisited.getName() != null) {
                    placeName.append(mostVisited.getName());
                }
                tvMostVisited.setText(placeName.length() > 0 ? placeName.toString() : "—");

                // Get visit count for this place
                int visitCount = placeDao.getVisitCountForPlace(db, mostVisited.getPlaceId(), userId);
                tvMostVisitedCount.setText(visitCount + (visitCount == 1 ? " memory" : " memories"));
            } else {
                tvMostVisited.setText("—");
                tvMostVisitedCount.setText("No places yet");
            }

            int year = Calendar.getInstance().get(Calendar.YEAR);
            tvYearStats.setText("You explored " + cities + " cities across " + countries +
                    " countries in " + year + " with " + memories + " memories captured.");
        }
    }
    private void shareStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int userId = getUserId();
            int cities = placeDao.getUniqueCitiesCount(db, userId);
            int countries = placeDao.getUniqueCountriesCount(db, userId);
            int memories = memoryDao.getMemoriesByUserId(db, userId).size();
            int trips = tripDao.getTripsByUserId(db, userId).size();
            int year = Calendar.getInstance().get(Calendar.YEAR);

            String summary = "My " + year + " Travel Year on Memoriva:\n" +
                    "🏙️ Cities visited: " + cities + "\n" +
                    "🌍 Countries: " + countries + "\n" +
                    "📸 Memories: " + memories + "\n" +
                    "✈️ Trips: " + trips + "\n" +
                    "#Memoriva #Travel";

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, summary);
            startActivity(Intent.createChooser(intent, getString(R.string.share_travel_year)));
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
