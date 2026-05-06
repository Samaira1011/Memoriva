package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.TripAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.TripDao;
import com.example.memoriva.models.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MyTripsActivity extends BaseActivity {

    private RecyclerView rvTrips;
    private TripAdapter tripAdapter;
    private List<Trip> trips = new ArrayList<>();
    private MemorivaDbHelper dbHelper;
    private TripDao tripDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_trips);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        dbHelper = new MemorivaDbHelper(this);
        tripDao = new TripDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvTrips = findViewById(R.id.rvTrips);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvTrips.setLayoutManager(gridLayoutManager);

        tripAdapter = new TripAdapter(this, trips);
        tripAdapter.setOnTripClickListener(trip -> {
            Intent intent = new Intent(this, TripDetailActivity.class);
            intent.putExtra("trip_id", trip.getTripId());
            startActivity(intent);
        });
        tripAdapter.setOnTripLongClickListener(new TripAdapter.OnTripLongClickListener() {
            @Override
            public void onTripEdit(Trip trip) {
                Intent intent = new Intent(MyTripsActivity.this, TripCreateEditActivity.class);
                intent.putExtra("trip_id", trip.getTripId());
                startActivity(intent);
            }

            @Override
            public void onTripDelete(Trip trip) {
                new androidx.appcompat.app.AlertDialog.Builder(MyTripsActivity.this)
                        .setTitle("Delete Trip")
                        .setMessage("Are you sure you want to delete \"" + trip.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                                tripDao.deleteTrip(db, trip.getTripId());
                            }
                            loadTrips();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rvTrips.setAdapter(tripAdapter);

        FloatingActionButton fabAddTrip = findViewById(R.id.fabAddTrip);
        fabAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(this, TripCreateEditActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_home);

        loadTrips();
    }

    private void loadTrips() {
        trips.clear();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Trip> loaded = tripDao.getTripsByUserId(db, 1); // placeholder userId
            trips.addAll(loaded);
        }
        tripAdapter.updateTrips(trips);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            // TODO: implement search
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
