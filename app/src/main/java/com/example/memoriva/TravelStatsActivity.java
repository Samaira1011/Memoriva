package com.example.memoriva;

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

    private static final int USER_ID = 1; // placeholder

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
    private TextView tvYearStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_stats);

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
        tvYearStats = findViewById(R.id.tvYearStats);

        MaterialButton btnShareYear = findViewById(R.id.btnShareYear);
        btnShareYear.setOnClickListener(v -> shareStats());

        loadStats();
    }

    private void loadStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int cities = placeDao.getUniqueCitiesCount(db, USER_ID);
            int countries = placeDao.getUniqueCountriesCount(db, USER_ID);
            int memories = memoryDao.getMemoriesByUserId(db, USER_ID).size();
            int trips = tripDao.getTripsByUserId(db, USER_ID).size();
            Place mostVisited = placeDao.getMostVisitedPlace(db, USER_ID);

            tvCitiesValue.setText(String.valueOf(cities));
            tvCountriesValue.setText(String.valueOf(countries));
            tvMemoriesValue.setText(String.valueOf(memories));
            tvTripsValue.setText(String.valueOf(trips));

            if (mostVisited != null) {
                tvMostVisited.setText(mostVisited.getName());
            } else {
                tvMostVisited.setText("—");
            }

            int year = Calendar.getInstance().get(Calendar.YEAR);
            tvYearStats.setText("You explored " + cities + " cities across " + countries +
                    " countries in " + year + " with " + memories + " memories captured.");
        }
    }
    private void shareStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int cities = placeDao.getUniqueCitiesCount(db, USER_ID);
            int countries = placeDao.getUniqueCountriesCount(db, USER_ID);
            int memories = memoryDao.getMemoriesByUserId(db, USER_ID).size();
            int trips = tripDao.getTripsByUserId(db, USER_ID).size();
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
