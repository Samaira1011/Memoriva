package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.auth.AuthManager;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.PlaceDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.models.Place;
import com.example.memoriva.utils.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class WorldMapActivity extends BaseActivity {

    private MapView mapView;
    private TextView tvPlacesCount;
    private TextView tvCountriesCount;
    private TextView tvMemoriesCount;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private PlaceDao placeDao;

    private int getUserId() {
        com.google.firebase.auth.FirebaseUser user =
                AuthManager.getInstance(this).getCurrentUser();
        return UserManager.getLocalUserId(this, user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // OSMDroid requires user-agent to be set before inflating the map view
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_world_map);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
                ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                (v, insets) -> {
                    androidx.core.graphics.Insets systemBars =
                            insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    boolean changed = v.getPaddingLeft() != systemBars.left
                            || v.getPaddingTop() != systemBars.top
                            || v.getPaddingRight() != systemBars.right
                            || v.getPaddingBottom() != systemBars.bottom;
                    if (changed) {
                        v.setPadding(systemBars.left, systemBars.top,
                                systemBars.right, systemBars.bottom);
                    }
                    return insets;
                });

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();
        placeDao = new PlaceDao();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Stats
        tvPlacesCount = findViewById(R.id.tvPlacesCount);
        tvCountriesCount = findViewById(R.id.tvCountriesCount);
        tvMemoriesCount = findViewById(R.id.tvMemoriesCount);

        // Map
        mapView = findViewById(R.id.osmMapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(3.0);
        mapView.getController().setCenter(new GeoPoint(20.0, 0.0));

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_map);

        loadMapData();
    }

    private void loadMapData() {
        int userId = getUserId();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Place> places = placeDao.getPlacesByUserId(db, userId);
            List<Memory> memories = memoryDao.getMemoriesByUserId(db, userId);
            int countriesCount = placeDao.getUniqueCountriesCount(db, userId);

            // Update stats bar
            tvPlacesCount.setText(String.valueOf(places.size()));
            tvCountriesCount.setText(String.valueOf(countriesCount));
            tvMemoriesCount.setText(String.valueOf(memories.size()));

            // Add a marker for each place
            for (Place place : places) {
                if (place.getLatitude() == 0.0 && place.getLongitude() == 0.0) continue;

                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                String title = place.getName() != null ? place.getName() : "Unknown place";
                marker.setTitle(title);

                StringBuilder snippet = new StringBuilder();
                if (place.getCity() != null && !place.getCity().isEmpty()) {
                    snippet.append(place.getCity());
                }
                if (place.getCountry() != null && !place.getCountry().isEmpty()) {
                    if (snippet.length() > 0) snippet.append(", ");
                    snippet.append(place.getCountry());
                }
                if (snippet.length() > 0) {
                    marker.setSnippet(snippet.toString());
                }

                // Tap a marker to open the place detail screen
                final int placeId = place.getPlaceId();
                marker.setOnMarkerClickListener((m, mv) -> {
                    Intent intent = new Intent(WorldMapActivity.this, PlaceDetailActivity.class);
                    intent.putExtra("place_id", placeId);
                    startActivity(intent);
                    return true;
                });

                mapView.getOverlays().add(marker);
            }

            mapView.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
