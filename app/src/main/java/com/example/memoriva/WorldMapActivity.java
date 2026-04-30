package com.example.memoriva;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.PlaceDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.models.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class WorldMapActivity extends BaseActivity {

    private static final int USER_ID = 1;

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private PlaceDao placeDao;
    private MapView osmMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(
                new java.io.File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(
                new java.io.File(getCacheDir(), "osmdroid/tiles"));

        setContentView(R.layout.activity_world_map);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();
        placeDao = new PlaceDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        osmMapView = findViewById(R.id.osmMapView);
        osmMapView.setTileSource(TileSourceFactory.MAPNIK);
        osmMapView.setMultiTouchControls(true);
        osmMapView.getController().setZoom(3.0);
        osmMapView.getController().setCenter(new GeoPoint(20.0, 0.0));

        // Long press on map → add memory at that location
        osmMapView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Add to this location")
                    .setItems(new String[]{"📸 Add Memory", "✈️ Add Trip"}, (dialog, which) -> {
                        if (which == 0) {
                            startActivity(new Intent(this, MemoryCreateEditActivity.class));
                        } else {
                            startActivity(new Intent(this, TripCreateEditActivity.class));
                        }
                    })
                    .show();
            return true;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_map);

        loadStatsAndPins();
    }

    private void loadStatsAndPins() {
        osmMapView.getOverlays().clear();

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> allMemories = memoryDao.getMemoriesByUserId(db, USER_ID);
            List<Place> places = placeDao.getPlacesByUserId(db, USER_ID);
            int countries = placeDao.getUniqueCountriesCount(db, USER_ID);

            // Update stats
            ((TextView) findViewById(R.id.tvPlacesCount)).setText(String.valueOf(places.size()));
            ((TextView) findViewById(R.id.tvCountriesCount)).setText(String.valueOf(countries));
            ((TextView) findViewById(R.id.tvMemoriesCount)).setText(String.valueOf(allMemories.size()));

            if (places.isEmpty()) return;

            double sumLat = 0, sumLng = 0;
            int count = 0;

            for (Place place : places) {
                if (place.getLatitude() == 0 && place.getLongitude() == 0) continue;

                GeoPoint point = new GeoPoint(place.getLatitude(), place.getLongitude());
                Marker marker = new Marker(osmMapView);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                String placeName = place.getName() != null ? place.getName() :
                        (place.getCity() != null ? place.getCity() : "Unknown");
                marker.setTitle(placeName);

                long memCount = allMemories.stream()
                        .filter(m -> m.getPlaceId() == place.getPlaceId())
                        .count();
                marker.setSnippet(memCount + " memor" + (memCount == 1 ? "y" : "ies") +
                        (place.getCountry() != null ? " · " + place.getCountry() : ""));

                Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_location);
                if (icon != null) {
                    icon.setTint(ContextCompat.getColor(this, R.color.colorPrimary));
                    marker.setIcon(icon);
                }

                final int placeId = place.getPlaceId();
                final String finalPlaceName = placeName;
                marker.setOnMarkerClickListener((m, mapView) -> {
                    m.showInfoWindow();
                    // Show options: View Memory or Add Trip
                    new androidx.appcompat.app.AlertDialog.Builder(WorldMapActivity.this)
                            .setTitle(finalPlaceName)
                            .setItems(new String[]{"📸 View Memory", "✈️ Add Trip here"}, (dialog, which) -> {
                                if (which == 0) {
                                    allMemories.stream()
                                            .filter(mem -> mem.getPlaceId() == placeId)
                                            .findFirst()
                                            .ifPresent(mem -> {
                                                Intent intent = new Intent(WorldMapActivity.this, MemoryDetailActivity.class);
                                                intent.putExtra("memory_id", mem.getMemoryId());
                                                startActivity(intent);
                                            });
                                } else {
                                    Intent intent = new Intent(WorldMapActivity.this, TripCreateEditActivity.class);
                                    intent.putExtra("destination", finalPlaceName);
                                    startActivity(intent);
                                }
                            })
                            .show();
                    return true;
                });

                osmMapView.getOverlays().add(marker);
                sumLat += place.getLatitude();
                sumLng += place.getLongitude();
                count++;
            }

            if (count > 0) {
                osmMapView.getController().animateTo(new GeoPoint(sumLat / count, sumLng / count));
                osmMapView.getController().setZoom(count == 1 ? 10.0 : 4.0);
            }
        }

        osmMapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        osmMapView.onResume();
        loadStatsAndPins();
    }

    @Override
    protected void onPause() {
        super.onPause();
        osmMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
